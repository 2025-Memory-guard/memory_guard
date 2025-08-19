package com.example.memory_guard.guard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GuardHomeResponseDto {
  private String selectedWardName;
  private int consecutiveRecordingDays;
  private List<LocalDate> weeklyStamps;
  private List<DiaryInfo> diaryList;

  @Getter
  @Builder
  public static class DiaryInfo {
    private String title;
    private LocalDate date;
  }
}