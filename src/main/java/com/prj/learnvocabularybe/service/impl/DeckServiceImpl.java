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

        Set<String> words = req.words().stream()
                .map(w -> w.english().trim().toLowerCase())
                .collect(Collectors.toSet());
        List<VocabularyEntity> existingVocabs = vocabularyRepository.findAllByWordInIgnoreCase(words);
        Map<String, VocabularyEntity> vocabMap = existingVocabs.stream()
                .collect(Collectors.toMap(v -> v.getWord().toLowerCase(), v -> v));

        List<DeckWordEntity> deckWords = new ArrayList<>();
        List<WordMeaningEntity> meanings = new ArrayList<>();
        for (int i = 0; i < req.words().size(); i++) {
            WordRequest wr = req.words().get(i);

            VocabularyEntity vocab = getOrCreateVocab(wr.english(), vocabMap);

            // create meaning
            WordMeaningEntity meaning = new WordMeaningEntity();
            meaning.setVocabulary(vocab);
            meaning.setUser(currentUser);
            meaning.setMeaning(wr.vietnamese());
            meaning.setExplanation(null);

            // save image to cloudinary
            MultipartFile img = imageByIndex.get(i);
            if (img != null && !img.isEmpty()) {
                String publicId = "deck_" + deck.getId() + "/meaning_" + i + "_image";
                String imageUrl = cloudinaryService.uploadImage(img, "flashcards/images", publicId);
                meaning.setImageUrl(imageUrl);
            }

            meanings.add(meaning);
        }
        wordMeaningRepository.saveAll(meanings);

        for (WordMeaningEntity m : meanings) {
            DeckWordEntity dw = new DeckWordEntity();
            dw.setDeck(deck);
            dw.setWordMeaning(m);
            deckWords.add(dw);
        }
        deckWordRepository.saveAll(deckWords);
        DeckEntity saved = deckRepository.save(deck);
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(saved.getId());
        return DeckMapper.map(saved, wordResponses);
    }

    @Transactional
    @Override
    public DeckResponse updateDeck(Long deckId,
                                   DeckUpdateRequest req,
                                   List<MultipartFile> images,
                                   List<Integer> imageIndexes) throws Exception {
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

        // Map existing DeckWord by wordMeaningId
        Map<Long, DeckWordEntity> existingDeckWordByMeaningId = deck.getDeckWords().stream()
                .collect(Collectors.toMap(dw -> dw.getWordMeaning().getId(), dw -> dw));

        // 1. Remove relations that user deleted: delete DeckWord only
        Set<Long> requestedMeaningIds = req.words().stream()
                .map(WordUpdateRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        deck.getDeckWords().removeIf(dw -> !requestedMeaningIds.contains(dw.getWordMeaning().getId()));

        // 2. Build new deckWords list in request order
        List<DeckWordEntity> newDeckWords = new ArrayList<>();

        for (int i = 0; i < req.words().size(); i++) {
            WordUpdateRequest wr = req.words().get(i);
            MultipartFile img = imageByIndex.get(i);

            //New item: create WordMeaning + link
            if (wr.id() == null) {
                WordMeaningEntity createdMeaning = createMeaningForDeckItem(
                        currentUser, deck.getId(), i, wr.english(), wr.vietnamese(), img
                );
                newDeckWords.add(newDeckWord(deck, createdMeaning));
                continue;
            }

            //Existing item: update meaning or create new meaning based on vocab change
            DeckWordEntity dw = existingDeckWordByMeaningId.get(wr.id());

            WordMeaningEntity meaning = dw.getWordMeaning();

            // vocab currently attached to that meaning
            String oldEnglish = meaning.getVocabulary().getWord();
            String newEnglish = wr.english() == null ? "" : wr.english().trim();

            boolean vocabChanged = !oldEnglish.equalsIgnoreCase(newEnglish);

            if (!vocabChanged) {
                // Case: only meaning/image changed -> update WordMeaning
                boolean meaningChanged = !Objects.equals(meaning.getMeaning(), wr.vietnamese());
                if (meaningChanged) {
                    meaning.setMeaning(wr.vietnamese());
                }

                if (img != null && !img.isEmpty()) {
                    String publicId = "deck_" + deck.getId() + "/meaning_" + meaning.getId() + "_image";
                    String imageUrl = cloudinaryService.uploadImage(img, "flashcards/images", publicId);
                    meaning.setImageUrl(imageUrl);
                }

                wordMeaningRepository.save(meaning);

                newDeckWords.add(dw);
            } else {
                // Case: vocab changed -> create new WordMeaning and change relation
                WordMeaningEntity newMeaning = createMeaningForDeckItem(
                        currentUser, deck.getId(), i, newEnglish, wr.vietnamese(), img
                );

                dw.setWordMeaning(newMeaning);

                newDeckWords.add(dw);
            }
        }

        // Replace order according to request
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

        // current meanings in deck
        Set<Long> meaningIds = deck.getDeckWords().stream()
                .map(dw -> dw.getWordMeaning().getId())
                .collect(Collectors.toSet());

        deckRepository.delete(deck);

        if (!meaningIds.isEmpty()) {
            wordMeaningRepository.deleteUnusedWordMeanings(meaningIds);
        }
    }

    @Override
    public List<DeckSummaryResponse> getAllDecksNotInFolder() {
        return deckRepository.getDeckSummariesNotInFolderByUserId(SecurityUtil.getCurrentUser().getId());
    }

    @Override
    public DeckResponse addDeckToFolder(Long deckId, Long folderId) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));
        if (!deck.getUser().getId().equals(SecurityUtil.getCurrentUser().getId())) {
            throw new RuntimeException("Forbidden");
        }
        deckRepository.addDeckToFolder(deckId, folderId);
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(deck.getId());
        return DeckMapper.map(deck, wordResponses);
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
        VocabularyEntity vocab = upsertVocabularyWithAudio(english);

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

    private VocabularyEntity upsertVocabularyWithAudio(String english) throws Exception {
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

        if (vocab.getAudioUrl() == null || vocab.getAudioUrl().isBlank()) {
            byte[] mp3 = ttsService.synthesizeEnglishToMp3(normalized);
            String audioPublicId = "vocab/" + sanitize(normalized) + "_audio";
            String audioUrl = cloudinaryService.uploadAudioMp3(mp3, "flashcards/audio", audioPublicId);
            vocab.setAudioUrl(audioUrl);
        }

        return vocabularyRepository.save(vocab);
    }

    private VocabularyEntity getOrCreateVocab(String word, Map<String, VocabularyEntity> vocabMap) throws Exception {
        String key = word.trim().toLowerCase();

        VocabularyEntity vocab = vocabMap.get(key);

        if (vocab == null) {
            vocab = new VocabularyEntity();
            vocab.setWord(word);

            byte[] mp3 = ttsService.synthesizeEnglishToMp3(word);
            String audioUrl = cloudinaryService.uploadAudioMp3(
                    mp3, "flashcards/audio", "vocab/" + key
            );
            vocab.setAudioUrl(audioUrl);

            vocab = vocabularyRepository.save(vocab);
            vocabMap.put(key, vocab);
        }
        return vocab;
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