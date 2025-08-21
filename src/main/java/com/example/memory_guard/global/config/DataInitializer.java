package com.example.memory_guard.global.config;

import com.example.memory_guard.analysis.domain.DementiaAnalysis;
import com.example.memory_guard.analysis.domain.FinalFeedback;
import com.example.memory_guard.analysis.domain.SentenceAnalysisIndicators;
import com.example.memory_guard.analysis.repository.FinalFeedbackRepository;
import com.example.memory_guard.analysis.repository.OverallAnalysisRepository;
import com.example.memory_guard.analysis.repository.SentenceAnalysisRepository;
import com.example.memory_guard.audio.domain.AudioTranscription;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.repository.AudioTranscriptionRepository;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.repository.DiaryRepository;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import com.example.memory_guard.user.repository.RoleRepository;
import com.example.memory_guard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AudioMetadataRepository audioMetadataRepository;
  private final AudioTranscriptionRepository audioTranscriptionRepository;
  private final OverallAnalysisRepository overallAnalysisRepository;
  private final DiaryRepository diaryRepository;
  private final SentenceAnalysisRepository sentenceAnalysisRepository;
  private final FinalFeedbackRepository finalFeedbackRepository;
  private final JdbcTemplate jdbcTemplate;
  private final String uploadDir;

  public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                         PasswordEncoder passwordEncoder, AudioMetadataRepository audioMetadataRepository,
                         AudioTranscriptionRepository audioTranscriptionRepository,
                         OverallAnalysisRepository overallAnalysisRepository, DiaryRepository diaryRepository,
                         SentenceAnalysisRepository sentenceAnalysisRepository, FinalFeedbackRepository finalFeedbackRepository,
                         JdbcTemplate jdbcTemplate, @Value("${file.upload-dir}") String uploadDir) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.audioMetadataRepository = audioMetadataRepository;
    this.audioTranscriptionRepository = audioTranscriptionRepository;
    this.overallAnalysisRepository = overallAnalysisRepository;
    this.diaryRepository = diaryRepository;
    this.sentenceAnalysisRepository = sentenceAnalysisRepository;
    this.finalFeedbackRepository = finalFeedbackRepository;
    this.jdbcTemplate = jdbcTemplate;
    this.uploadDir = uploadDir;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {

    Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() ->
        roleRepository.save(Role.builder().name("ROLE_USER").build())
    );
    Role guardRole = roleRepository.findByName("ROLE_GUARD").orElseGet(() ->
        roleRepository.save(Role.builder().name("ROLE_GUARD").build())
    );

    if (userRepository.findByUserProfileUserId("altjd88").isEmpty()) {
      UserProfile userProfile = UserProfile.builder()
          .userId("altjd88")
          .username("김미성")
          .password(passwordEncoder.encode("altjd88"))
          .build();
      User user = User.builder()
          .userProfile(userProfile)
          .build();
      user.addRole(userRole);
      userRepository.save(user);

      if (userRepository.findByUserProfileUserId("tjdwns99").isEmpty()) {
        UserProfile guardianProfile = UserProfile.builder()
            .userId("tjdwns99")
            .username("김성준")
            .password(passwordEncoder.encode("tjdwns99"))
            .build();
        User guardian = User.builder()
            .userProfile(guardianProfile)
            .build();
        guardian.addRole(guardRole);
        guardian.addWard(user);
        userRepository.save(guardian);
      }

      if (userRepository.findByUserProfileUserId("whdcjf65").isEmpty()) {
        UserProfile guardianProfile = UserProfile.builder()
            .userId("whdcjf65")
            .username("박종철")
            .password(passwordEncoder.encode("whdcjf65"))
            .build();
        User user2 = User.builder()
            .userProfile(guardianProfile)
            .build();
        user2.addRole(userRole);
        userRepository.save(user2);
      }


    }

    userRepository.findByUserProfileUserId("altjd88").ifPresent(user -> {
      if (audioMetadataRepository.findByUser(user).stream().anyMatch(m -> m.getOriginalFilename().startsWith("dummy_audio"))) {
        return; // 이미 데이터가 있으면 실행하지 않음
      }

      List<LocalDate> dates = List.of(
          LocalDate.of(2025, 8, 18),
          LocalDate.of(2025, 8, 19),
          LocalDate.of(2025, 8, 20)
      );

      List<String> titles = List.of(
          "비가 오는 오후에",
          "손주와 함께 공원을 간 날",
          "남편과 함께 시원한 드라이브"
      );


      for (int i = 0; i < 3; i++) {
        createDummyDataForDate(user, dates.get(i), titles.get(i));
      }
      userRepository.save(user); // 변경된 사용자 정보(연속 기록일 등) 최종 저장
    });
  }

  private void createDummyDataForDate(User user, LocalDate date, String title) {
    String fileName = "dummy_audio_" + date + ".wav";
    String fullPath = Paths.get(uploadDir, fileName).toString();

    LocalAudioMetadata metadata = LocalAudioMetadata.builder()
        .user(user)
        .originalFilename("dummy_audio_" + date + ".wav")
        .fileSize(1572864L)
        .duration(23L)
        .filePath(fullPath)
        .build();
    LocalAudioMetadata savedMetadata = audioMetadataRepository.save(metadata);

    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(10, 30));
    jdbcTemplate.update(
        "UPDATE abstract_audio_metadata SET created_at = ? WHERE id = ?",
        dateTime, savedMetadata.getId()
    );

    AudioTranscription transcription = AudioTranscription.builder()
        .audioMetadata(savedMetadata)
        .text("오늘은 " + date.getDayOfMonth() + "일입니다. 날씨가 참 좋아서 공원에 산책을 다녀왔어요. " +
            "점심으로는 된장찌개를 먹었고, 오후에는 텔레비전을 조금 보다가 깜빡 잠이 들었네요. " +
            "저녁에는 손주와 영상 통화를 했는데, 재롱을 보니 웃음이 절로 나왔습니다.")
        .build();
    audioTranscriptionRepository.save(transcription);

    double score = 20 + (Math.random() * 15);
    DementiaAnalysis analysis = DementiaAnalysis.builder()
        .audioMetadata(savedMetadata)
        .score(score)
        .dementiaProbability(100.0 - score)
        .speakingRate(140 + (Math.random() * 20))
        .utteranceVolume(65 + (Math.random() * 10))
        .avgSilenceDuration(0.4 + (Math.random() * 0.2))
        .vocabularyAccuracy(90 + (Math.random() * 8))
        .fillerFrequency(1 + (Math.random() * 2))
        .repetitionRatio(3 + (Math.random() * 4))
        .build();
    overallAnalysisRepository.save(analysis);

    Diary diary = Diary.builder()
        .title(title)
        .body("날씨가 좋아 공원으로 산책을 나갔다.\\n손주와 영상 통화를 하며 웃음꽃을 피웠다.\\n소소한 행복이 가득한 하루였다.")
        .author(user)
        .audioMetadata(savedMetadata)
        .build();
    Diary savedDiary = diaryRepository.save(diary);
    jdbcTemplate.update(
        "UPDATE diary SET created_at = ? WHERE id = ?",
        dateTime, savedDiary.getId()
    );

    SentenceAnalysisIndicators indicator1 = SentenceAnalysisIndicators.builder()
        .audioMetadata(savedMetadata)
        .category("시제 일치")
        .comment("과거의 일을 이야기할 때는 시제를 맞춰주는 것이 좋아요. 잘하고 계세요!")
        .exampleOriginal("텔레비전을 조금 보다가 깜빡 잠이 들고 있어요.")
        .exampleSuggestion("텔레비전을 조금 보다가 깜빡 잠이 들었네요.")
        .build();
    sentenceAnalysisRepository.save(indicator1);

    SentenceAnalysisIndicators indicator2 = SentenceAnalysisIndicators.builder()
        .audioMetadata(savedMetadata)
        .category("어휘 다양성")
        .comment("'조금'이라는 표현 대신 다른 단어를 사용하면 문장이 더 풍부해질 거예요.")
        .exampleOriginal("텔레비전을 조금 보다가...")
        .exampleSuggestion("텔레비전을 잠시 보다가...")
        .build();
    sentenceAnalysisRepository.save(indicator2);

    FinalFeedback feedback1 = FinalFeedback.builder()
        .audioMetadata(savedMetadata)
        .title("오늘의 피드백")
        .content("전반적으로 하루의 일상을 차분하게 잘 표현해주셨어요. 특히 손주와의 통화를 이야기하실 때 목소리에서 행복함이 느껴져 듣기 좋았습니다.")
        .build();
    finalFeedbackRepository.save(feedback1);

    FinalFeedback feedback2 = FinalFeedback.builder()
        .audioMetadata(savedMetadata)
        .title("시제 표현")
        .content("과거의 일을 말씀하실 때 현재 시제를 사용하는 경우가 가끔 있어요. '...했어요' 또는 '...했습니다' 와 같이 과거형으로 마무리하면 더 자연스러울 거예요.")
        .build();
    finalFeedbackRepository.save(feedback2);

    LocalDate previousDate = user.getLastRecordingDate();
    if (previousDate == null || !date.minusDays(1).equals(previousDate)) {
      user.setConsecutiveRecordingDays(1);
    } else {
      user.setConsecutiveRecordingDays(user.getConsecutiveRecordingDays() + 1);
    }
    user.setLastRecordingDate(date);

    user.addAudioMetadata(savedMetadata);
    user.updateAvgRecordingTime((int) savedMetadata.getDuration().longValue());
    user.updateAvgScore(analysis.getScore());
  }
}