package com.example.memory_guard.guard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class GuardCalendarResponseDto {
    private long monthlyAttendanceCount;
    private List<LocalDate> monthlyAttendance;
}
