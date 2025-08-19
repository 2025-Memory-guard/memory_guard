package com.example.memory_guard.setting.domain;

import com.example.memory_guard.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ConnectionRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 이후 시간있으면 LAZY로 수정
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestStatus status;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  public ConnectionRequest(User requester, User receiver) {
    this.requester = requester;
    this.receiver = receiver;
    this.status = RequestStatus.PENDING;
  }

  public void accept() {
    this.status = RequestStatus.ACCEPTED;
  }

  public void reject() {
    this.status = RequestStatus.REJECTED;
  }
}
