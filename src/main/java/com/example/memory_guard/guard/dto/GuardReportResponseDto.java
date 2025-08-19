package com.example.memory_guard.guard.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GuardReportResponseDto {
    //희승님이 보내신 ai data

    private long weeklyAttendanceCount;
    //교정 횟수(아직 api 없음)
    private long correctionCount;
}
