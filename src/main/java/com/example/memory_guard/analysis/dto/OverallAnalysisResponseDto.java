package com.example.memory_guard.analysis.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class OverallAnalysisResponseDto {

  private double score;
  private double speakingRate;
  private double utteranceVolume;
  private double avgSilenceDuration;
  private double vocabularyAccuracy;
  private double fillerFrequency;
  private double repetitionRatio;
}
