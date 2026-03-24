package com.prj.learnvocabularybe.dto.request;

public record WordUpdateRequest(
        Long id,
        String english,
        String vietnamese
) {
}
