package com.example.memory_guard.guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.guard.dto.*;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.WardUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuardService {

    private final AudioMetadataRepository audioMetadataRepository;

    public GuardHomeResponseDto getHomeData(User user) {
        checkUser(user);

        //보호자 이름
        String guardianUserName = user.getUserProfile().getUsername();

        //ward
        User ward = user.getPrimaryWard();

        //weeklyStamp 구하기
        LocalDate today = LocalDate.now();
        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);

        List<AbstractAudioMetadata> weeklyRecordings = audioMetadataRepository.findByUserAndCreatedAtBetween(ward, startOfWeek, endOfWeek);

        List<LocalDate> weeklyStamps = weeklyRecordings.stream()
                .map(metadata -> metadata.getCreatedAt().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        //오늘 기록 구하기
        LocalDateTime startOfToday = today.atStartOfDay();         // 00:00:00
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);   // 23:59:59.999999999

        List<AbstractAudioMetadata> todayRecord = audioMetadataRepository.findByUserAndCreatedAtBetween(ward, startOfToday, endOfToday);

        return GuardHomeResponseDto.builder()
                .username(guardianUserName)
                .weeklyStamps(weeklyStamps)
                .consecutiveRecordingDays(ward.getConsecutiveRecordingDays())
                .wardUsername(ward.getUserProfile().getUsername())
                .todayRecord(todayRecord)
                .build();
    }

    public GuardReportResponseDto getReport(User user) {
        checkUser(user);

        User ward = user.getPrimaryWard();

        //이번 주 출석횟수 구하기
        LocalDate today = LocalDate.now();
        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);

        List<AbstractAudioMetadata> weeklyRecordings = audioMetadataRepository.findByUserAndCreatedAtBetween(ward, startOfWeek, endOfWeek);

        long weeklyAttendanceCount = weeklyRecordings.stream()
                .map(metadata -> metadata.getCreatedAt().toLocalDate())
                .distinct()
                .count();

        return GuardReportResponseDto.builder()
                .weeklyAttendanceCount(weeklyAttendanceCount)
                .correctionCount(0)
                .build();
    }

    public GuardCalendarResponseDto getCalendar(User user) {
        checkUser(user);

        User ward = user.getPrimaryWard();

        //이번 주 출석횟수 구하기
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

        List<AbstractAudioMetadata> monthlyRecordings = audioMetadataRepository.findByUserAndCreatedAtBetween(ward, startOfMonth, endOfMonth);

        long monthlyAttendanceCount = monthlyRecordings.stream()
                .map(metadata -> metadata.getCreatedAt().toLocalDate())
                .distinct()
                .count();

        List<LocalDate> monthlyAttendance = monthlyRecordings.stream()
                .map(metadata -> metadata.getCreatedAt().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return GuardCalendarResponseDto.builder()
                .monthlyAttendanceCount(monthlyAttendanceCount)
                .monthlyAttendance(monthlyAttendance)
                .build();
    }

    public GuardSettingResponseDto getSettings(User user) {
        checkUser(user);
        return GuardSettingResponseDto.fromEntity(user);
    }

    //현재 모든 피보호자 + 다른 피보호자에게 받은 요청 + 보호자가 보낸 요청 모두 보여주기
    public GuardManagementResponseDto getManagement(User user) {
        checkUser(user);
        return GuardManagementResponseDto.fromEntity(user.getWards(), user.getReceivedRequests(), user.getSentRequests());
    }



    private static void checkUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("user id가 비어있습니다");
        }

        boolean isGuardian = user.getRoles().stream().anyMatch(role -> "ROLE_GUARD".equals(role.getName()));

        if (!isGuardian) {
            throw new IllegalArgumentException("보호자 권한이 없습니다");
        }
    }
}
