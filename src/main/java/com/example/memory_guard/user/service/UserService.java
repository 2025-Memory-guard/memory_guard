package com.example.memory_guard.user.service;

import com.example.memory_guard.user.dto.LoginResponseDto;
import com.example.memory_guard.user.dto.SignupRequestDto;
import com.example.memory_guard.user.dto.GuardSignupRequestDto;
import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import com.example.memory_guard.user.domain.repository.UserRepository;
import com.example.memory_guard.user.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

  public void signup(SignupRequestDto signupRequest) {
    if (userRepository.findByUserId(signupRequest.getUserId()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
    }

    if (userRepository.existsByUsername(signupRequest.getUsername())) {
      throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
    }

    Role userRole = roleRepository.findByName("ROLE_USER")
        .orElseThrow(() -> new IllegalStateException("ROLE_USER가 존재하지 않습니다."));

    User user = User.builder()
        .userId(signupRequest.getUserId())
        .username(signupRequest.getUsername())
        .password(passwordEncoder.encode(signupRequest.getPassword()))
        .build();

    user.addRole(userRole);
    userRepository.save(user);
  }

  public void guardSignup(GuardSignupRequestDto signupRequest) {
    if (userRepository.findByUserId(signupRequest.getUserId()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
    }

    if (userRepository.existsByUsername(signupRequest.getUsername())) {
      throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
    }

    User ward = userRepository.findByUserId(signupRequest.getWardUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피보호자입니다."));

    boolean hasUserRole = ward.getRoles().stream()
        .anyMatch(role -> "ROLE_USER".equals(role.getName()));
    
    if (!hasUserRole) {
      throw new IllegalArgumentException("해당 사용자는 피보호자 권한이 없습니다.");
    }

    Role guardRole = roleRepository.findByName("ROLE_GUARD")
        .orElseThrow(() -> new IllegalStateException("ROLE_GUARD가 존재하지 않습니다."));

    User guardian = User.builder()
        .userId(signupRequest.getUserId())
        .username(signupRequest.getUsername())
        .password(passwordEncoder.encode(signupRequest.getPassword()))
        .build();

    guardian.addWard(ward);
    guardian.addRole(guardRole);
    userRepository.save(guardian);
  }

  public TokenDto login(String userId, String password) {

    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID입니다."));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    List<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toList());

    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserId(), null, authorities);

    return jwtProvider.generateToken(authentication);
  }

  public List<String> getUserRoles(String userId) {
    User user = userRepository.findByUserId(userId)
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

    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    String authorities = user.getRoles().stream()
        .map(role -> role.getName())
        .collect(Collectors.joining(","));

    String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), authorities);

    return LoginResponseDto.builder()
        .grantType("Bearer")
        .accessToken(newAccessToken)
        .build();
  }
}