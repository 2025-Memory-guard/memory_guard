package com.example.memory_guard.guard.service;

import com.example.memory_guard.audio.dto.response.AudioStampResponseDto;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.repository.DiaryRepository;
import com.example.memory_guard.guard.dto.GuardHomeResponseDto;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GuardService {

  private final DiaryRepository diaryRepository;
  private final AudioService audioService;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public GuardHomeResponseDto getGuardHomeData(User guardian) {
    User managedGuardian = userRepository.findById(guardian.getId())
        .orElseThrow(() -> new UsernameNotFoundException("보호자 정보를 찾을 수 없습니다."));

    if (managedGuardian.getWards().isEmpty()) {
      throw new IllegalStateException("관리하는 피보호자가 없습니다.");
    }

    User selectedWard = managedGuardian.getSelectedWard();

    if (selectedWard == null) {
      return GuardHomeResponseDto.builder().build();
    }

    AudioStampResponseDto stampsDto = audioService.getAudioStamps(selectedWard);

    List<Diary> diaries = diaryRepository.findByAuthorId(selectedWard.getId());
    List<GuardHomeResponseDto.DiaryInfo> diaryInfos = getDiaryInfos(diaries);

    return GuardHomeResponseDto.builder()
        .selectedWardName(selectedWard.getUserProfile().getUsername())
        .consecutiveRecordingDays(stampsDto.getConsecutiveRecordingDays())
        .weeklyStamps(stampsDto.getWeeklyStamps())
        .diaryList(diaryInfos)
        .build();
  }

  private static List<GuardHomeResponseDto.DiaryInfo> getDiaryInfos(List<Diary> diaries) {
    return diaries.stream()
        .map(diary -> GuardHomeResponseDto.DiaryInfo.builder()
            .title(diary.getTitle())
            .date(diary.getCreatedAt().toLocalDate())
            .build())
        .collect(Collectors.toList());
  }
}
