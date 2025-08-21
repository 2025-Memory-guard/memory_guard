package com.example.memory_guard.guard.controller;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.dto.response.AudioAnalysisReport;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.diary.dto.WardCalendarResponseDto;
import com.example.memory_guard.guard.dto.*;
import com.example.memory_guard.guard.service.GuardService;
import com.example.memory_guard.user.domain.Status;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardRequestDto;
import com.example.memory_guard.user.dto.WardUserDto;
import com.example.memory_guard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guard")
public class GuardController {

    private final GuardService guardService;
    private final AudioService audioService;
    private final UserService userService;

    @GetMapping("/home")
    public ResponseEntity<GuardHomeResponseDto> getHomeData(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(guardService.getHomeData(user));
    }

    @GetMapping("/weekly-report")
    public ResponseEntity<GuardReportResponseDto> getReport(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(guardService.getReport(user));
    }

//    @GetMapping("/calendar")
//    public ResponseEntity<GuardCalendarResponseDto> getCalendar(
//            @AuthenticationPrincipal User user
//    ) {
//        return ResponseEntity.ok(guardService.getCalendar(user));
//    }

    @GetMapping("/setting")
    public ResponseEntity<List<GuardSettingResponseDto>> getSettings(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(guardService.getSettings(user));
    }

    @GetMapping("/management")
    public ResponseEntity<GuardManagementResponseDto> getAllWards(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(guardService.getManagement(user));
    }

    @GetMapping("/search-ward")
    public ResponseEntity<Optional<WardUserDto>> getWard(
            @RequestParam String userId
    ) {
        return ResponseEntity.ok(guardService.getWard(userId));
    }

    @PostMapping("/enroll-wards")
    public ResponseEntity<?> addWard(
            @AuthenticationPrincipal User user,
            @RequestBody GuardRequestDto guardRequestDto
    ) {
        guardService.sendGuardRequest(user, guardRequestDto);
        return ResponseEntity.ok("피보호자에게 요청이 전송되었습니다.");
    }


    //비보호자가 보낸 연결 요청 수락
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long requestId,
            @RequestBody Status status
    ) {
        guardService.updateRequestStatus(requestId, status);
        return ResponseEntity.ok("OK");
    }

    @PatchMapping("/selectWard/{wardId}")
    public ResponseEntity<String> selectWard(@AuthenticationPrincipal User guardian, @PathVariable String wardId) {
        userService.selectWardForGuardian(guardian, wardId);
        return ResponseEntity.ok("동행자 선택이 변경되었습니다.");
    }

    @GetMapping("/report")
    public ResponseEntity<AudioAnalysisReport> audioReport(@AuthenticationPrincipal User guardian) throws IOException {

        User ward = guardian.getPrimaryWard();
        if (ward == null) {
            throw new IllegalStateException("주 피보호자가 설정되어 있지 않습니다.");
        }

        AbstractAudioMetadata latestMetadata = audioService.getLatestAudioMetadata(ward);

        audioService.audioEvaluate(latestMetadata, ward);

        AudioAnalysisReport report = audioService.audioEvaluateWardReport(latestMetadata, ward);

        return ResponseEntity.ok(report);
    }
}
