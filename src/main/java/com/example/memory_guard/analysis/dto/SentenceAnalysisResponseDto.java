package com.example.memory_guard.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceAnalysisResponseDto {
    private String audioText;
    private SentenceAnalysisIndicatorsDto feedbacks;
}
