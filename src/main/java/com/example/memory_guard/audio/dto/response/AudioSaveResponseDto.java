package com.example.memory_guard.audio.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class AudioSaveResponseDto {

  private Long audioId;
  private int consecutiveRecordingDays;
}
