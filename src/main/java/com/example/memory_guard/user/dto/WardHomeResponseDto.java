package com.example.memory_guard.user.dto;

import com.example.memory_guard.diary.dto.DiaryAudioInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardHomeResponseDto {
    private int consecutiveRecordingDays;
    private List<LocalDate> weeklyStamps;
    private List<DiaryAudioInfoDto> diaryList;
}