package com.example.memory_guard.guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.guard.dto.*;
import com.example.memory_guard.user.domain.GuardRequest;
import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.Status;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardRequestDto;
import com.example.memory_guard.user.dto.WardUserDto;
import com.example.memory_guard.user.repository.GuardRequestRepository;
import com.example.memory_guard.user.repository.GuardUserLinkRepository;
import com.example.memory_guard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardService {

    private final AudioMetadataRepository audioMetadataRepository;
    private final UserRepository userRepository;
    private final GuardRequestRepository guardRequestRepository;
    private final GuardUserLinkRepository guardUserLinkRepository;

    public GuardHomeResponseDto getHomeData(User user) {
        User persistUser = userRepository
            .findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        checkUser(persistUser);

        //보호자 이름
        String guardianUserName = persistUser.getUserProfile().getUsername();

        //ward
        User ward = persistUser.getPrimaryWard();

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
        User persistUser = userRepository
            .findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        checkUser(persistUser);

        User ward = persistUser.getPrimaryWard();

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
        User persistUser = userRepository
            .findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        checkUser(persistUser);

        User ward = persistUser.getPrimaryWard();

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

    public List<GuardSettingResponseDto> getSettings(User user) {
        User persistUser = userRepository
            .findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        checkUser(persistUser);

        return persistUser.getWards().stream()
            .map(link -> GuardSettingResponseDto.fromEntity(link, persistUser))
            .collect(Collectors.toList());
    }

    //현재 모든 피보호자 + 다른 피보호자에게 받은 요청 + 보호자가 보낸 요청 모두 보여주기
    public GuardManagementResponseDto getManagement(User user) {
        User persistUser = userRepository
            .findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        checkUser(persistUser);
        return GuardManagementResponseDto.fromEntity(persistUser.getWards(), persistUser.getReceivedRequests(), persistUser.getSentRequests());
    }

    //피보호자 아이디로 검색
    public Optional<WardUserDto> getWard(String userId) {
        return userRepository.findByUserProfileUserId(userId)
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_USER")))
                .map(ward -> WardUserDto.fromEntity(ward, false));
    }

    public void sendGuardRequest(User guard, GuardRequestDto guardRequestDto) {
        log.info("guardRequestDto: {}", guardRequestDto);
        User persistGuard = userRepository
            .findById(guard.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        checkUser(persistGuard);
        User persistWard = userRepository.findUserById(guardRequestDto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("요청 대상 피보호자를 찾을 수 없습니다."));

        GuardRequest guardRequest = GuardRequestDto.toEntity(persistGuard, persistWard);
        persistWard.getReceivedRequests().add(guardRequest);
        persistGuard.getSentRequests().add(guardRequest);

        guardRequestRepository.save(guardRequest);
    }

    public void updateRequestStatus(Long requestId, Status status) {
        GuardRequest request = guardRequestRepository.findGuardRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수가 없습니다"));

        if (request.getStatus() != Status.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        User guard = request.getReceiver();
        User ward = request.getRequester();

        //요청이 거절되었을 떄
        if (status == Status.REJECTED) {
            guard.getReceivedRequests().remove(request);
            ward.getSentRequests().remove(request);
            guardRequestRepository.delete(request);
        }

        //요청 수락되었을 떄
        if (status == Status.ACCEPTED) {
            GuardUserLink guardUserLink = ward.addGuardian(guard);
            guardUserLinkRepository.save(guardUserLink);

            ward.getReceivedRequests().remove(request);
            guard.getSentRequests().remove(request);
            guardRequestRepository.delete(request);
        }
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
