package com.prj.learnvocabularybe.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prj.learnvocabularybe.dto.request.ReviewActionRequest;
import com.prj.learnvocabularybe.dto.response.StudyWordResponse;
import com.prj.learnvocabularybe.entity.ReviewLogsEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.entity.UserWordProgressEntity;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import com.prj.learnvocabularybe.repository.ReviewLogsRepository;
import com.prj.learnvocabularybe.repository.UserRepository;
import com.prj.learnvocabularybe.repository.UserWordProgressRepository;
import com.prj.learnvocabularybe.repository.WordMeaningRepository;
import com.prj.learnvocabularybe.service.SpacedRepetitionService;

@Service
public class SpacedRepetitionServiceImpl implements SpacedRepetitionService {

    @Autowired
    private UserWordProgressRepository progressRepository;

    @Autowired
    private ReviewLogsRepository reviewLogsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordMeaningRepository wordMeaningRepository;

    @Override
    @Transactional
    public void processUserReview(Long userId, ReviewActionRequest request) {
        // 1. Tìm bản ghi tiến độ (nếu chưa học bao giờ thì tạo mới)
        UserWordProgressEntity progress = progressRepository
                .findByUserIdAndWordMeaningId(userId, request.getWordMeaningId())
                .orElseGet(() -> createNewProgress(userId, request.getWordMeaningId()));

        int grade = request.getGrade(); // 1: Quên, 2: Khó, 3: Nhớ, 4: Rất nhớ

        // Lấy các giá trị hiện tại
        int currentLapses = progress.getLapses() != null ? progress.getLapses() : 0;
        int currentRepetition = progress.getRepetitionCount() != null ? progress.getRepetitionCount() : 0;
        float currentEaseFactor = progress.getEaseFactor() != null ? progress.getEaseFactor() : 2.5f;
        int currentInterval = progress.getIntervalDays() != null ? progress.getIntervalDays() : 0;

        // 2. Thuật toán SM-2
        if (grade == 1) { // Quên
            currentRepetition = 0;
            currentInterval = 1;
            currentLapses += 1;
            currentEaseFactor = Math.max(1.3f, currentEaseFactor - 0.2f);
        } else { // Nhớ
            if (currentRepetition == 0) {
                currentInterval = 1;
            } else if (currentRepetition == 1) {
                currentInterval = 6;
            } else {
                currentInterval = Math.round(currentInterval * currentEaseFactor);
            }
            currentRepetition += 1;
            currentEaseFactor = currentEaseFactor + (0.1f - (4 - grade) * (0.08f + (4 - grade) * 0.02f));
            currentEaseFactor = Math.max(1.3f, currentEaseFactor); 
        }

        // 3. Cập nhật tiến độ
        progress.setRepetitionCount(currentRepetition);
        progress.setIntervalDays(currentInterval);
        progress.setEaseFactor(currentEaseFactor);
        progress.setLapses(currentLapses);
        progress.setLastReviewDate(LocalDateTime.now());
        progress.setNextReviewDate(LocalDateTime.now().plusDays(currentInterval));
        progressRepository.save(progress);

        // 4. Ghi log
        ReviewLogsEntity log = ReviewLogsEntity.builder()
                .user(progress.getUser())
                .wordMeaning(progress.getWordMeaning())
                .grade(grade)
                .timeTakenMs(request.getTimeTakenMs())
                .reviewedAt(LocalDateTime.now())
                .build();
        reviewLogsRepository.save(log);
    }

    @Override
    public List<StudyWordResponse> getWordsToReviewToday(Long userId) {
        // Lấy các từ có next_review_date <= hiện tại
        List<UserWordProgressEntity> wordsToReview = progressRepository.findWordsToReviewToday(userId, LocalDateTime.now());
        
        // Map từ Entity sang DTO để trả về cho Frontend
        return wordsToReview.stream().map(progress -> {
            WordMeaningEntity meaning = progress.getWordMeaning();
            return StudyWordResponse.builder()
                    .progressId(progress.getId())
                    .wordMeaningId(meaning.getId())
                    .word(meaning.getVocabulary().getWord()) // Lấy từ bảng Vocabulary
                    .meaning(meaning.getMeaning())
                    .audioUrl(meaning.getVocabulary().getAudioUrl())
                    .repetitionCount(progress.getRepetitionCount())
                    .lapses(progress.getLapses())
                    .build();
        }).collect(Collectors.toList());
    }

    // Hàm phụ trợ tạo bản ghi mới
    private UserWordProgressEntity createNewProgress(Long userId, Long wordMeaningId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WordMeaningEntity wordMeaning = wordMeaningRepository.findById(wordMeaningId)
                .orElseThrow(() -> new RuntimeException("Word Meaning not found"));

        return UserWordProgressEntity.builder()
                .user(user)
                .wordMeaning(wordMeaning)
                .easeFactor(2.5f)
                .repetitionCount(0)
                .intervalDays(0)
                .lapses(0)
                .build();
    }
}