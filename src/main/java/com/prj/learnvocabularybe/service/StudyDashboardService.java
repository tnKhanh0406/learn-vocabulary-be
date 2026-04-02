package com.prj.learnvocabularybe.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.dto.response.ForgottenWordResponse;
import com.prj.learnvocabularybe.dto.response.StudyDashboardResponse;
import com.prj.learnvocabularybe.entity.UserWordProgressEntity;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.ReviewLogsRepository;
import com.prj.learnvocabularybe.repository.UserWordProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyDashboardService {

    private static final int FORGOTTEN_LAPSES_THRESHOLD = 2;
    private static final int MAX_RECOMMENDED_DECKS = 3;
    private static final int MAX_FORGOTTEN_WORDS = 8;

    private final UserWordProgressRepository progressRepository;
    private final ReviewLogsRepository reviewLogsRepository;
    private final DeckRepository deckRepository;

    @Transactional(readOnly = true)
    public StudyDashboardResponse getDashboard(Long userId) {
        long trackedWords = progressRepository.countByUserId(userId);
        long rememberedWords = progressRepository.countRememberedWords(userId);
        int memoryRate = trackedWords == 0
                ? 0
                : (int) Math.round((rememberedWords * 100.0) / trackedWords);

        List<LocalDate> reviewDates = reviewLogsRepository.findDistinctReviewDates(userId);
        int streakDays = calculateStreak(reviewDates);

        List<DeckSummaryResponse> recommendedDecks = deckRepository.findTopRecommendedPublicDecks(userId)
                .stream()
                .limit(MAX_RECOMMENDED_DECKS)
                .toList();

        List<ForgottenWordResponse> forgottenWords = progressRepository
                .findForgottenWords(userId, FORGOTTEN_LAPSES_THRESHOLD)
                .stream()
                .sorted((a, b) -> Integer.compare(
                        valueOrZero(b.getLapses()),
                        valueOrZero(a.getLapses())
                ))
                .limit(MAX_FORGOTTEN_WORDS)
                .map(this::toForgottenWord)
                .toList();

        return new StudyDashboardResponse(memoryRate, streakDays, trackedWords, recommendedDecks, forgottenWords);
    }

    private int calculateStreak(List<LocalDate> reviewDates) {
        if (reviewDates == null || reviewDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> dateSet = Set.copyOf(reviewDates);
        LocalDate cursor = reviewDates.stream()
                .max(LocalDate::compareTo)
                .orElse(null);
        if (cursor == null) {
            return 0;
        }
        int streak = 0;

        while (dateSet.contains(cursor)) {
            streak += 1;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    private ForgottenWordResponse toForgottenWord(UserWordProgressEntity progress) {
        var meaning = progress.getWordMeaning();
        var vocab = meaning.getVocabulary();

        return new ForgottenWordResponse(
                meaning.getId(),
                vocab.getWord(),
                meaning.getMeaning(),
                progress.getLapses(),
                vocab.getAudioUrl()
        );
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
