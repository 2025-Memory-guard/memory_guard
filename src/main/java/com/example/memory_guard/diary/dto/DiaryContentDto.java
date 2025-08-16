package com.example.memory_guard.diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiaryContentDto {

  @JsonProperty("title")
  private String title;

  @JsonProperty("body")
  private String body;
}