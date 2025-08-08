package com.example.memory_guard.diary.dto;

import com.example.memory_guard.diary.domain.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DiaryResponseDto {
  private String title;
  private String body;
  private String authorName;
  private LocalDate writtenAt;

  @Builder
  public DiaryResponseDto(String title, String body, String authorName, LocalDate writtenAt){
    this.title = title;
    this.body =body;
    this.authorName = authorName;
    this.writtenAt = writtenAt;
  }

}