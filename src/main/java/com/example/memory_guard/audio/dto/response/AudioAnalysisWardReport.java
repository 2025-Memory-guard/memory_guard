package com.example.memory_guard.audio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioAnalysisWardReport {
    private double speakingRate;
    private double utteranceVolume;
    private double avgSilenceDuration;
    private double vocabularyAccuracy;
    private double avgRecordingTime;
    private int attendanceRate;
    private double avgScore;
}