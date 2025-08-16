package com.example.memory_guard.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryAudioInfoDto {
    private Long audioId;
    private String title;
    private Long duration;
}