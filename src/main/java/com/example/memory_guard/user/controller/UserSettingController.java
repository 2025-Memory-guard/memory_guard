package com.example.memory_guard.user.controller;

import com.example.memory_guard.user.domain.Status;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardManagementResponseDto;
import com.example.memory_guard.user.dto.GuardRequestDto;
import com.example.memory_guard.user.dto.GuardUserDto;
import com.example.memory_guard.user.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserSettingController {

    private final UserSettingService userSettingService;

    @GetMapping("/setting")
    public ResponseEntity<List<GuardUserDto>> getAllGuards(
            @AuthenticationPrincipal User ward
    ) {
        return ResponseEntity.ok(userSettingService.getAllGuards(ward));
    }

    @GetMapping("/management")
    public ResponseEntity<GuardManagementResponseDto> getManagement(
            @AuthenticationPrincipal User ward
    ) {
        return ResponseEntity.ok(userSettingService.getManagement(ward));
    }

    @GetMapping("/search-guard")
    public ResponseEntity<Optional<GuardUserDto>> getWard(
            @RequestParam String userId
    ) {
        return ResponseEntity.ok(userSettingService.getGuard(userId));
    }

    @PostMapping("/enroll-guard")
    public ResponseEntity<?> addWard(
            @AuthenticationPrincipal User user,
            @RequestBody GuardRequestDto guardRequestDto
    ) {
        userSettingService.sendGuardRequest(user, guardRequestDto);
        return ResponseEntity.ok("보호자에게 요청이 전송되었습니다.");
    }

    //보호자가 보낸 연결 요청 수락
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long requestId,
            @RequestBody Status status
    ) {
        userSettingService.updateRequestStatus(requestId, status);
        return ResponseEntity.ok("OK");
    }
}
