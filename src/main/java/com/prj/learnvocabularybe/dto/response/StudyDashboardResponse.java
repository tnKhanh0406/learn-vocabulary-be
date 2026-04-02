package com.prj.learnvocabularybe.dto.response;

import java.util.List;

public record StudyDashboardResponse(
        int memoryRate,
        int streakDays,
        long trackedWords,
        List<DeckSummaryResponse> recommendedDecks,
        List<ForgottenWordResponse> forgottenWords
) {
}
