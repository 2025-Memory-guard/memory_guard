package com.example.memory_guard.audio.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class AudioStampResponseDto {
  private final int consecutiveRecordingDays;
  private final List<LocalDate> weeklyStamps;

  @Builder
  public AudioStampResponseDto(int consecutiveRecordingDays, List<LocalDate> weeklyStamps) {
    this.consecutiveRecordingDays = consecutiveRecordingDays;
    this.weeklyStamps = weeklyStamps;
  }
}
