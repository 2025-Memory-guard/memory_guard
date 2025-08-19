package com.example.memory_guard.setting.service;

import com.example.memory_guard.global.exception.custom.InvalidRequestException;
import com.example.memory_guard.setting.domain.ConnectionRequest;
import com.example.memory_guard.setting.domain.RequestStatus;
import com.example.memory_guard.setting.repository.ConnectionRequestRepository;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConnectionService {

  private final UserRepository userRepository;
  private final ConnectionRequestRepository connectionRequestRepository;
  // private final NotificationService notificationService; 지영님이 만드신 서비스

  @Transactional
  public void requestConnectionByGuardian(User guardian, String wardUserId) {
    User managedGuardian = userRepository.findById(guardian.getId())
        .orElseThrow(() -> new UsernameNotFoundException("보호자 정보를 찾을 수 없습니다."));

    User ward = userRepository.findByUserProfileUserId(wardUserId)
        .orElseThrow(() -> new InvalidRequestException("존재하지 않는 피보호자 ID입니다."));

    if (guardian.getId().equals(ward.getId())) {
      throw new InvalidRequestException("자기 자신에게 연결 요청을 보낼 수 없습니다.");
    }

    boolean alreadyConnected = ward.getGuardians().stream()
        .anyMatch(g -> g.getId().equals(managedGuardian.getId()));

    if (alreadyConnected) {
      throw new InvalidRequestException("이미 회원님과 연결된 피보호자입니다.");
    }

    if (connectionRequestRepository.existsByRequesterAndReceiverAndStatus(guardian, ward, RequestStatus.PENDING)) {
      throw new InvalidRequestException("이미 보낸 요청이 처리 대기 중입니다.");
    }

    ConnectionRequest request = new ConnectionRequest(guardian, ward);
    connectionRequestRepository.save(request);

    String title = "보호자 연결 요청";
    String body = guardian.getUserProfile().getUsername() + " 님이 보호자 연결을 요청했습니다.";
    //notificationService.sendNotification(ward, title, body); 알림 기능
  }

  @Transactional
  public void requestConnectionByWard(User ward, String guardianUserId) {
    User managedWard = userRepository.findById(ward.getId())
        .orElseThrow(() -> new UsernameNotFoundException("피보호자 정보를 찾을 수 없습니다."));

    User guardian = userRepository.findByUserProfileUserId(guardianUserId)
        .orElseThrow(() -> new InvalidRequestException("존재하지 않는 보호자 ID입니다."));


    if (ward.getId().equals(guardian.getId())) {
      throw new InvalidRequestException("자기 자신에게 연결 요청을 보낼 수 없습니다.");
    }

    boolean alreadyConnected = managedWard.getGuardians().stream()
        .anyMatch(g -> g.getId().equals(guardian.getId()));

    if (alreadyConnected) {
      throw new InvalidRequestException("이미 연결된 보호자입니다.");
    }

    if (connectionRequestRepository.existsByRequesterAndReceiverAndStatus(ward, guardian, RequestStatus.PENDING)) {
      throw new InvalidRequestException("이미 보낸 요청이 처리 대기 중입니다.");
    }
    if (!guardian.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_GUARD"))) {
      throw new InvalidRequestException("해당 사용자는 보호자가 아닙니다.");
    }

    ConnectionRequest request = new ConnectionRequest(ward, guardian);
    connectionRequestRepository.save(request);

    String title = "피보호자 연결 요청";
    String body = ward.getUserProfile().getUsername() + " 님이 피보호자 연결을 요청했습니다.";
    // notificationService.sendNotification(guardian, title, body); 알림기능
  }

  @Transactional
  public void acceptConnectionRequest(User receiver, Long requestId) {
    ConnectionRequest request = connectionRequestRepository.findById(requestId)
        .orElseThrow(() -> new InvalidRequestException("존재하지 않는 요청입니다."));

    if (!request.getReceiver().getId().equals(receiver.getId())) {
      throw new InvalidRequestException("요청을 수락할 권한이 없습니다.");
    }
    if (request.getStatus() != RequestStatus.PENDING) {
      throw new InvalidRequestException("이미 처리된 요청입니다.");
    }

    User managedRequester = request.getRequester();
    User managedReceiver = request.getReceiver();

    User guardian, ward;
    if (managedRequester.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_GUARD"))) {
      guardian = managedRequester;
      ward = managedReceiver;
    } else {
      guardian = managedReceiver;
      ward = managedRequester;
    }

    guardian.addWard(ward);
    request.accept();

    String title = "연결 요청 수락";
    String body = receiver.getUserProfile().getUsername() + " 님이 연결 요청을 수락했습니다.";
    // notificationService.sendNotification(requester, title, body); 알림 기능
  }

  @Transactional
  public void rejectConnectionRequest(User receiver, Long requestId) {
    ConnectionRequest request = connectionRequestRepository.findById(requestId)
        .orElseThrow(() -> new InvalidRequestException("존재하지 않는 요청입니다."));

    if (!request.getReceiver().getId().equals(receiver.getId())) {
      throw new InvalidRequestException("요청을 거절할 권한이 없습니다.");
    }

    if (request.getStatus() != RequestStatus.PENDING) {
      throw new InvalidRequestException("이미 처리된 요청입니다.");
    }

    request.reject();

    User requester = request.getRequester();
    String title = "연결 요청 거절";
    String body = receiver.getUserProfile().getUsername() + " 님이 연결 요청을 거절했습니다.";
    //notificationService.sendNotification(requester, title, body);
  }
}