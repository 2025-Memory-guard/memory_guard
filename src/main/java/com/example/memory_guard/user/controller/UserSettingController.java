package com.example.memory_guard.user.controller;

import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardUserDto;
import com.example.memory_guard.user.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/setting")
public class UserSettingController {

    private final UserSettingService userSettingService;

    @GetMapping("/all-guards")
    public ResponseEntity<List<GuardUserDto>> getAllGuards(
            @AuthenticationPrincipal User ward
    ) {
        return ResponseEntity.ok(userSettingService.getAllGuards(ward));
    }

    @GetMapping("/management")
    public ResponseEntity<Ward>
}
