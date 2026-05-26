package com.prj.learnvocabularybe.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.prj.learnvocabularybe.repository.DeckWordRepository;
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

/**
 * Cài đặt nghiệp vụ thao tác với deck: tạo, sửa, xóa, sao chép và gắn vào folder.
 */
@Service
@RequiredArgsConstructor
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;
    private final WordMeaningRepository wordMeaningRepository;
    private final VocabularyRepository vocabularyRepository;
    private final DeckWordRepository deckWordRepository;

    private final TranslateTtsService ttsService;
    private final CloudinaryService cloudinaryService;
    private final SecurityUtil securityUtil;

    /**
     * Lấy danh sách deck của người dùng hiện tại, có hỗ trợ tìm kiếm.
     */
    @Override
    public List<DeckSummaryResponse> getAllDecks(String q) {
        Long userId = securityUtil.getCurrentUser().getId();
        return deckRepository.searchMyDecksByName(userId, q);
    }

    /**
     * Lấy chi tiết deck nếu người dùng có quyền xem.
     */
    @Override
    public DeckResponse getDeckById(Long id) {
        DeckEntity deckEntity = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found with id: " + id));
        boolean isOwner = deckEntity.getUser().getId().equals(securityUtil.getCurrentUser().getId());
        boolean canAccess = isOwner || Boolean.TRUE.equals(deckEntity.getIsPublic());
        if (!canAccess) {
            throw new RuntimeException("Forbidden");
        }
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(id);
        return DeckMapper.map(deckEntity, wordResponses);
    }

    /**
     * Tạo deck mới cùng danh sách từ, ảnh và âm thanh đi kèm.
     */
    @Transactional
    @Override
    public DeckResponse createDeck(DeckRequest req, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception {
        UserEntity currentUser = securityUtil.getCurrentUser();

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

    /**
     * Cập nhật deck hiện có, giữ thứ tự từ theo request.
     */
    @Transactional
    @Override
    public DeckResponse updateDeck(Long deckId,
                                   DeckUpdateRequest req,
                                   List<MultipartFile> images,
                                   List<Integer> imageIndexes) throws Exception {
        UserEntity currentUser = securityUtil.getCurrentUser();

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

    /**
     * Xóa deck và dọn các word meaning không còn được dùng.
     */
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

    /**
     * Lấy các deck của người dùng hiện tại chưa nằm trong folder nào.
     */
    @Override
    public List<DeckSummaryResponse> getAllDecksNotInFolder() {
        return deckRepository.getDeckSummariesNotInFolderByUserId(securityUtil.getCurrentUser().getId());
    }

    /**
     * Gắn một deck vào folder.
     */
    @Override
    public DeckResponse addDeckToFolder(Long deckId, Long folderId) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));
        if (!deck.getUser().getId().equals(securityUtil.getCurrentUser().getId())) {
            throw new RuntimeException("Forbidden");
        }
        deckRepository.addDeckToFolder(deckId, folderId);
        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(deck.getId());
        return DeckMapper.map(deck, wordResponses);
    }

    /**
     * Sao chép deck hiện có cho người dùng hiện tại.
     */
    @Transactional
    @Override
    public DeckResponse copyDeck(Long sourceDeckId) {
        UserEntity currentUser = securityUtil.getCurrentUser();

        DeckEntity source = deckRepository.findByIdWithWords(sourceDeckId)
                .orElseThrow(() -> new RuntimeException("Deck not found: " + sourceDeckId));

        boolean isOwner = source.getUser().getId().equals(currentUser.getId());
        boolean canCopy = isOwner || Boolean.TRUE.equals(source.getIsPublic());
        if (!canCopy) throw new RuntimeException("Forbidden");

        DeckEntity copy = new DeckEntity();
        copy.setUser(currentUser);
        copy.setCopiedFromDeck(source);
        copy.setCreatedBy(currentUser);
        copy.setIsGeneratedByAI(false);
        copy.setName("Copy of " + source.getName());
        copy.setDescription(source.getDescription());
        copy.setIsPublic(source.getIsPublic());

        copy = deckRepository.save(copy);

        List<WordMeaningEntity> newMeanings = new ArrayList<>();
        // copy meanings
        for (DeckWordEntity srcDw : source.getDeckWords()) {
            WordMeaningEntity newMeaning = getWordMeaningEntity(srcDw, currentUser);

//            newMeaning = wordMeaningRepository.save(newMeaning);
            newMeanings.add(newMeaning);
            DeckWordEntity newDw = new DeckWordEntity();
            newDw.setDeck(copy);
            newDw.setWordMeaning(newMeaning);
            copy.getDeckWords().add(newDw);
        }
        wordMeaningRepository.saveAll(newMeanings);
        DeckEntity saved = deckRepository.save(copy);

        List<WordResponse> wordResponses = wordMeaningRepository.findWordResponsesByDeckId(saved.getId());
        return DeckMapper.map(saved, wordResponses);
    }

    /**
     * Tìm deck public theo tên.
     */
    @Override
    public List<DeckSummaryResponse> searchPublicDecksByName(String q) {
        return deckRepository.searchPublicDecksByName(securityUtil.getCurrentUser().getId(), q);
    }

    /**
     * Lấy các deck public của một user theo id.
     */
    @Override
    public List<DeckSummaryResponse> getPublicDecksByUserId(Long userId) {
        return deckRepository.searchPublicDecksByUserId(userId);
    }

    /**
     * Sao chép dữ liệu WordMeaning để dùng trong deck mới.
     */
    private static WordMeaningEntity getWordMeaningEntity(DeckWordEntity srcDw, UserEntity currentUser) {
        WordMeaningEntity srcMeaning = srcDw.getWordMeaning();

        WordMeaningEntity newMeaning = new WordMeaningEntity();
        newMeaning.setVocabulary(srcMeaning.getVocabulary());
        newMeaning.setUser(currentUser);
        newMeaning.setMeaning(srcMeaning.getMeaning());
        newMeaning.setExplanation(null);
        newMeaning.setImageUrl(srcMeaning.getImageUrl());
        return newMeaning;
    }

    /**
     * Tạo một liên kết deck-word mới.
     */
    private DeckWordEntity newDeckWord(DeckEntity deck, WordMeaningEntity meaning) {
        DeckWordEntity dw = new DeckWordEntity();
        dw.setDeck(deck);
        dw.setWordMeaning(meaning);
        return dw;
    }

    /**
     * Tạo mới meaning cho một item của deck và lưu ảnh nếu có.
     */
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

    /**
     * Lấy hoặc tạo vocabulary và đảm bảo có audio MP3 đi kèm.
     */
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

    /**
     * Lấy vocabulary hiện có hoặc tạo mới theo từ tiếng Anh.
     */
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

    /**
     * Chuẩn hóa chuỗi dùng làm publicId trên Cloudinary.
     */
    private String sanitize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9_\\-]+", "_");
    }

    /**
     * Chuyển danh sách ảnh và vị trí ảnh thành map để truy cập nhanh theo index.
     */
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