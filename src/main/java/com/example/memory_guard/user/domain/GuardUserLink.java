package com.example.memory_guard.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "guard_user_link")
public class GuardUserLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guardian_id", nullable = false)
  private User guardian;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ward_id", nullable = false)
  private User ward;

  public GuardUserLink(User guardian, User ward) {
    this.guardian = guardian;
    this.ward = ward;
  }
}
