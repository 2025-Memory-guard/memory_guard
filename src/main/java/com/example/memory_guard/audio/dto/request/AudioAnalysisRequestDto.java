package com.example.memory_guard.audio.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudioAnalysisRequestDto {

  private String audioData;
  private String filename;

}
