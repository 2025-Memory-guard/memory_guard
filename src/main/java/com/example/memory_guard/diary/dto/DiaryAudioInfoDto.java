package com.example.memory_guard.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryAudioInfoDto {
    private Long audioId;
    private String title;
    private Long duration;
    private LocalDate createdAt;
}