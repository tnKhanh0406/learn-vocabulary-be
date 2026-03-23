package com.prj.learnvocabularybe.dto.response;

public record WordResponse(
        Long id,
        String english,
        String vietnamese,
        String imageUrl,
        String audioUrl
) {
}
