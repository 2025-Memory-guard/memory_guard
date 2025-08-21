package com.example.memory_guard.diary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WardCalendarResponseDto {
  private List<LocalDate> recordingDates;
  private List<LocalDate> speechDates;
}
