package com.example.memory_guard.service;

import com.example.memory_guard.user.dto.GuardSignupRequestDto;
import com.example.memory_guard.user.dto.SignupRequestDto;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.repository.RoleRepository;
import com.example.memory_guard.user.domain.repository.UserRepository;
import com.example.memory_guard.user.service.UserService;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

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
        
        wardUser = User.builder()
            .userId("ward1")
            .username("피보호자1")
            .password("encodedPassword")
            .build();
        wardUser.addRole(userRole);
    }

    @Test
    @DisplayName("성공: 정상적인 보호자 회원가입")
    void guardSignup_Success() {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        when(userRepository.findByUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserId("ward1")).thenReturn(Optional.of(wardUser));
        when(roleRepository.findByName("ROLE_GUARD")).thenReturn(Optional.of(guardRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // when
        userService.guardSignup(request);

        // then
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("실패: 중복된 userId로 보호자 회원가입")
    void guardSignup_DuplicateUserId() {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        User existingUser = User.builder()
            .userId("guard1")
            .username("기존사용자")
            .password("password")
            .build();

        when(userRepository.findByUserId("guard1")).thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 사용자 ID입니다.");
    }

    @Test
    @DisplayName("실패: 중복된 username으로 보호자 회원가입")
    void guardSignup_DuplicateUsername() {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        when(userRepository.findByUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("보호자1")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 사용자명입니다.");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 피보호자 username")
    void guardSignup_WardNotFound() {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("nonexistent");

        when(userRepository.findByUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 피보호자입니다.");
    }

    @Test
    @DisplayName("실패: 피보호자가 ROLE_USER 권한이 없음")
    void guardSignup_WardHasNoUserRole() {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        User wardWithoutUserRole = User.builder()
            .userId("ward1")
            .username("피보호자1")
            .password("encodedPassword")
            .build();
        // ROLE_USER 권한을 주지 않음

        when(userRepository.findByUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserId("ward1")).thenReturn(Optional.of(wardWithoutUserRole));

        // when & then
        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당 사용자는 피보호자 권한이 없습니다.");
    }

    @Test
    @DisplayName("실패: ROLE_GUARD가 데이터베이스에 존재하지 않음")
    void guardSignup_GuardRoleNotFound() {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        when(userRepository.findByUserId("guard1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("보호자1")).thenReturn(false);
        when(userRepository.findByUserId("ward1")).thenReturn(Optional.of(wardUser));
        when(roleRepository.findByName("ROLE_GUARD")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.guardSignup(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("ROLE_GUARD가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("성공: 정상적인 피보호자 회원가입")
    void signup_Success() {
        // given
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        when(userRepository.findByUserId("user1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("사용자1")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // when
        userService.signup(request);

        // then
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("실패: 중복된 userId로 피보호자 회원가입")
    void signup_DuplicateUserId() {
        // given
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        User existingUser = User.builder()
            .userId("user1")
            .username("기존사용자")
            .password("password")
            .build();

        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 사용자 ID입니다.");
    }

    @Test
    @DisplayName("실패: 중복된 username으로 피보호자 회원가입")
    void signup_DuplicateUsername() {
        // given
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        when(userRepository.findByUserId("user1")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("사용자1")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 사용자명입니다.");
    }
}