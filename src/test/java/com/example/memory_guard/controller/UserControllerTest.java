package com.example.memory_guard.controller;

import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.global.exception.custom.AuthenticationException;
import com.example.memory_guard.user.controller.UserController;
import com.example.memory_guard.user.dto.GuardSignupRequestDto;
import com.example.memory_guard.user.dto.LoginRequestDto;
import com.example.memory_guard.user.dto.LoginResponseDto;
import com.example.memory_guard.user.dto.SignupRequestDto;
import com.example.memory_guard.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("성공: 보호자 회원가입 요청")
    void guardSignup_Success() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doNothing().when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("보호자 회원가입이 완료되었습니다."));

        verify(userService).guardSignup(any(GuardSignupRequestDto.class));
    }

    @Test
    @DisplayName("실패: 중복된 userId로 보호자 회원가입")
    void guardSignup_DuplicateUserId() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자 ID입니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자 ID입니다."));
    }

    @Test
    @DisplayName("실패: 중복된 username으로 보호자 회원가입")
    void guardSignup_DuplicateUsername() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자명입니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자명입니다."));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 피보호자")
    void guardSignup_WardNotFound() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("nonexistent");

        doThrow(new IllegalArgumentException("존재하지 않는 피보호자입니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 피보호자입니다."));
    }

    @Test
    @DisplayName("실패: 피보호자가 ROLE_USER 권한이 없음")
    void guardSignup_WardHasNoUserRole() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalArgumentException("해당 사용자는 피보호자 권한이 없습니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("해당 사용자는 피보호자 권한이 없습니다."));
    }

    @Test
    @DisplayName("실패: ROLE_GUARD가 존재하지 않음")
    void guardSignup_GuardRoleNotFound() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalStateException("ROLE_GUARD가 존재하지 않습니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("ROLE_GUARD가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("성공: 피보호자 회원가입 요청")
    void wardSignup_Success() throws Exception {
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        doNothing().when(userService).signup(any(SignupRequestDto.class));

        mockMvc.perform(post("/ward/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."));

        verify(userService).signup(any(SignupRequestDto.class));
    }

    @Test
    @DisplayName("실패: 중복된 userId로 피보호자 회원가입")
    void wardSignup_DuplicateUserId() throws Exception {
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자 ID입니다."))
            .when(userService).signup(any(SignupRequestDto.class));

        mockMvc.perform(post("/ward/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자 ID입니다."));
    }

    @Test
    @DisplayName("실패: 중복된 username으로 피보호자 회원가입")
    void wardSignup_DuplicateUsername() throws Exception {
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자명입니다."))
            .when(userService).signup(any(SignupRequestDto.class));

        mockMvc.perform(post("/ward/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자명입니다."));
    }

    @Test
    @DisplayName("실패: 필수 필드 누락")
    void guardSignup_MissingFields() throws Exception {
        GuardSignupRequestDto request = new GuardSignupRequestDto();

        doThrow(new IllegalArgumentException("필수 필드가 누락되었습니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 정상적인 로그인 요청")
    void login_Success() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUserId("testUser");
        loginRequest.setPassword("password");

        TokenDto tokenDto = TokenDto.builder()
            .grantType("Bearer")
            .accessToken("access.token")
            .refreshToken("refresh.token")
            .build();

        when(userService.login("testUser", "password")).thenReturn(tokenDto);
        when(userService.getUserRoles("testUser")).thenReturn(List.of("ROLE_USER"));

        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.grantType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").value("access.token"))
            .andExpect(jsonPath("$.userId").value("testUser"))
            .andExpect(cookie().httpOnly("refreshToken", true))
            .andExpect(cookie().value("refreshToken", "refresh.token"));
    }

    @Test
    @DisplayName("실패: 잘못된 자격증명으로 로그인 요청 시 401 Unauthorized")
    void login_Failure_InvalidCredentials() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUserId("testUser");
        loginRequest.setPassword("wrongPassword");

        when(userService.login(anyString(), anyString()))
            .thenThrow(new AuthenticationException("비밀번호가 일치하지 않습니다."));

        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 토큰 재발급")
    void reissue_Success() throws Exception {
        LoginResponseDto responseDto = LoginResponseDto.builder()
            .grantType("Bearer").accessToken("new.access.token").build();
        when(userService.reissueAccessToken("valid.refresh.token")).thenReturn(responseDto);

        mockMvc.perform(post("/token/reissue")
                .cookie(new Cookie("refreshToken", "valid.refresh.token")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new.access.token"));
    }

    @Test
    @DisplayName("실패: 리프레시 토큰 없이 재발급 요청 시 401 Unauthorized")
    void reissue_Failure_NoToken() throws Exception {
        mockMvc.perform(post("/token/reissue"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$").value("Refresh Token이 없습니다."));
    }

    @Test
    @DisplayName("실패: 유효하지 않은 리프레시 토큰으로 재발급 요청 시 401 Unauthorized 및 쿠키 삭제")
    void reissue_Failure_InvalidToken() throws Exception {
        when(userService.reissueAccessToken("invalid.token"))
            .thenThrow(new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token 입니다."));

        mockMvc.perform(post("/token/reissue")
                .cookie(new Cookie("refreshToken", "invalid.token")))
            .andExpect(status().isUnauthorized())
            .andExpect(cookie().maxAge("refreshToken", 0))
            .andExpect(jsonPath("$").value("유효하지 않은 Refresh Token 입니다. 다시 로그인해주세요."));
    }
}