package com.example.memory_guard.service;

import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.global.exception.custom.AuthenticationException;
import com.example.memory_guard.global.exception.custom.InvalidRequestException;
import com.example.memory_guard.user.dto.GuardSignupRequestDto;
import com.example.memory_guard.user.dto.LoginResponseDto;
import com.example.memory_guard.user.dto.SignupRequestDto;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import com.example.memory_guard.user.repository.RoleRepository;
import com.example.memory_guard.user.repository.UserRepository;
import com.example.memory_guard.user.service.UserService;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    private Role userRole;
    private Role guardRole;
    private User wardUser;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name("ROLE_USER").build();
        guardRole = Role.builder().name("ROLE_GUARD").build();
        
        UserProfile wardProfile = UserProfile.builder()
            .userId("ward1")
            .username("피보호자1")
            .password("encodedPassword")
            .build();
        wardUser = User.builder()
            .userProfile(wardProfile)
            .build();
        wardUser.addRole(userRole);
    }

    @Test
    @DisplayName("성공: 정상적인 보호자 회원가입")
    void guardSignup_Success() {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        when(userRepository.findByUserProfileUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserProfileUserId("ward1")).thenReturn(Optional.of(wardUser));
        when(roleRepository.findByName("ROLE_GUARD")).thenReturn(Optional.of(guardRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        userService.guardSignup(request);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("실패: 중복된 userId로 보호자 회원가입")
    void guardSignup_DuplicateUserId() {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        UserProfile existingProfile = UserProfile.builder()
            .userId("guard1")
            .username("기존사용자")
            .password("password")
            .build();
        User existingUser = User.builder()
            .userProfile(existingProfile)
            .build();

        when(userRepository.findByUserProfileUserId("guard1")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("이미 존재하는 사용자 ID입니다.");
    }

    @Test
    @DisplayName("실패: 중복된 username으로 보호자 회원가입")
    void guardSignup_DuplicateUsername() {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        when(userRepository.findByUserProfileUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("보호자1")).thenReturn(true);

        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("이미 존재하는 사용자명입니다.");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 피보호자 username")
    void guardSignup_WardNotFound() {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("nonexistent");

        when(userRepository.findByUserProfileUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserProfileUserId("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("존재하지 않는 피보호자입니다.");
    }

    @Test
    @DisplayName("실패: 피보호자가 ROLE_USER 권한이 없음")
    void guardSignup_WardHasNoUserRole() {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        UserProfile wardWithoutUserRoleProfile = UserProfile.builder()
            .userId("ward1")
            .username("피보호자1")
            .password("encodedPassword")
            .build();
        User wardWithoutUserRole = User.builder()
            .userProfile(wardWithoutUserRoleProfile)
            .build();

        when(userRepository.findByUserProfileUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserProfileUserId("ward1")).thenReturn(Optional.of(wardWithoutUserRole));

        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("해당 사용자는 피보호자 권한이 없습니다.");
    }

    @Test
    @DisplayName("실패: ROLE_GUARD가 데이터베이스에 존재하지 않음")
    void guardSignup_GuardRoleNotFound() {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        when(userRepository.findByUserProfileUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserProfileUserId("ward1")).thenReturn(Optional.of(wardUser));
        when(roleRepository.findByName("ROLE_GUARD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("ROLE_GUARD가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("성공: 정상적인 피보호자 회원가입")
    void signup_Success() {
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        when(userRepository.findByUserProfileUserId("user1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("사용자1")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        userService.signup(request);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("실패: 중복된 userId로 피보호자 회원가입")
    void signup_DuplicateUserId() {
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        UserProfile existingProfile = UserProfile.builder()
            .userId("user1")
            .username("기존사용자")
            .password("password")
            .build();
        User existingUser = User.builder()
            .userProfile(existingProfile)
            .build();

        when(userRepository.findByUserProfileUserId("user1")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("이미 존재하는 사용자 ID입니다.");
    }

    @Test
    @DisplayName("실패: 중복된 username으로 피보호자 회원가입")
    void signup_DuplicateUsername() {
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        when(userRepository.findByUserProfileUserId("user1")).thenReturn(Optional.empty());
        when(userRepository.existsByUserProfileUsername("사용자1")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("이미 존재하는 사용자명입니다.");
    }

    @Test
    @DisplayName("성공: 정상적인 로그인")
    void login_Success() {
        String userId = "testUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        UserProfile userProfile = UserProfile.builder().userId(userId).password(encodedPassword).build();
        User user = User.builder().userProfile(userProfile).build();
        user.addRole(Role.builder().name("ROLE_USER").build());

        TokenDto tokenDto = TokenDto.builder().accessToken("access").refreshToken("refresh").grantType("Bearer").build();

        when(userRepository.findByUserProfileUserId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtProvider.generateToken(any(Authentication.class))).thenReturn(tokenDto);

        TokenDto result = userService.login(userId, rawPassword);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access");
        verify(userRepository, times(1)).findByUserProfileUserId(userId);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verify(jwtProvider, times(1)).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 로그인")
    void login_Fail_UserNotFound() {
        when(userRepository.findByUserProfileUserId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("nonexistent", "password"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("존재하지 않는 사용자 ID입니다.");
    }

    @Test
    @DisplayName("실패: 비밀번호 불일치로 로그인 실패")
    void login_Fail_PasswordMismatch() {
        String userId = "testUser";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";
        UserProfile userProfile = UserProfile.builder().userId(userId).password(encodedPassword).build();
        User user = User.builder().userProfile(userProfile).build();

        when(userRepository.findByUserProfileUserId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThatThrownBy(() -> userService.login(userId, rawPassword))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 액세스 토큰 재발급")
    void reissueAccessToken_Success() {
        String refreshToken = "valid.refresh.token";
        String userId = "testUser";
        String newAccessToken = "new.access.token";

        UserProfile userProfile = UserProfile.builder().userId(userId).username("테스트").password("pass").build();
        User user = User.builder().userProfile(userProfile).build();
        user.addRole(Role.builder().name("ROLE_USER").build());

        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findByUserProfileUserId(userId)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(eq(userId), anyString())).thenReturn(newAccessToken);

        LoginResponseDto responseDto = userService.reissueAccessToken(refreshToken);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getAccessToken()).isEqualTo(newAccessToken);
    }

    @Test
    @DisplayName("실패: 유효하지 않은 리프레시 토큰으로 재발급 요청")
    void reissueAccessToken_Fail_InvalidToken() {
        String invalidToken = "invalid.token";
        when(jwtProvider.validateToken(invalidToken)).thenReturn(false);

        assertThatThrownBy(() -> userService.reissueAccessToken(invalidToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("유효하지 않거나 만료된 Refresh Token 입니다.");
    }
}