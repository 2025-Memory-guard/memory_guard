package com.example.memory_guard.user.service;

import com.example.memory_guard.global.exception.custom.AuthenticationException;
import com.example.memory_guard.global.exception.custom.InvalidRequestException;
import com.example.memory_guard.user.domain.UserProfile;
import com.example.memory_guard.user.dto.LoginResponseDto;
import com.example.memory_guard.user.dto.SignupRequestDto;
import com.example.memory_guard.user.dto.GuardSignupRequestDto;
import com.example.memory_guard.user.dto.WardHomeResponseDto;
import com.example.memory_guard.diary.dto.DiaryAudioInfoDto;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.audio.dto.response.AudioStampResponseDto;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import com.example.memory_guard.user.repository.UserRepository;
import com.example.memory_guard.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AudioService audioService;

  public void signup(SignupRequestDto signupRequest, String role) {
    isDupUser(signupRequest);

    User user = createUser(signupRequest);
    setRole(user, role);

    userRepository.save(user);
  }

//  public void guardSignup(GuardSignupRequestDto signupRequest) {
//    isDupUser(signupRequest);
//
//    User ward = isValidWardUser(signupRequest);
//    User guardian = createUser(signupRequest);
//
//    guardian.addWard(ward);
//    setRole(guardian, "ROLE_GUARD");
//    userRepository.save(guardian);
//  }

  public TokenDto login(String userId, String password) {

    User user = isExistUser(userId, password);

    List<GrantedAuthority> authorities = createGrantedAuthority(user);

    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserProfile().getUserId(), null, authorities);

    return jwtProvider.generateToken(authentication);
  }

  public List<String> getUserRoles(String userId) {
    User user = userRepository.findByUserProfileUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID입니다."));
    
    return user.getRoles().stream()
        .map(role -> role.getName())
        .collect(Collectors.toList());
  }

  public LoginResponseDto reissueAccessToken(String refreshToken) {
    if (!jwtProvider.validateToken(refreshToken)) {
      throw new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token 입니다.");
    }

    String userId = jwtProvider.getUserIdFromToken(refreshToken);

    User user = userRepository.findByUserProfileUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    String authorities = user.getRoles().stream()
        .map(role -> role.getName())
        .collect(Collectors.joining(","));

    String newAccessToken = jwtProvider.createAccessToken(user.getUserProfile().getUserId(), authorities);

    return LoginResponseDto.builder()
        .grantType("Bearer")
        .accessToken(newAccessToken)
        .build();
  }

  @Transactional(readOnly = true)
  public WardHomeResponseDto getWardHomeData(User user) {
    User persistentUser = userRepository.findById(user.getId())
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + user.getId()));

    AudioStampResponseDto audioStamps = audioService.getAudioStamps(persistentUser);

    List<DiaryAudioInfoDto> diaryList = persistentUser.getDiaries().stream()
        .map(this::convertToDiaryAudioInfoDto)
        .collect(Collectors.toList());

    return WardHomeResponseDto.builder()
        .consecutiveRecordingDays(audioStamps.getConsecutiveRecordingDays())
        .weeklyStamps(audioStamps.getWeeklyStamps())
        .diaryList(diaryList)
        .build();
  }

  private DiaryAudioInfoDto convertToDiaryAudioInfoDto(Diary diary) {
    return DiaryAudioInfoDto.builder()
        .audioId(diary.getAudioMetadata().getId())
        .title(diary.getTitle())
        .duration(diary.getAudioMetadata().getDuration())
        .build();
  }

  private User isExistUser(String userId, String password) {
    User user = userRepository.findByUserProfileUserId(userId)
        .orElseThrow(() -> new AuthenticationException("존재하지 않는 사용자 ID입니다."));

    if (!passwordEncoder.matches(password, user.getUserProfile().getPassword())) {
      throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }
    return user;
  }

  private User isValidWardUser(GuardSignupRequestDto signupRequest) {
    User ward = userRepository.findByUserProfileUserId(signupRequest.getWardUserId())
        .orElseThrow(() -> new InvalidRequestException("존재하지 않는 피보호자입니다."));

    boolean hasUserRole = ward.getRoles().stream()
        .anyMatch(role -> "ROLE_USER".equals(role.getName()));

    if (!hasUserRole) {
      throw new InvalidRequestException("해당 사용자는 피보호자 권한이 없습니다.");
    }
    return ward;
  }

  private void setRole(User user, String role) {
    Role userRole  = roleRepository.findByName(role)
        .orElseThrow(() -> new IllegalStateException(role + "가 존재하지 않습니다."));

    user.addRole(userRole);
  }

  private User createUser(SignupRequestDto signupRequest) {
    UserProfile userProfile = UserProfile.builder()
        .userId(signupRequest.getUserId())
        .password(passwordEncoder.encode(signupRequest.getPassword()))
        .username(signupRequest.getUsername())
        .build();

    return User.builder().userProfile(userProfile).build();
  }

  private static List<GrantedAuthority> createGrantedAuthority(User user) {
    List<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toList());
    return authorities;
  }

  private void isDupUser(SignupRequestDto signupRequest) {
    if (userRepository.findByUserProfileUserId(signupRequest.getUserId()).isPresent()) {
      throw new InvalidRequestException("이미 존재하는 사용자 ID입니다.");
    }

    if (userRepository.existsByUserProfileUsername(signupRequest.getUsername())) {
      throw new InvalidRequestException("이미 존재하는 사용자명입니다.");
    }
  }
}