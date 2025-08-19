package com.example.memory_guard.guard.controller;

import com.example.memory_guard.guard.dto.GuardHomeResponseDto;
import com.example.memory_guard.guard.service.GuardService;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guard")
@RequiredArgsConstructor
public class GuardController {

  private final UserService userService;
  private final GuardService guardService;

  @GetMapping("/home")
  public ResponseEntity<GuardHomeResponseDto> getGuardHomePage(@AuthenticationPrincipal User guardian) {
    GuardHomeResponseDto response = guardService.getGuardHomeData(guardian);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/selectWard/{wardId}")
  public ResponseEntity<String> selectWard(@AuthenticationPrincipal User guardian, @PathVariable String wardId) {
    userService.selectWardForGuardian(guardian, wardId);
    return ResponseEntity.ok("동행자 선택이 변경되었습니다.");
  }
}