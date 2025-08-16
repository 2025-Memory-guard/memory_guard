package com.example.memory_guard.guard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GuardReportResponseDto {
    //희승님이 보내신 ai data

    private long weeklyAttendanceCount;
    //교정 횟수(아직 api 없음)
    private long correctionCount;

    @Builder
    public GuardReportResponseDto(long weeklyAttendanceCount, long correctionCount) {
        this.weeklyAttendanceCount = weeklyAttendanceCount;
        this.correctionCount = correctionCount;
    }
}
