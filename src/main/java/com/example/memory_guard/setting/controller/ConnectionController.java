package com.example.memory_guard.setting.controller;

import com.example.memory_guard.setting.service.ConnectionService;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

  private final ConnectionService connectionService;

  @PostMapping("/request/guardian-to-ward/{wardUserId}")
  public ResponseEntity<String> requestByGuardian(
      @AuthenticationPrincipal User guardian,
      @PathVariable String wardUserId) {
    connectionService.requestConnectionByGuardian(guardian, wardUserId);
    return ResponseEntity.ok("피보호자에게 연결 요청을 보냈습니다.");
  }

  @PostMapping("/request/ward-to-guardian/{guardianUserId}")
  public ResponseEntity<String> requestByWard(
      @AuthenticationPrincipal User ward,
      @PathVariable String guardianUserId) {
    connectionService.requestConnectionByWard(ward, guardianUserId);
    return ResponseEntity.ok("보호자에게 연결 요청을 보냈습니다.");
  }

  @PostMapping("/accept/{requestId}")
  public ResponseEntity<String> acceptRequest(
      @AuthenticationPrincipal User user,
      @PathVariable Long requestId) {
    connectionService.acceptConnectionRequest(user, requestId);
    return ResponseEntity.ok("연결 요청을 수락했습니다.");
  }

  @PostMapping("/reject/{requestId}")
  public ResponseEntity<String> rejectRequest(
      @AuthenticationPrincipal User user,
      @PathVariable Long requestId) {
    connectionService.rejectConnectionRequest(user, requestId);
    return ResponseEntity.ok("연결 요청을 거절했습니다.");
  }
}