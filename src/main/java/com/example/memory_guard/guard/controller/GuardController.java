package com.example.memory_guard.guard.controller;

import com.example.memory_guard.guard.dto.GuardHomeResponseDto;
import com.example.memory_guard.guard.service.GuardService;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GuardController {

    private final GuardService guardService;

    public ResponseEntity<GuardHomeResponseDto> getHomeData(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(guardService.getHomeData(user));
    }

}
