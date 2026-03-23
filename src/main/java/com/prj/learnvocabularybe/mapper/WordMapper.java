package com.prj.learnvocabularybe.mapper;

import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.WordEntity;

public class WordMapper {
    public static WordResponse map(WordEntity wordEntity) {
        return new WordResponse(
                wordEntity.getId(),
                wordEntity.getEnglish(),
                wordEntity.getVietnamese(),
                wordEntity.getImageUrl(),
                wordEntity.getAudioUrl()
        );
    }
}
