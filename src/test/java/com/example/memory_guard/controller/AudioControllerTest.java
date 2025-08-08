package com.example.memory_guard.controller;

import com.example.memory_guard.audio.controller.AudioController;
import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.global.exception.GlobalExceptionHandler;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = {AudioController.class, GlobalExceptionHandler.class}, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
class AudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AudioService audioService;

    @MockitoBean
    private DiaryService diaryService;

    @MockitoBean
    private AudioMetadataRepository audioMetadataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile mockAudioFile;
    private User testUser;
    private AbstractAudioMetadata mockAudioMetadata;

    @BeforeEach
    void setUp() {
        mockAudioFile = new MockMultipartFile(
            "audioFile",
            "test.wav",
            "audio/wav",
            "test audio content".getBytes()
        );

        UserProfile userProfile = UserProfile.builder()
            .userId("testUser")
            .username("테스트사용자")
            .password("password")
            .build();
        
        testUser = User.builder()
            .userProfile(userProfile)
            .build();

        mockAudioMetadata = LocalAudioMetadata.builder()
            .user(testUser)
            .originalFilename("test.wav")
            .fileSize(1024L)
            .duration(0L)
            .filePath("/path/to/test.wav")
            .build();
    }

    @Test
    @DisplayName("성공: 오디오 파일 업로드 및 평가")
    void audioEvaluation_Success() throws Exception {
        doNothing().when(audioService).processNewAudio(any(), any(User.class));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mockAudioFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("오디오 파일이 성공적으로 저장되었습니다."));

        verify(audioService).processNewAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("실패: 빈 오디오 파일 업로드")
    void audioEvaluation_EmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "audioFile",
            "empty.wav",
            "audio/wav",
            new byte[0]
        );

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(emptyFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("오디오 파일이 비어있습니다."));

        verify(audioService, never()).processNewAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 IOException 발생")
    void audioEvaluation_IOException() throws Exception {
        doThrow(new IOException("파일 저장 실패"))
            .when(audioService).processNewAudio(any(), any(User.class));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mockAudioFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("FILE_IO_ERROR"));

        verify(audioService).processNewAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 일반 예외 발생")
    void audioEvaluation_GeneralException() throws Exception {
        doThrow(new RuntimeException("예상치 못한 오류"))
            .when(audioService).processNewAudio(any(), any(User.class));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mockAudioFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 오류"));

        verify(audioService).processNewAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("실패: audioFile 파라미터 누락")
    void audioEvaluation_MissingAudioFileParameter() throws Exception {
        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(audioService, never()).processNewAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("성공: 다양한 오디오 파일 형식 업로드")
    void audioEvaluation_DifferentAudioFormats() throws Exception {
        MockMultipartFile mp3File = new MockMultipartFile(
            "audioFile",
            "test.mp3",
            "audio/mpeg",
            "test mp3 content".getBytes()
        );

        doNothing().when(audioService).processNewAudio(any(), any(User.class));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mp3File)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("오디오 파일이 성공적으로 저장되었습니다."));

        verify(audioService).processNewAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("성공: 큰 오디오 파일 업로드")
    void audioEvaluation_LargeFile() throws Exception {
        byte[] largeContent = new byte[1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
            "audioFile",
            "large.wav",
            "audio/wav",
            largeContent
        );

        doNothing().when(audioService).processNewAudio(any(), any(User.class));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(largeFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("오디오 파일이 성공적으로 저장되었습니다."));

        verify(audioService).processNewAudio(any(), any(User.class));
    }

  @Test
  @DisplayName("성공: 오디오 ID로 오디오 파일과 일기를 함께 조회한다")
  void playAudio_Success() throws Exception {
    Long audioId = 1L;

    File tempFile = Files.createTempFile("test-audio", ".wav").toFile();
    tempFile.deleteOnExit();

    UserProfile userProfile = UserProfile.builder().userId("testUser").username("테스트사용자").build();
    User author = User.builder().userProfile(userProfile).build();

    Diary mockDiary = mock(Diary.class);
    when(mockDiary.getTitle()).thenReturn("테스트 일기");
    when(mockDiary.getBody()).thenReturn("이것은 테스트 내용입니다.");
    when(mockDiary.getAuthor()).thenReturn(author);
    when(mockDiary.getCreatedAt()).thenReturn(LocalDateTime.now());

    when(audioService.getFile(audioId)).thenReturn(tempFile);
    when(audioService.getDairyByAudioId(audioId)).thenReturn(mockDiary);

    mockMvc.perform(get("/api/ward/audio/play/{audioId}", audioId))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA));

    verify(audioService, times(1)).getFile(audioId);
    verify(audioService, times(1)).getDairyByAudioId(audioId);
  }

  @Test
  @DisplayName("실패: 존재하지 않는 오디오 ID로 조회 시 404 Not Found")
  void playAudio_Failure_NotFound() throws Exception {
    Long nonExistentAudioId = 999L;
    when(audioService.getFile(nonExistentAudioId)).thenThrow(new IOException("파일을 찾을 수 없습니다."));

    mockMvc.perform(get("/api/ward/audio/play/{audioId}", nonExistentAudioId))
        .andExpect(status().isInternalServerError());
  }

}