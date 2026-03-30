package com.prj.learnvocabularybe.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyWordResponse {
    private Long progressId;       // ID của bảng tiến độ
    private Long wordMeaningId;    // ID của nghĩa từ
    private String word;           // Từ gốc (VD: "Apple")
    private String meaning;        // Nghĩa tiếng Việt
    private String pronunciation;  // Phiên âm
    private String audioUrl;       // Link âm thanh
    
    // Các thông số tracking để client hiển thị nếu cần
    private Integer repetitionCount;
    private Integer lapses;
}