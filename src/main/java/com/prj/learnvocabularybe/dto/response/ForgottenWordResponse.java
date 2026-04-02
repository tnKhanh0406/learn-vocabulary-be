package com.prj.learnvocabularybe.dto.response;

public record ForgottenWordResponse(
        Long wordMeaningId,
        String word,
        String meaning,
        Integer lapses,
        String audioUrl
) {
}
