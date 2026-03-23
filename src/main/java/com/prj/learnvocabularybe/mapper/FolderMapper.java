package com.prj.learnvocabularybe.mapper;

import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;
import com.prj.learnvocabularybe.entity.FolderEntity;

public class FolderMapper {
    public static FolderSummaryResponse map(FolderEntity entity) {
        return new FolderSummaryResponse(
                entity.getId(),
                entity.getName(),
                entity.getUser().getUsername()
        );
    }
}
