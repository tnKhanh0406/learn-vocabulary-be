package com.prj.learnvocabularybe.dto.request;

import lombok.Data;

@Data
public class ReviewActionRequest {
    private Long wordMeaningId; // ID của từ vừa học
    private Integer grade;        // Điểm đánh giá (1: Quên, 2: Hơi nhớ, 3: Nhớ, 4: Rất nhớ)
    private Long timeTakenMs;     // Thời gian trả lời tính bằng mili giây
}