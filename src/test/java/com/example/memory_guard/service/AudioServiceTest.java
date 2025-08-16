package com.example.memory_guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.dto.response.AudioStampResponseDto;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.analysis.service.AudioAnalysisService;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.audio.service.AudioStorageService;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;

import com.example.memory_guard.user.domain.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private AudioAnalysisService audioEvaluationService;
    @Mock
    private DiaryService diaryService;

    @Mock
    private AudioMetadataRepository audioMetadataRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private MultipartFile mockMultipartFile;
    @Mock
    private AbstractAudioMetadata mockAudioMetadata;

    @Spy
    private User testUser = User.builder()
        .userProfile(UserProfile.builder()
            .userId("testUser")
            .username("테스트사용자")
            .password("password")
            .build())
        .build();

    @Test
    @DisplayName("성공: 새로운 오디오 처리 시 User의 updateRecordingStreak 메소드가 호출된다")
    void processNewAudio_shouldCallUpdateRecordingStreak() throws IOException {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(audioStorageService.save(mockMultipartFile, testUser)).thenReturn(mockAudioMetadata);

        audioService.processNewAudio(mockMultipartFile, testUser);

        verify(testUser, times(1)).updateRecordingStreak();
        verify(audioStorageService, times(1)).save(mockMultipartFile, testUser);
        verify(audioEvaluationService, times(1)).evaluate(mockAudioMetadata, testUser);
        verify(diaryService, times(1)).createAudioDiary(mockAudioMetadata, testUser);
    }

    @Test
    @DisplayName("성공: 주간 오디오 스탬프 조회 시 올바른 DTO를 반환한다")
    void getAudioStamps_Success() {
        AbstractAudioMetadata mondayRecording = mock(AbstractAudioMetadata.class);
        when(mondayRecording.getCreatedAt()).thenReturn(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY));

        AbstractAudioMetadata wednesdayRecording = mock(AbstractAudioMetadata.class);
        when(wednesdayRecording.getCreatedAt()).thenReturn(LocalDateTime.now().with(java.time.DayOfWeek.WEDNESDAY));

        List<AbstractAudioMetadata> weeklyRecordings = List.of(mondayRecording, wednesdayRecording);

        when(audioMetadataRepository.findByUserAndCreatedAtBetween(eq(testUser), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(weeklyRecordings);

        AudioStampResponseDto response = audioService.getAudioStamps(testUser);

        assertThat(response.getConsecutiveRecordingDays()).isEqualTo(testUser.getConsecutiveRecordingDays());
        assertThat(response.getWeeklyStamps()).hasSize(2);
        assertThat(response.getWeeklyStamps()).contains(
            LocalDate.now().with(java.time.DayOfWeek.MONDAY),
            LocalDate.now().with(java.time.DayOfWeek.WEDNESDAY)
        );
    }

    @Test
    @DisplayName("성공: 주간 녹음 기록이 없을 경우 빈 리스트를 반환한다")
    void getAudioStamps_NoRecordings() {
        when(audioMetadataRepository.findByUserAndCreatedAtBetween(eq(testUser), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        AudioStampResponseDto response = audioService.getAudioStamps(testUser);

        assertThat(response.getConsecutiveRecordingDays()).isEqualTo(0);
        assertThat(response.getWeeklyStamps()).isNotNull();
        assertThat(response.getWeeklyStamps()).isEmpty();
    }


    @Test
    @DisplayName("성공: 새로운 오디오 처리 시 모든 하위 서비스가 순서대로 호출된다")
    void processNewAudio_Success_ShouldCallServices() throws IOException {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(audioStorageService.save(mockMultipartFile, testUser)).thenReturn(mockAudioMetadata);

        audioService.processNewAudio(mockMultipartFile, testUser);

        verify(audioStorageService, times(1)).save(mockMultipartFile, testUser);
        verify(audioEvaluationService, times(1)).evaluate(mockAudioMetadata, testUser);
        verify(diaryService, times(1)).createAudioDiary(mockAudioMetadata, testUser);
    }

    @Test
    @DisplayName("실패: 오디오 저장 중 IOException 발생 시 예외가 전파된다")
    void processNewAudio_ThrowsIOException_WhenStorageFails() throws IOException {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(audioStorageService.save(mockMultipartFile, testUser))
            .thenThrow(new IOException("디스크 공간 부족"));

        assertThatThrownBy(() -> audioService.processNewAudio(mockMultipartFile, testUser))
            .isInstanceOf(IOException.class)
            .hasMessage("디스크 공간 부족");

        verify(audioEvaluationService, never()).evaluate(any(), any());
        verify(diaryService, never()).createAudioDiary(any(), any());
    }
}