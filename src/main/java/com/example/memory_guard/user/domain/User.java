package com.example.memory_guard.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String userId;

  @Column(nullable = false)
  private String password;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "guardian_id")
  private User guardian;

  @OneToMany(mappedBy = "guardian")
  private List<User> wards = new ArrayList<>();

  @Builder
  public User(String userId, String password) {
    this.userId = userId;
    this.password = password;
  }

  public void addRole(Role role) {
    this.roles.add(role);
  }

  public void addWard(User ward){
    this.wards.add(ward);
    ward.setGuardian(this);
  }

  public void setGuardian(User guardian) {this.guardian = guardian;}
}