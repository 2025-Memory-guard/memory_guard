package com.example.memory_guard.audio.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpeakSentenceRequestDto {

  private String audioData;
  private String filename;
  private String sentence;

}