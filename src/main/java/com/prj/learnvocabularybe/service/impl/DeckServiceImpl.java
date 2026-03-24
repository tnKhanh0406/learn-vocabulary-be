package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.DeckUpdateRequest;
import com.prj.learnvocabularybe.dto.request.WordRequest;
import com.prj.learnvocabularybe.dto.request.WordUpdateRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.DeckEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.entity.WordEntity;
import com.prj.learnvocabularybe.mapper.DeckMapper;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.WordRepository;
import com.prj.learnvocabularybe.service.CloudinaryService;
import com.prj.learnvocabularybe.service.DeckService;
import com.prj.learnvocabularybe.service.TranslateTtsService;
import com.prj.learnvocabularybe.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;
    private final WordRepository wordRepository;
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
        List<WordResponse> wordResponses = wordRepository.findWordsByDeckId(id);
        return DeckMapper.map(deckEntity, wordResponses);
    }

    @Transactional
    @Override
    public DeckResponse createDeck(DeckRequest req, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception {
        UserEntity currentUser = SecurityUtil.getCurrentUser();

        DeckEntity deck = new DeckEntity();
        deck.setName(req.name());
        deck.setDescription(req.description());
        deck.setIsPublic(req.isPublic());
        deck.setUser(currentUser);

        deck = deckRepository.save(deck);

        Map<Integer, MultipartFile> imageByIndex = toImageIndexMap(images, imageIndexes);

        List<WordRequest> words = req.words();
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("words is required");
        }

        for (int i = 0; i < words.size(); i++) {
            WordRequest wr = words.get(i);

            WordEntity w = new WordEntity();
            w.setEnglish(wr.english());
            w.setVietnamese(wr.vietnamese());

            // 1) image optional
            MultipartFile img = imageByIndex.get(i);
            if (img != null && !img.isEmpty()) {
                String publicId = "deck_" + deck.getId() + "/word_" + i + "_image";
                String imageUrl = cloudinaryService.uploadImage(img, "flashcards/images", publicId);
                w.setImageUrl(imageUrl);
            }

            // 2) audio for english
            byte[] mp3 = ttsService.synthesizeEnglishToMp3(wr.english());
            String audioPublicId = "deck_" + deck.getId() + "/word_" + i + "_audio";
            String audioUrl = cloudinaryService.uploadAudioMp3(mp3, "flashcards/audio", audioPublicId);
            w.setAudioUrl(audioUrl);

            // 3) gắn deck_id
            deck.addWord(w);
        }

        // cascade => save words
        DeckEntity saved = deckRepository.save(deck);
        List<WordResponse> wordResponses = wordRepository.findWordsByDeckId(saved.getId());
        return DeckMapper.map(saved, wordResponses);
    }

    @Override
    public DeckResponse updateDeck(Long deckId,
                                   DeckUpdateRequest req,
                                   List<MultipartFile> images,
                                   List<Integer> imageIndexes) throws Exception {
        UserEntity currentUser = SecurityUtil.getCurrentUser();

        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found: " + deckId));

        // (tuỳ bạn) check quyền sở hữu
        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Forbidden");
        }

        // Update deck fields
        if (req.name() != null) deck.setName(req.name());
        if (req.description() != null) deck.setDescription(req.description());
        if (req.isPublic() != null) deck.setIsPublic(req.isPublic());

        if (req.words() == null) {
            throw new IllegalArgumentException("words is required");
        }

        Map<Integer, MultipartFile> imageByIndex = toImageIndexMap(images, imageIndexes);

        // Map existing words by id
        Map<Long, WordEntity> existingById = deck.getWords().stream()
                .filter(w -> w.getId() != null)
                .collect(Collectors.toMap(WordEntity::getId, Function.identity()));

        // Track ids that remain after update
        Set<Long> keepIds = new HashSet<>();

        // Rebuild words list in the same order as request (optional)
        List<WordEntity> newWordsList = new ArrayList<>();

        for (int i = 0; i < req.words().size(); i++) {
            WordUpdateRequest wr = req.words().get(i);

            WordEntity word;
            boolean isNew = (wr.id() == null);

            if (isNew) {
                word = new WordEntity();
                word.setDeck(deck);
            } else {
                word = existingById.get(wr.id());
                if (word == null) {
                    throw new IllegalArgumentException("Word id not in this deck: " + wr.id());
                }
                keepIds.add(wr.id());
            }

            // Detect english change to regenerate audio
            String oldEnglish = word.getEnglish();
            String newEnglish = wr.english();

            word.setEnglish(newEnglish);
            word.setVietnamese(wr.vietnamese());

            // Update image if file provided for this index
            MultipartFile img = imageByIndex.get(i);
            if (img != null && !img.isEmpty()) {
                String publicId = "deck_" + deck.getId() + "/word_" + (isNew ? "new_" + i : word.getId()) + "_image";
                String imageUrl = cloudinaryService.uploadImage(img, "flashcards/images", publicId);
                word.setImageUrl(imageUrl);
            }

            // Create/regenerate audio when needed
            boolean englishChanged = oldEnglish == null || !oldEnglish.equalsIgnoreCase(newEnglish);
            if (isNew || englishChanged || word.getAudioUrl() == null || word.getAudioUrl().isBlank()) {
                byte[] mp3 = ttsService.synthesizeEnglishToMp3(newEnglish);
                String audioPublicId = "deck_" + deck.getId() + "/word_" + (isNew ? "new_" + i : word.getId()) + "_audio";
                String audioUrl = cloudinaryService.uploadAudioMp3(mp3, "flashcards/audio", audioPublicId);
                word.setAudioUrl(audioUrl);
            }

            newWordsList.add(word);
        }

        deck.getWords().clear();
        for (WordEntity w : newWordsList) deck.addWord(w);

        DeckEntity saved = deckRepository.save(deck);
        List<WordResponse> wordResponses = wordRepository.findWordsByDeckId(saved.getId());
        return DeckMapper.map(saved, wordResponses);
    }

    @Override
    public void deleteDeck(Long id) {
        DeckEntity deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + id));
        deckRepository.delete(deck);
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
