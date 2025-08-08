package com.example.memory_guard.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.repository.DiaryRepository;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

  @InjectMocks
  private DiaryService diaryService;

  @Mock
  private DiaryRepository diaryRepository;

  private User testUser;
  private AbstractAudioMetadata mockAudioMetadata;
  private Diary testDiary;
  private File tempFile;

  @BeforeEach
  void setUp() throws IOException {
    UserProfile userProfile = UserProfile.builder().userId("testUser").username("테스트사용자").password("password").build();
    testUser = User.builder().userProfile(userProfile).build();

    Path tempFilePath = Files.createTempFile("test-audio", ".wav");
    tempFile = tempFilePath.toFile();

    mockAudioMetadata = LocalAudioMetadata.builder()
        .filePath(tempFile.getAbsolutePath())
        .user(testUser)
        .build();

    testDiary = Diary.builder()
        .title("제목")
        .body("본문")
        .author(testUser)
        .audioMetadata(mockAudioMetadata)
        .build();
  }

  @Test
  @DisplayName("성공: 오디오 메타데이터로 일기를 생성하고 저장한다")
  void createAudioDiary_Success() throws IOException {
    diaryService.createAudioDiary(mockAudioMetadata, testUser);

    verify(diaryRepository, times(1)).save(any(Diary.class));
  }

  @Test
  @DisplayName("성공: 오디오 ID로 다이어리를 성공적으로 조회한다")
  void getDairyByAudioId_Success() {
    Long audioId = 1L;
    when(diaryRepository.findByAudioMetadataId(audioId)).thenReturn(Optional.of(testDiary));

    Diary foundDiary = diaryService.getDairyByAudioId(audioId);

    assertThat(foundDiary).isNotNull();
    assertThat(foundDiary.getTitle()).isEqualTo("제목");
    verify(diaryRepository, times(1)).findByAudioMetadataId(audioId);
  }

  @Test
  @DisplayName("실패: 존재하지 않는 오디오 ID로 조회 시 예외가 발생한다")
  void getDairyByAudioId_NotFound_ThrowsException() {
    Long nonExistentAudioId = 999L;
    when(diaryRepository.findByAudioMetadataId(nonExistentAudioId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> diaryService.getDairyByAudioId(nonExistentAudioId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("해당 오디오 ID에 맞는 다이어리를 찾을 수 없습니다: " + nonExistentAudioId);
  }
}