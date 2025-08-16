package com.example.memory_guard.guard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class GuardCalendarResponseDto {
    private long monthlyAttendanceCount;
    private List<LocalDate> monthlyAttendance;

    @Builder
    public GuardCalendarResponseDto(long monthlyAttendanceCount, List<LocalDate> monthlyAttendance) {
        this.monthlyAttendanceCount = monthlyAttendanceCount;
        this.monthlyAttendance = monthlyAttendance;
    }
}
