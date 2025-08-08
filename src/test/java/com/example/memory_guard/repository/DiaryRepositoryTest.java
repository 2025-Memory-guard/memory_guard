package com.example.memory_guard.repository;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.repository.DiaryRepository;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DiaryRepositoryTest {

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;
    private AbstractAudioMetadata savedAudioMetadata;

    @BeforeEach
    void setUp() {
        String userId = "testUser";
        String password = "testPassword";
        Role userRole = Role.builder().name("ROLE_USER").build();
        entityManager.persist(userRole);

        UserProfile userProfile = UserProfile.builder()
            .userId(userId)
            .username("테스트사용자")
            .password(password)
            .build();

        User user = User.builder()
            .userProfile(userProfile)
            .build();

        user.addRole(userRole);
        savedUser = entityManager.persist(user);

        AbstractAudioMetadata audioMetadata = LocalAudioMetadata.builder()
            .user(user).
            fileSize(1000L)
            .originalFilename("testFile")
            .duration(20L)
            .filePath("/test")
            .build();

        savedAudioMetadata = entityManager.persist(audioMetadata);
    }

    @Test
    @DisplayName("다이어리 저장 및 ID로 조회 테스트")
    void saveAndFindById() {
        Diary diary = Diary.builder()
            .title("테스트 일기")
            .body("이것은 테스트 내용입니다.")
            .author(savedUser)
            .audioMetadata(savedAudioMetadata)
            .build();

        Diary savedDiary = diaryRepository.save(diary);

        Optional<Diary> foundDiaryOpt = diaryRepository.findById(savedDiary.getId());

        assertThat(foundDiaryOpt).isPresent();
        Diary foundDiary = foundDiaryOpt.get();
        assertThat(foundDiary.getId()).isEqualTo(savedDiary.getId());
        assertThat(foundDiary.getTitle()).isEqualTo("테스트 일기");
        assertThat(foundDiary.getAuthor().getId()).isEqualTo(savedUser.getId());
        assertThat(foundDiary.getAudioMetadata().getId()).isEqualTo(savedAudioMetadata.getId());

        assertThat(foundDiary.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("AudioMetadata ID로 다이어리 조회 성공 테스트")
    void findByAudioMetadataId_Success() {
        Diary diary = Diary.builder()
            .title("오디오 ID로 찾을 일기")
            .body("내용")
            .author(savedUser)
            .audioMetadata(savedAudioMetadata)
            .build();
        diaryRepository.save(diary);

        entityManager.flush();
        entityManager.clear();

        Optional<Diary> foundDiaryOpt = diaryRepository.findByAudioMetadataId(savedAudioMetadata.getId());

        assertThat(foundDiaryOpt).isPresent();
        assertThat(foundDiaryOpt.get().getTitle()).isEqualTo("오디오 ID로 찾을 일기");
        assertThat(foundDiaryOpt.get().getAudioMetadata().getId()).isEqualTo(savedAudioMetadata.getId());
    }

    @Test
    @DisplayName("AudioMetadata ID로 다이어리 조회 실패 테스트 (존재하지 않는 ID)")
    void findByAudioMetadataId_Fail() {
        Long nonExistentAudioId = 999L;

        Optional<Diary> foundDiaryOpt = diaryRepository.findByAudioMetadataId(nonExistentAudioId);

        assertThat(foundDiaryOpt).isEmpty();
    }
}