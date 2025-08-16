package com.example.memory_guard.analysis.dto;

import com.example.memory_guard.analysis.domain.FinalFeedback;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FinalFeedbackResponseDto {
  private List<FinalFeedbackItemDto> finalFeedbacks;

  @Getter
  @Builder
  public static class FinalFeedbackItemDto {
    private String title;
    private String content;

    public static FinalFeedbackItemDto from(FinalFeedback finalFeedback) {
      return FinalFeedbackItemDto.builder()
          .title(finalFeedback.getTitle())
          .content(finalFeedback.getContent())
          .build();
    }
  }

  public static FinalFeedbackResponseDto from(List<FinalFeedback> finalFeedbacks) {
    List<FinalFeedbackItemDto> feedbackItems = finalFeedbacks.stream()
        .map(FinalFeedbackItemDto::from)
        .toList();

    return FinalFeedbackResponseDto.builder()
        .finalFeedbacks(feedbackItems)
        .build();
  }
}