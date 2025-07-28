package com.example.memory_guard.user.service;

import com.example.memory_guard.user.dto.LoginResponseDto;
import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import com.example.memory_guard.user.domain.repository.UserRepository;
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
  private final PasswordEncoder passwordEncoder;

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