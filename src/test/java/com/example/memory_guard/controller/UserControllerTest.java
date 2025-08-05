package com.example.memory_guard.controller;

import com.example.memory_guard.user.controller.UserController;
import com.example.memory_guard.user.dto.GuardSignupRequestDto;
import com.example.memory_guard.user.dto.SignupRequestDto;
import com.example.memory_guard.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
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
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doNothing().when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
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
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자 ID입니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 사용자 ID입니다."));
    }

    @Test
    @DisplayName("실패: 중복된 username으로 보호자 회원가입")
    void guardSignup_DuplicateUsername() throws Exception {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자명입니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 사용자명입니다."));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 피보호자")
    void guardSignup_WardNotFound() throws Exception {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("nonexistent");

        doThrow(new IllegalArgumentException("존재하지 않는 피보호자입니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 피보호자입니다."));
    }

    @Test
    @DisplayName("실패: 피보호자가 ROLE_USER 권한이 없음")
    void guardSignup_WardHasNoUserRole() throws Exception {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalArgumentException("해당 사용자는 피보호자 권한이 없습니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("해당 사용자는 피보호자 권한이 없습니다."));
    }

    @Test
    @DisplayName("실패: ROLE_GUARD가 존재하지 않음")
    void guardSignup_GuardRoleNotFound() throws Exception {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        request.setUserId("guard1");
        request.setUsername("보호자1");
        request.setPassword("password123");
        request.setWardUserId("ward1");

        doThrow(new IllegalStateException("ROLE_GUARD가 존재하지 않습니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ROLE_GUARD가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("성공: 피보호자 회원가입 요청")
    void wardSignup_Success() throws Exception {
        // given
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        doNothing().when(userService).signup(any(SignupRequestDto.class));

        // when & then
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
        // given
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자 ID입니다."))
            .when(userService).signup(any(SignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/ward/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 사용자 ID입니다."));
    }

    @Test
    @DisplayName("실패: 중복된 username으로 피보호자 회원가입")
    void wardSignup_DuplicateUsername() throws Exception {
        // given
        SignupRequestDto request = new SignupRequestDto();
        request.setUserId("user1");
        request.setUsername("사용자1");
        request.setPassword("password123");

        doThrow(new IllegalArgumentException("이미 존재하는 사용자명입니다."))
            .when(userService).signup(any(SignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/ward/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 사용자명입니다."));
    }

    @Test
    @DisplayName("실패: 잘못된 JSON 형식")
    void guardSignup_InvalidJson() throws Exception {
        // given
        String invalidJson = "{ invalid json }";

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 필수 필드 누락")
    void guardSignup_MissingFields() throws Exception {
        // given
        GuardSignupRequestDto request = new GuardSignupRequestDto();
        // 필드를 설정하지 않음

        doThrow(new IllegalArgumentException("필수 필드가 누락되었습니다."))
            .when(userService).guardSignup(any(GuardSignupRequestDto.class));

        // when & then
        mockMvc.perform(post("/guard/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}