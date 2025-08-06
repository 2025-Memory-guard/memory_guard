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
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private UserProfile userProfile;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "guardian", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private List<GuardUserLink> wards = new ArrayList<>();

  @OneToOne(mappedBy = "ward", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private GuardUserLink guardian;

  @Builder
  public User(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  public void addRole(Role role) {
    this.roles.add(role);
  }

  public void addWard(User ward){
    GuardUserLink guardUserLink = new GuardUserLink(this, ward);
    this.wards.add(guardUserLink);
    ward.setGuardian(guardUserLink);
  }

  public void setGuardian(GuardUserLink guardUserLink) {
    this.guardian = guardUserLink;
  }

  public List<User> getWards(){
    return wards.stream()
        .map(GuardUserLink::getWard)
        .collect(Collectors.toList());
  }

  public User getGuardian(){
    return this.guardian != null ? guardian.getGuardian() : null;
  }
}