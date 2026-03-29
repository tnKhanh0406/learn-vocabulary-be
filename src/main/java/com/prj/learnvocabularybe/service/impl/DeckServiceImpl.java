package com.prj.learnvocabularybe.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.DeckUpdateRequest;
import com.prj.learnvocabularybe.dto.request.WordRequest;
import com.prj.learnvocabularybe.dto.request.WordUpdateRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.DeckEntity;
import com.prj.learnvocabularybe.entity.DeckWordEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.entity.VocabularyEntity;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import com.prj.learnvocabularybe.mapper.DeckMapper;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.VocabularyRepository;
import com.prj.learnvocabularybe.repository.WordMeaningRepository;
import com.prj.learnvocabularybe.service.CloudinaryService;
import com.prj.learnvocabularybe.service.DeckService;
import com.prj.learnvocabularybe.service.TranslateTtsService;
import com.prj.learnvocabularybe.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;
    private final WordMeaningRepository wordMeaningRepository;
    private final VocabularyRepository vocabularyRepository;

    private final TranslateTtsService ttsService;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<DeckSummaryResponse> getAllDecks() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return deckRepository.getDeckSummariesByUserId(userId);
    }

    @Override
    public DeckResponse getDeckById(Long id) {
        DeckEntity deckEntity = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found with id: " + id));
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(id);
        return DeckMapper.map(deckEntity, wordResponses);
    }

    @Transactional
    @Override
    public DeckResponse createDeck(DeckRequest req, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception {
        UserEntity currentUser = SecurityUtil.getCurrentUser();

        if (req.words() == null || req.words().isEmpty()) {
            throw new IllegalArgumentException("words is required");
        }

        DeckEntity deck = new DeckEntity();
        deck.setName(req.name());
        deck.setDescription(req.description());
        deck.setIsPublic(Boolean.TRUE.equals(req.isPublic()));
        deck.setUser(currentUser);
        deck.setCreatedBy(currentUser);
        deck.setIsGeneratedByAI(false);

        deck = deckRepository.save(deck);

        Map<Integer, MultipartFile> imageByIndex = toImageIndexMap(images, imageIndexes);

        for (int i = 0; i < req.words().size(); i++) {
            WordRequest wr = req.words().get(i);

            // 1) upsert vocabulary by english (word)
            VocabularyEntity vocab = upsertVocabularyWithAudio(wr.english(), deck.getId(), i);

            // 2) create meaning (owned by current user)
            WordMeaningEntity meaning = new WordMeaningEntity();
            meaning.setVocabulary(vocab);
            meaning.setUser(currentUser);
            meaning.setMeaning(wr.vietnamese());
            meaning.setExplanation(null);

            // image optional -> save to cloudinary then set url
            MultipartFile img = imageByIndex.get(i);
            if (img != null && !img.isEmpty()) {
                String publicId = "deck_" + deck.getId() + "/meaning_" + i + "_image";
                String imageUrl = cloudinaryService.uploadImage(img, "flashcards/images", publicId);
                meaning.setImageUrl(imageUrl);
            }

            meaning = wordMeaningRepository.save(meaning);

            // 3) link deck <-> meaning
            deck.addDeckWord(meaning);
        }

        DeckEntity saved = deckRepository.save(deck);
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(saved.getId());
        return DeckMapper.map(saved, wordResponses);
    }

    @Transactional
    @Override
    public DeckResponse updateDeck(Long deckId, DeckUpdateRequest req, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception {
        UserEntity currentUser = SecurityUtil.getCurrentUser();

        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found: " + deckId));

        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Forbidden");
        }

        if (req.name() != null) deck.setName(req.name());
        if (req.description() != null) deck.setDescription(req.description());
        if (req.isPublic() != null) deck.setIsPublic(req.isPublic());

        if (req.words() == null) {
            throw new IllegalArgumentException("words is required");
        }

        Map<Integer, MultipartFile> imageByIndex = toImageIndexMap(images, imageIndexes);

        // current meanings in deck (ids)
        Set<Long> existingMeaningIds = deck.getDeckWords().stream()
                .map(dw -> dw.getWordMeaning().getId())
                .collect(Collectors.toSet());

        // ids requested (non-null)
        Set<Long> requestedMeaningIds = req.words().stream()
                .map(WordUpdateRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // meanings removed by user => delete DeckWord + delete WordMeaning (NOT vocabulary)
        Set<Long> removedMeaningIds = new HashSet<>(existingMeaningIds);
        removedMeaningIds.removeAll(requestedMeaningIds);
        if (!removedMeaningIds.isEmpty()) {
            // remove from deckWords list
            deck.getDeckWords().removeIf(dw -> removedMeaningIds.contains(dw.getWordMeaning().getId()));
            // delete meanings
            wordMeaningRepository.deleteAllById(removedMeaningIds);
        }

        // Now handle requested list in order.
        // Rule of yours: "nếu từ nào bị user xóa hoặc sửa thì sẽ xóa meaning"
        // => Với item có id != null, nếu english/vietnamese thay đổi HOẶC có ảnh mới => delete old meaning and create new one.
        // Với item id == null => create new one.
        //
        // Implement bằng cách build deckWords mới theo request order.
        Map<Long, WordMeaningEntity> currentMeaningById = deck.getDeckWords().stream()
                .map(DeckWordEntity::getWordMeaning)
                .collect(Collectors.toMap(WordMeaningEntity::getId, wm -> wm));

        List<DeckWordEntity> newDeckWords = new ArrayList<>();

        for (int i = 0; i < req.words().size(); i++) {
            WordUpdateRequest wr = req.words().get(i);
            MultipartFile img = imageByIndex.get(i);

            if (wr.id() == null) {
                WordMeaningEntity created = createMeaningForDeckItem(currentUser, deck.getId(), i, wr.english(), wr.vietnamese(), img);
                newDeckWords.add(newDeckWord(deck, created));
                continue;
            }

            WordMeaningEntity oldMeaning = currentMeaningById.get(wr.id());
            if (oldMeaning == null) {
                // Client gửi id không thuộc deck hoặc đã bị xóa
                throw new IllegalArgumentException("WordMeaning id not in this deck: " + wr.id());
            }

            boolean hasNewImage = (img != null && !img.isEmpty());
            boolean englishChanged = !oldMeaning.getVocabulary().getWord().equalsIgnoreCase(wr.english());
            boolean meaningChanged = !Objects.equals(oldMeaning.getMeaning(), wr.vietnamese());

            if (englishChanged || meaningChanged || hasNewImage) {
                // delete old meaning + create new meaning (per your rule)
                // 1) remove link (not strictly required if we rebuild list)
                // 2) delete old meaning entity (vocab stays)
                wordMeaningRepository.delete(oldMeaning);

                WordMeaningEntity created = createMeaningForDeckItem(currentUser, deck.getId(), i, wr.english(), wr.vietnamese(), img);
                newDeckWords.add(newDeckWord(deck, created));
            } else {
                // unchanged => keep old meaning (no new image)
                newDeckWords.add(newDeckWord(deck, oldMeaning));
            }
        }

        // Replace deckWords with new list (orphanRemoval deletes removed DeckWord rows)
        deck.getDeckWords().clear();
        deck.getDeckWords().addAll(newDeckWords);

        DeckEntity saved = deckRepository.save(deck);
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(saved.getId());
        return DeckMapper.map(saved, wordResponses);
    }

    @Override
    public void deleteDeck(Long id) {
        DeckEntity deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + id));

        // Xóa meanings thuộc deck theo rule? (hiện tại yêu cầu chỉ nói update)
        // Nếu bạn muốn delete deck thì cũng delete WordMeaning (owned by deck owner) để không rác:
        Set<Long> meaningIds = deck.getDeckWords().stream()
                .map(dw -> dw.getWordMeaning().getId())
                .collect(Collectors.toSet());

        deckRepository.delete(deck); // orphanRemoval deletes DeckWord rows

        if (!meaningIds.isEmpty()) {
            wordMeaningRepository.deleteAllById(meaningIds);
        }
    }

    private DeckWordEntity newDeckWord(DeckEntity deck, WordMeaningEntity meaning) {
        DeckWordEntity dw = new DeckWordEntity();
        dw.setDeck(deck);
        dw.setWordMeaning(meaning);
        return dw;
    }

    private WordMeaningEntity createMeaningForDeckItem(
            UserEntity currentUser,
            Long deckId,
            int index,
            String english,
            String vietnamese,
            MultipartFile img
    ) throws Exception {
        VocabularyEntity vocab = upsertVocabularyWithAudio(english, deckId, index);

        WordMeaningEntity meaning = new WordMeaningEntity();
        meaning.setVocabulary(vocab);
        meaning.setUser(currentUser);
        meaning.setMeaning(vietnamese);
        meaning.setExplanation(null);

        if (img != null && !img.isEmpty()) {
            String publicId = "deck_" + deckId + "/meaning_" + index + "_image";
            String imageUrl = cloudinaryService.uploadImage(img, "flashcards/images", publicId);
            meaning.setImageUrl(imageUrl);
        }

        return wordMeaningRepository.save(meaning);
    }

    private VocabularyEntity upsertVocabularyWithAudio(String english, Long deckId, int index) throws Exception {
        String normalized = english == null ? null : english.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("english is required");
        }

        VocabularyEntity vocab = vocabularyRepository.findByWordIgnoreCase(normalized)
                .orElseGet(() -> {
                    VocabularyEntity v = new VocabularyEntity();
                    v.setWord(normalized);
                    return v;
                });

        // Ensure audio_url exists (only generate if missing)
        if (vocab.getAudioUrl() == null || vocab.getAudioUrl().isBlank()) {
            byte[] mp3 = ttsService.synthesizeEnglishToMp3(normalized);
            String audioPublicId = "vocab/" + sanitize(normalized) + "_audio";
            String audioUrl = cloudinaryService.uploadAudioMp3(mp3, "flashcards/audio", audioPublicId);
            vocab.setAudioUrl(audioUrl);
        }

        return vocabularyRepository.save(vocab);
    }

    private String sanitize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9_\\-]+", "_");
    }

    private Map<Integer, MultipartFile> toImageIndexMap(List<MultipartFile> images, List<Integer> imageIndexes) {
        Map<Integer, MultipartFile> map = new HashMap<>();
        if (images == null || imageIndexes == null) return map;

        if (images.size() != imageIndexes.size()) {
            throw new IllegalArgumentException("images and imageIndexes must have same size");
        }
        for (int i = 0; i < images.size(); i++) {
            map.put(imageIndexes.get(i), images.get(i));
        }
        return map;
    }
}