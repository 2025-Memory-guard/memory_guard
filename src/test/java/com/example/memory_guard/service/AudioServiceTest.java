package com.example.memory_guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioEvaluationService;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.audio.service.AudioStorageService;
import com.example.memory_guard.audio.strategy.saveStrategy.AudioSaveStrategy;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @InjectMocks
    private AudioService audioService;

    @Mock
    private AudioStorageService audioStorageService;
    @Mock
    private AudioEvaluationService audioEvaluationService;
    @Mock
    private DiaryService diaryService;

    @Mock
    private MultipartFile mockMultipartFile;
    @Mock
    private AbstractAudioMetadata mockAudioMetadata;

    private User testUser;

    @BeforeEach
    void setUp() {
        UserProfile userProfile = UserProfile.builder()
            .userId("testUser")
            .username("테스트사용자")
            .password("password")
            .build();

        testUser = User.builder()
            .userProfile(userProfile)
            .build();
    }

    @Test
    @DisplayName("성공: 새로운 오디오 처리 시 모든 하위 서비스가 순서대로 호출된다")
    void processNewAudio_Success_ShouldCallServices() throws IOException {
        when(audioStorageService.save(mockMultipartFile, testUser)).thenReturn(mockAudioMetadata);

        audioService.saveAudio(mockMultipartFile, testUser);

        verify(audioStorageService, times(1)).save(mockMultipartFile, testUser);
        verify(audioEvaluationService, times(1)).evaluate(mockAudioMetadata, testUser);
        verify(diaryService, times(1)).createAudioDiary(mockAudioMetadata, testUser);
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 IOException 발생 시 예외가 전파된다")
    void processNewAudio_ThrowsIOException_WhenStorageFails() throws IOException {
        when(audioStorageService.save(mockMultipartFile, testUser))
            .thenThrow(new IOException("디스크 공간 부족"));

        assertThatThrownBy(() -> audioService.saveAudio(mockMultipartFile, testUser))
            .isInstanceOf(IOException.class)
            .hasMessage("디스크 공간 부족");

        verify(audioEvaluationService, never()).evaluate(any(), any());
        verify(diaryService, never()).createAudioDiary(any(), any());
    }
}