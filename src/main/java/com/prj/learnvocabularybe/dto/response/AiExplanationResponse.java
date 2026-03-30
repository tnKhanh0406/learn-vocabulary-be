package com.prj.learnvocabularybe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiExplanationResponse {
    private String meaning;       // Nghĩa do AI dịch
    private String explanation;   // Giải thích ngữ cảnh
    private String example;       // Câu ví dụ
    private Boolean isFromCache;  // Báo cho FE biết dữ liệu này lấy từ DB hay API thật (Tùy chọn)
}