package com.example.memory_guard.guard.dto;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class GuardHomeResponseDto {

    private String username;
    private List<LocalDate> weeklyStamps;
    private int consecutiveRecordingDays;
    private String wardUsername;
    private List<AbstractAudioMetadata> todayRecord;

    @Builder
    public GuardHomeResponseDto(String username, List<LocalDate> weeklyStamps, int consecutiveRecordingDays, String wardUsername, List<AbstractAudioMetadata> todayRecord) {
        this.username = username;
        this.weeklyStamps = weeklyStamps;
        this.consecutiveRecordingDays = consecutiveRecordingDays;
        this.wardUsername = wardUsername;
        this.todayRecord = todayRecord;
    }
}
