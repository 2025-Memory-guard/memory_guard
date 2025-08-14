package com.example.memory_guard.guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.dto.AudioStampResponseDto;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.guard.dto.GuardHomeResponseDto;
import com.example.memory_guard.user.domain.User;
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
        if (user.getId() == null) {
            throw new IllegalArgumentException("user id가 비어있습니다");
        }

        boolean isGuardian = user.getRoles().stream().anyMatch(role -> "ROLE_GUARD".equals(role.getName()));

        if (!isGuardian) {
            throw new IllegalArgumentException("보호자 권한이 없습니다");
        }

        //보호자 이름
        String guardianUserName = user.getUserProfile().getUsername();

        //ward
        User ward = user.getWard();

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

}
