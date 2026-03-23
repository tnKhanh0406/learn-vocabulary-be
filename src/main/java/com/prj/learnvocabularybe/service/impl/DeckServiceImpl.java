package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.WordRequest;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
