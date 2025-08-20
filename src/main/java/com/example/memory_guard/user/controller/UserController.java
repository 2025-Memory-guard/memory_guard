package com.example.memory_guard.user.controller;

import com.example.memory_guard.user.dto.*;
import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.user.service.UserService;
import com.example.memory_guard.user.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Value ("${jwt.refresh-token-expiration-seconds}")
  private long refreshTokenValiditySeconds;

  @PostMapping("/ward/signup")
  public ResponseEntity<String> signup(@RequestBody SignupRequestDto signupDto) {
      userService.signup(signupDto, "ROLE_USER");
      return ResponseEntity.ok("회원가입이 완료되었습니다.");
  }

  @PostMapping("/guard/signup")
  public ResponseEntity<String> guardSignup(@RequestBody SignupRequestDto signupDto) {
      userService.signup(signupDto, "ROLE_GUARD");
      return ResponseEntity.ok("보호자 회원가입이 완료되었습니다.");
  }

  @PostMapping("/user/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDto, HttpServletResponse response) {
    String userId = loginDto.getUserId();
    String password = loginDto.getPassword();

    TokenDto tokenInfo = userService.login(userId, password);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenInfo.getRefreshToken())
        .maxAge(refreshTokenValiditySeconds)
        .path("/")
        .secure(false)
        .httpOnly(true)
        .sameSite("Lax")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    LoginResponseDto loginResponseDto = LoginResponseDto.builder()
        .grantType(tokenInfo.getGrantType())
        .accessToken(tokenInfo.getAccessToken())
        .userId(userId)
        .roles(userService.getUserRoles(userId))
        .build();

    return ResponseEntity.ok(loginResponseDto);
  }

  @PostMapping("/token/reissue")
  public ResponseEntity<?> reissue(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
    if (refreshToken == null) {
      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .body("Refresh Token이 없습니다.");
    }

    try {
      LoginResponseDto newAccessToken = userService.reissueAccessToken(refreshToken);
      return ResponseEntity.ok(newAccessToken);
    } catch (IllegalArgumentException e) {
      ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
          .maxAge(0)
          .path("/")
          .build();

      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body("유효하지 않은 Refresh Token 입니다. 다시 로그인해주세요.");
    }
  }

  @GetMapping("/api/ward/home")
  public ResponseEntity<WardHomeResponseDto> wardHome(@AuthenticationPrincipal User user) {
    WardHomeResponseDto response = userService.getWardHomeData(user);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/exists")
  public ResponseEntity<UserExistResponseDto> existUser(@PathVariable String userId){
    boolean exist = userService.existUserId(userId);
    return ResponseEntity.ok(new UserExistResponseDto(exist));
  }

  @GetMapping("/home/user")
  public String testUser(){
    return "User";
  }

  @GetMapping("/home/guard")
  public String testGuard(){
    return "Guard";
  }
}



