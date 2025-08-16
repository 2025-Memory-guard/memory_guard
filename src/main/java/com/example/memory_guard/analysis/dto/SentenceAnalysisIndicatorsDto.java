package com.example.memory_guard.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceAnalysisIndicatorsDto {
  @JsonProperty("linguistic_feedback")
  private List<FeedbackItem> linguisticFeedback;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class FeedbackItem {
    @JsonProperty("category")
    private String category;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("example_original")
    private String exampleOriginal;

    @JsonProperty("example_suggestion")
    private String exampleSuggestion;
  }
}