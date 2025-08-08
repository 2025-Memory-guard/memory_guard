package com.example.memory_guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.audio.strategy.saveStrategy.AudioSaveStrategy;
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

    @Mock
    private AudioSaveStrategy audioSaveStrategy;

    @InjectMocks
    private AudioService audioService;

    @Mock
    private MultipartFile mockMultipartFile;

    @Mock
    private AbstractAudioMetadata mockAudioMetadata;

    @Mock
    private AudioMetadataRepository audioMetadataRepository;

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
    @DisplayName("성공: 오디오 파일 저장")
    void saveAudio_Success() throws IOException {
        when(audioSaveStrategy.save(eq(mockMultipartFile), eq(testUser)))
            .thenReturn(mockAudioMetadata);

        when(audioMetadataRepository.save(any(AbstractAudioMetadata.class)))
            .thenReturn(mockAudioMetadata);

        AbstractAudioMetadata result = audioService.saveAudio(mockMultipartFile, testUser);

        assertThat(result).isEqualTo(mockAudioMetadata);
        verify(audioSaveStrategy).save(mockMultipartFile, testUser);
        verify(audioMetadataRepository).save(mockAudioMetadata);
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 IOException 발생")
    void saveAudio_ThrowsIOException() throws IOException {
        IOException ioException = new IOException("파일 저장 실패");
        when(audioSaveStrategy.save(eq(mockMultipartFile), eq(testUser)))
            .thenThrow(ioException);

        assertThatThrownBy(() -> audioService.saveAudio(mockMultipartFile, testUser))
            .isInstanceOf(IOException.class)
            .hasMessage("파일 저장 실패");

        verify(audioSaveStrategy).save(mockMultipartFile, testUser);
    }

    @Test
    @DisplayName("성공: null이 아닌 매개변수로 오디오 저장") // 존재하지않은 audioMetadataRepository.save(metadata); 호출함
    void saveAudio_WithValidParameters() throws IOException {
        when(audioSaveStrategy.save(eq(mockMultipartFile), eq(testUser)))
            .thenReturn(mockAudioMetadata);

        when(audioMetadataRepository.save(any(AbstractAudioMetadata.class)))
            .thenReturn(mockAudioMetadata);

        AbstractAudioMetadata result = audioService.saveAudio(mockMultipartFile, testUser);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockAudioMetadata);
        verify(audioSaveStrategy).save(mockMultipartFile, testUser);
        verify(audioMetadataRepository).save(mockAudioMetadata);
    }

    @Test
    @DisplayName("실패: null MultipartFile로 오디오 저장 시도")
    void saveAudio_WithNullMultipartFile() throws IOException {
        when(audioSaveStrategy.save(eq(null), eq(testUser)))
            .thenThrow(new IllegalArgumentException("MultipartFile이 null입니다"));

        assertThatThrownBy(() -> audioService.saveAudio(null, testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("MultipartFile이 null입니다");

        verify(audioSaveStrategy).save(null, testUser);
    }

    @Test
    @DisplayName("실패: null User로 오디오 저장 시도")
    void saveAudio_WithNullUser() throws IOException {
        when(audioSaveStrategy.save(eq(mockMultipartFile), eq(null)))
            .thenThrow(new IllegalArgumentException("User가 null입니다"));

        assertThatThrownBy(() -> audioService.saveAudio(mockMultipartFile, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User가 null입니다");

        verify(audioSaveStrategy).save(mockMultipartFile, null);
    }
}