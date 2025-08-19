package com.example.memory_guard.entity;

import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set; // List -> Set

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private User testUser;

    @BeforeEach
    void setup() {
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
    @DisplayName("User 빌더 패턴과 addRole 메서드로 생성 테스트")
    void memberBuilderAndAddRoleTest() {
        String userId = "testUser";
        String password = "testPassword";
        Role userRole = Role.builder().name("ROLE_USER").build();
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();

        UserProfile userProfile = UserProfile.builder()
            .userId(userId)
            .username("테스트사용자")
            .password(password)
            .build();
        User member = User.builder()
            .userProfile(userProfile)
            .build();

        member.addRole(userRole);
        member.addRole(adminRole);

        assertThat(member).isNotNull();
        assertThat(member.getUserProfile().getUserId()).isEqualTo(userId);
        assertThat(member.getUserProfile().getPassword()).isEqualTo(password);
        assertThat(member.getRoles()).isInstanceOf(Set.class);
        assertThat(member.getRoles()).hasSize(2);
        assertThat(member.getRoles()).extracting("name")
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("User 단일 권한으로 생성 테스트")
    void memberSingleRoleTest() {
        String userId = "user1";
        String password = "pass123";
        Role userRole = Role.builder().name("ROLE_USER").build();

        UserProfile userProfile = UserProfile.builder()
            .userId(userId)
            .username("사용자1")
            .password(password)
            .build();
        User member = User.builder()
            .userProfile(userProfile)
            .build();
        member.addRole(userRole);

        assertThat(member.getUserProfile().getUserId()).isEqualTo(userId);
        assertThat(member.getUserProfile().getPassword()).isEqualTo(password);
        assertThat(member.getRoles()).hasSize(1);
        assertThat(member.getRoles()).extracting("name")
            .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("User 권한 없이 생성 테스트")
    void memberWithoutRolesTest() {
        String userId = "user2";
        String password = "pass456";

        UserProfile userProfile = UserProfile.builder()
            .userId(userId)
            .username("사용자2")
            .password(password)
            .build();
        User member = User.builder()
            .userProfile(userProfile)
            .build();

        assertThat(member.getUserProfile().getUserId()).isEqualTo(userId);
        assertThat(member.getUserProfile().getPassword()).isEqualTo(password);
        // roles 필드가 new HashSet<>()으로 초기화되어 있으므로, 비어있는 Set이어야 함
        assertThat(member.getRoles()).isNotNull();
        assertThat(member.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("User getter 메소드 테스트")
    void memberGetterTest() {
        String userId = "testId";
        String password = "testPass";
        Role userRole = Role.builder().name("ROLE_USER").build();

        UserProfile userProfile = UserProfile.builder()
            .userId(userId)
            .username("테스트아이디")
            .password(password)
            .build();
        User member = User.builder()
            .userProfile(userProfile)
            .build();
        member.addRole(userRole);

        assertThat(member.getId()).isNull();
        assertThat(member.getUserProfile().getUserId()).isEqualTo(userId);
        assertThat(member.getUserProfile().getPassword()).isEqualTo(password);
        assertThat(member.getRoles()).extracting("name").contains("ROLE_USER");
    }

    @Test
    @DisplayName("중복된 권한 추가 시 Set에 의해 한 번만 저장되는지 테스트")
    void addDuplicateRoleTest() {
        UserProfile userProfile = UserProfile.builder()
            .userId("testUser")
            .username("테스트유저")
            .password("password")
            .build();
        User member = User.builder()
            .userProfile(userProfile)
            .build();
        Role userRole = Role.builder().name("ROLE_USER").build();

        member.addRole(userRole);
        member.addRole(userRole);

        assertThat(member.getRoles()).hasSize(1);
    }


    @Test
    @DisplayName("User 객체 동등성 테스트 - @EqualsAndHashCode가 없을 경우")
    void memberEqualityTest() {
        Role userRole = Role.builder().name("ROLE_USER").build();

        UserProfile profile1 = UserProfile.builder().userId("user1").username("사용자1").password("pass1").build();
        User member1 = User.builder().userProfile(profile1).build();
        member1.addRole(userRole);

        UserProfile profile2 = UserProfile.builder().userId("user1").username("중복사용자1").password("pass1").build();
        User member2 = User.builder().userProfile(profile2).build();
        member2.addRole(userRole);

        UserProfile profile3 = UserProfile.builder().userId("user2").username("사용자2").password("pass1").build();
        User member3 = User.builder().userProfile(profile3).build();
        member3.addRole(userRole);

        assertThat(member1).isNotEqualTo(member2);
        assertThat(member1).isNotEqualTo(member3);
    }

    @Test
    @DisplayName("User null 값으로 생성 테스트")
    void memberNullValuesTest() {
        UserProfile userProfile = UserProfile.builder()
            .userId(null)
            .username(null)
            .password(null)
            .build();
        User member = User.builder()
            .userProfile(userProfile)
            .build();

        assertThat(member).isNotNull();
        assertThat(member.getUserProfile().getUserId()).isNull();
        assertThat(member.getUserProfile().getPassword()).isNull();
        assertThat(member.getRoles()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("성공: 보호자(guardian)가 피보호자(ward)를 추가하면 양방향 관계가 설정된다")
    void should_set_bidirectional_relationship_when_adding_ward() {
        UserProfile guardianProfile = UserProfile.builder()
            .userId("guardian1")
            .username("가디언1")
            .password("guardianPass")
            .build();
        User guardian = User.builder()
            .userProfile(guardianProfile)
            .build();

        UserProfile wardProfile = UserProfile.builder()
            .userId("ward1")
            .username("피보호자1")
            .password("wardPass")
            .build();
        User ward = User.builder()
            .userProfile(wardProfile)
            .build();

//        guardian.addWard(ward);
//
//        assertThat(guardian.getWards()).hasSize(1);
//        assertThat(guardian.getWards()).contains(ward);

//        assertThat(ward.getGuardian()).isNotNull();
//        assertThat(ward.getGuardian()).isEqualTo(guardian);
    }

    @Test
    @DisplayName("성공: 첫 녹음 시 연속 녹음 일수는 1이 되고 마지막 녹음 날짜는 오늘로 설정된다")
    void updateRecordingStreak_FirstTime() {
        testUser.updateRecordingStreak();

        assertThat(testUser.getConsecutiveRecordingDays()).isEqualTo(1);
        assertThat(testUser.getLastRecordingDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("성공: 어제 녹음 후 오늘 또 녹음하면 연속 녹음 일수가 1 증가한다")
    void updateRecordingStreak_ConsecutiveDays() {
         testUser.setLastRecordingDate(LocalDate.now().minusDays(1));
         testUser.setConsecutiveRecordingDays(5);

         testUser.updateRecordingStreak();
         assertThat(testUser.getConsecutiveRecordingDays()).isEqualTo(6);
    }

    @Test
    @DisplayName("성공: 며칠 전 녹음 후 오늘 녹음하면 연속 녹음 일수가 1로 초기화된다")
    void updateRecordingStreak_ResetStreak() {
         testUser.setLastRecordingDate(LocalDate.now().minusDays(3));
         testUser.setConsecutiveRecordingDays(5);

         testUser.updateRecordingStreak();

         assertThat(testUser.getConsecutiveRecordingDays()).isEqualTo(1);
         assertThat(testUser.getLastRecordingDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("성공: 오늘 여러 번 녹음해도 연속 녹음 일수는 변하지 않는다")
    void updateRecordingStreak_MultipleTimesInOneDay() {
        testUser.updateRecordingStreak();
        testUser.updateRecordingStreak();
        testUser.updateRecordingStreak();

        assertThat(testUser.getConsecutiveRecordingDays()).isEqualTo(1);
        assertThat(testUser.getLastRecordingDate()).isEqualTo(LocalDate.now());
    }
}