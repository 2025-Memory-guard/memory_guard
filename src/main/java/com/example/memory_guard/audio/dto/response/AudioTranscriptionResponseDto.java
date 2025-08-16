package com.example.memory_guard.audio.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AudioTranscriptionResponseDto {

  private Long audioId;
  private String audioText;

  @Builder
  public AudioTranscriptionResponseDto(Long audioId, String audioText){
    this.audioId = audioId;
    this.audioText = audioText;
  }
}
