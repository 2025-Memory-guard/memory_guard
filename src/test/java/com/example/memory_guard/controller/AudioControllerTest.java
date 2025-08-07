package com.example.memory_guard.controller;

import com.example.memory_guard.audio.controller.AudioController;
import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AudioController.class, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles("test")
class AudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AudioService audioService;

    @MockitoBean
    private AudioMetadataRepository audioMetadataRepository;

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
        when(audioService.saveAudio(any(), any(User.class)))
            .thenReturn(mockAudioMetadata);

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mockAudioFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("오디오 파일이 성공적으로 저장되었습니다."));

        verify(audioService).saveAudio(any(), any(User.class));
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
                .andExpect(content().string("오디오 파일이 비어있습니다."));

        verify(audioService, never()).saveAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 IOException 발생")
    void audioEvaluation_IOException() throws Exception {
        when(audioService.saveAudio(any(), any(User.class)))
            .thenThrow(new IOException("파일 저장 실패"));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mockAudioFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("서버 오류가 발생했습니다: 파일 저장 실패"));

        verify(audioService).saveAudio(any(), any(User.class));
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 일반 예외 발생")
    void audioEvaluation_GeneralException() throws Exception {
        when(audioService.saveAudio(any(), any(User.class)))
            .thenThrow(new RuntimeException("예상치 못한 오류"));

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mockAudioFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("서버 오류가 발생했습니다: 예상치 못한 오류"));

        verify(audioService).saveAudio(any(), any(User.class));
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
                .andExpect(status().isBadRequest());

        verify(audioService, never()).saveAudio(any(), any(User.class));
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

        when(audioService.saveAudio(any(), any(User.class)))
            .thenReturn(mockAudioMetadata);

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(mp3File)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("오디오 파일이 성공적으로 저장되었습니다."));

        verify(audioService).saveAudio(any(), any(User.class));
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

        when(audioService.saveAudio(any(), any(User.class)))
            .thenReturn(mockAudioMetadata);

        mockMvc.perform(multipart("/api/ward/audio/evaluation")
                .file(largeFile)
                .with(request -> {
                    request.setUserPrincipal(() -> "testUser");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("오디오 파일이 성공적으로 저장되었습니다."));

        verify(audioService).saveAudio(any(), any(User.class));
    }
}