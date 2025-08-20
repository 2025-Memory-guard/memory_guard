package com.example.memory_guard.diary.dto;

import com.example.memory_guard.diary.domain.Diary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DiaryInfoDto {
  private String title;
  private LocalDate createdAt;

  public static DiaryInfoDto fromEntity(Diary diary) {
    return DiaryInfoDto.builder()
        .title(diary.getTitle())
        .createdAt(diary.getCreatedAt().toLocalDate())
        .build();
  }
}