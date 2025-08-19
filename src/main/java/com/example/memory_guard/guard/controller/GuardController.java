package com.example.memory_guard.guard.controller;

import com.example.memory_guard.guard.dto.*;
import com.example.memory_guard.guard.service.GuardService;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guard")
public class GuardController {

    private final GuardService guardService;

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

    @GetMapping("/calendar")
    public ResponseEntity<GuardCalendarResponseDto> getCalendar(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(guardService.getCalendar(user));
    }

    @GetMapping("/settings")
    public ResponseEntity<GuardSettingResponseDto> getSettings(
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

//    @GetMapping("/search-wards")
//    public ResponseEntity<User> getWard(
//            @RequestParam String userId
//    ) {
//        return ResponseEntity.ok(guardService.getWard(userId));
//    }
}
