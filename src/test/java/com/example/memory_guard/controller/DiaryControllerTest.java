package com.example.memory_guard.controller;

import com.example.memory_guard.diary.controller.DiaryController;
import com.example.memory_guard.diary.dto.DiaryResponseDto;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import com.example.memory_guard.global.config.SecurityConfig;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = DiaryController.class)
@Import(SecurityConfig.class)
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화를 위해 ObjectMapper 주입

    @MockBean
    private DiaryService diaryService;

    @MockBean
    private JwtProvider jwtProvider;

    private User testUser;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        UserProfile userProfile = UserProfile.builder()
            .userId("testUser")
            .username("테스트사용자")
            .password("password")
            .build();
        testUser = User.builder().userProfile(userProfile).build();
        testUser.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "", testUser.getAuthorities())
        );
    }

    @Test
    @DisplayName("성공: 사용자의 일기 목록을 정상적으로 조회한다")
    void getUserDiaries_Success() throws Exception {
        // given
        List<DiaryResponseDto> mockDiaries = List.of(
            DiaryResponseDto.builder()
                .title("첫 번째 일기")
                .body("오늘은 날씨가 좋았다.")
                .authorName("테스트사용자")
                .writtenAt(LocalDate.now())
                .build(),
            DiaryResponseDto.builder()
                .title("두 번째 일기")
                .body("내일은 비가 온다.")
                .authorName("테스트사용자")
                .writtenAt(LocalDate.now().minusDays(1))
                .build()
        );

        when(diaryService.getUserDiaries(testUser.getId())).thenReturn(mockDiaries);

        // when & then
        mockMvc.perform(get("/api/ward/diarys"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // jsonPath의 value() 메소드 사용
            .andExpect(jsonPath("$[0].title").value("첫 번째 일기"))
            .andExpect(jsonPath("$[0].body").value("오늘은 날씨가 좋았다."))
            .andExpect(jsonPath("$[1].title").value("두 번째 일기"))
            .andExpect(jsonPath("$[1].authorName").value("테스트사용자"));

        // verify
        verify(diaryService, times(1)).getUserDiaries(testUser.getId());
    }

    @Test
    @DisplayName("성공: 작성된 일기가 없을 경우 빈 배열을 반환한다")
    void getUserDiaries_EmptyList() throws Exception {
        when(diaryService.getUserDiaries(testUser.getId())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/ward/diarys"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(Collections.emptyList())));

        verify(diaryService, times(1)).getUserDiaries(testUser.getId());
    }
}