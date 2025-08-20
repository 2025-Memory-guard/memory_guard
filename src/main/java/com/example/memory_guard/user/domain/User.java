package com.example.memory_guard.user.domain;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.diary.domain.Diary;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter  // 테스트를 위해 임시 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private UserProfile userProfile;

  private int consecutiveRecordingDays = 0;

  private LocalDate lastRecordingDate;

  private double avgRecordingTime = 0.0;

  private double avgScore = 0.0;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "guardian", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private List<GuardUserLink> wards = new ArrayList<>();

  // OneToOne -> OneToMany 로 변경
  @OneToMany(mappedBy = "ward", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private List<GuardUserLink> guardians = new ArrayList<>();

  //추가
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "primary_ward_id")
  private User primaryWard;

  //추가
  @OneToMany(mappedBy = "requester")
  private List<GuardRequest> sentRequests = new ArrayList<>();

  //추가
  @OneToMany(mappedBy = "receiver")
  private List<GuardRequest> receivedRequests = new ArrayList<>();


  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AbstractAudioMetadata> audioMetadataList = new ArrayList<>();

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Diary> diaries = new ArrayList<>();

  @Builder
  public User(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  public void addRole(Role role) {
    this.roles.add(role);
  }

  public GuardUserLink addWard(User ward){
    boolean isFirstWard = this.getWards().isEmpty();

    GuardUserLink guardUserLink = new GuardUserLink(this, ward);
    this.wards.add(guardUserLink);
    ward.getGuardians().add(guardUserLink);

    if (isFirstWard) {
      this.setPrimaryWard(ward);
    }

    return guardUserLink;
  }

  public GuardUserLink addGuardian(User guard) {
    GuardUserLink guardUserLink = new GuardUserLink(guard, this);
    this.guardians.add(guardUserLink);
    guard.addWard(guardUserLink.getWard());
    return guardUserLink;
  }

  public void setPrimaryWard(User ward) {
    boolean exists = wards.stream().anyMatch(link -> link.getWard().equals(ward));
    if (!exists) {
      throw new IllegalArgumentException("이 피보호자는 연결되어 있지 않습니다.");
    }
    this.primaryWard = ward;
  }

  public void updateRecordingStreak() {
    LocalDate today = LocalDate.now();

    if (today.equals(this.lastRecordingDate)) {
      return;
    }

    if (today.minusDays(1).equals(this.lastRecordingDate)) {
      this.consecutiveRecordingDays++;
    } else {
      this.consecutiveRecordingDays = 1;
    }

    this.lastRecordingDate = today;
  }

  public void addAudioMetadata(AbstractAudioMetadata audioMetadata) {
    this.audioMetadataList.add(audioMetadata);
  }

  public void updateAvgRecordingTime(int newRecordingTimeSeconds) {
    int totalRecordings = this.audioMetadataList.size();
    if (totalRecordings == 1) {
      this.avgRecordingTime = newRecordingTimeSeconds;
    } else {
      this.avgRecordingTime = ((this.avgRecordingTime * (totalRecordings - 1)) + newRecordingTimeSeconds) / totalRecordings;
    }
  }

  public void updateAvgScore(double newScore) {
    int totalEvaluations = this.audioMetadataList.size();
    if (totalEvaluations == 1) {
      this.avgScore = newScore;
    } else {
      this.avgScore = ((this.avgScore * (totalEvaluations - 1)) + newScore) / totalEvaluations;
    }
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return this.userProfile.getPassword();
  }

  @Override
  public String getUsername() {
    return this.userProfile.getUserId();
  }
}