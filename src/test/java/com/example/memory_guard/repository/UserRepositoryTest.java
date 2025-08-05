package com.example.memory_guard.repository;

import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.repository.RoleRepository;
import com.example.memory_guard.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role roleUser;
    private Role roleGuard;

    @BeforeEach
    void setUp() {

        Role userRoleEntity = Role.builder().name("ROLE_USER").build();
        Role adminRoleEntity = Role.builder().name("ROLE_ADMIN").build();

        this.roleUser = entityManager.persist(userRoleEntity);
        this.roleGuard = entityManager.persist(adminRoleEntity);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("userId로 회원 조회 - 존재하는 회원")
    void findByUserIdExistingUserTest() {
        // given
        String userId = "testUser";
        User member = User.builder()
            .userId(userId)
            .username("테스트사용자")
            .password("password123")
            .build();
        member.addRole(roleUser);

        entityManager.persist(member);
        entityManager.flush();

        Optional<User> foundUserOpt = userRepository.findByUserId(userId);

        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();

        assertThat(foundUser.getUserId()).isEqualTo(userId);
        assertThat(foundUser.getPassword()).isEqualTo("password123");

        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles()).extracting("name").containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("회원 저장 테스트")
    void saveUserTest() {
        User member = User.builder()
            .userId("newUser")
            .username("새사용자")
            .password("newPassword")
            .build();
        member.addRole(roleUser);
        member.addRole(roleGuard);

        User savedUser = userRepository.save(member);
        entityManager.flush();

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUserId()).isEqualTo("newUser");

        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser.getRoles()).hasSize(2);
        assertThat(foundUser.getRoles()).extracting("name").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("ID로 회원 조회 테스트")
    void findByIdTest() {
        User member = User.builder()
            .userId("user123")
            .username("사용자123")
            .password("pass123")
            .build();
        member.addRole(roleUser);

        User savedUser = entityManager.persist(member);
        entityManager.flush();
        Long userId = savedUser.getId();

        Optional<User> foundUserOpt = userRepository.findById(userId);

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getId()).isEqualTo(userId);
        assertThat(foundUserOpt.get().getUserId()).isEqualTo("user123");
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    void deleteUserTest() {
        User member = User.builder()
            .userId("deleteUser")
            .username("삭제사용자")
            .password("deletePass")
            .build();
        member.addRole(roleUser);

        User savedUser = entityManager.persist(member);
        entityManager.flush();
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isNotPresent();

        Optional<Role> userRoleAfterDelete = roleRepository.findByName("ROLE_USER");
        assertThat(userRoleAfterDelete).isPresent();
    }

    @Test
    @DisplayName("모든 회원 조회 테스트")
    void findAllUsersTest() {
        User member1 = User.builder().userId("user1").username("사용자1").password("pass1").build();
        member1.addRole(roleUser);

        User member2 = User.builder().userId("user2").username("사용자2").password("pass2").build();
        member2.addRole(roleGuard);

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting("userId").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("중복 userId 저장 시 예외 발생 테스트")
    void saveDuplicateUserIdTest() {
        User member1 = User.builder().userId("duplicateUser").username("중복사용자1").password("pass1").build();
        member1.addRole(roleUser);
        userRepository.saveAndFlush(member1);

        User member2 = User.builder().userId("duplicateUser").username("중복사용자2").password("pass2").build();
        member2.addRole(roleGuard);

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(member2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    @DisplayName("회원 정보 업데이트 테스트")
    void updateUserTest() {
        User member = User.builder()
            .userId("updateUser")
            .username("업데이트사용자")
            .password("originalPass")
            .build();
        member.addRole(roleUser);

        entityManager.persistAndFlush(member);
        entityManager.clear();

        User foundUser = userRepository.findByUserId("updateUser").get();
        foundUser.addRole(roleGuard);

        userRepository.saveAndFlush(foundUser);
        entityManager.clear();

        User updatedUser = userRepository.findById(foundUser.getId()).get();
        assertThat(updatedUser.getPassword()).isEqualTo("originalPass");
        assertThat(updatedUser.getRoles()).hasSize(2);
        assertThat(updatedUser.getRoles()).extracting("name")
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("성공: 보호자와 피보호자 관계를 저장하고 올바르게 조회한다")
    void should_save_and_find_user_with_guardian_relationship() {
        User guardian = User.builder()
            .userId("guardian_test")
            .username("가디언테스트")
            .password("pass_g")
            .build();
        guardian.addRole(roleGuard);

        User ward = User.builder()
            .userId("ward_test")
            .username("피보호자테스트")
            .password("pass_w")
            .build();
        ward.addRole(roleUser);

        guardian.addWard(ward);

        entityManager.persist(guardian);
        entityManager.persist(ward);
        entityManager.flush();
        entityManager.clear();

        User foundWard = userRepository.findByUserId("ward_test").orElse(null);

        User foundGuardian = userRepository.findByUserId("guardian_test").orElse(null);

        assertThat(foundWard).isNotNull();
        assertThat(foundGuardian).isNotNull();

        assertThat(foundWard.getGuardian()).isNotNull();
        assertThat(foundWard.getGuardian().getId()).isEqualTo(foundGuardian.getId());

        assertThat(foundGuardian.getWards()).hasSize(1);
        assertThat(foundGuardian.getWards().get(0).getId()).isEqualTo(foundWard.getId());
    }

    @Test
    @DisplayName("username으로 회원 조회 - 존재하는 회원")
    void findByUsernameExistingUserTest() {
        // given
        String username = "테스트유저명";
        User member = User.builder()
            .userId("testUserId")
            .username(username)
            .password("password123")
            .build();
        member.addRole(roleUser);

        entityManager.persist(member);
        entityManager.flush();

        Optional<User> foundUserOpt = userRepository.findByUsername(username);

        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();

        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getUserId()).isEqualTo("testUserId");
    }

    @Test
    @DisplayName("username 존재 여부 확인 테스트")
    void existsByUsernameTest() {
        // given
        String username = "존재하는유저명";
        User member = User.builder()
            .userId("existUser")
            .username(username)
            .password("password123")
            .build();
        member.addRole(roleUser);

        entityManager.persist(member);
        entityManager.flush();

        boolean exists = userRepository.existsByUsername(username);
        boolean notExists = userRepository.existsByUsername("존재하지않는유저명");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("중복 username 저장 시 예외 발생 테스트")
    void saveDuplicateUsernameTest() {
        User member1 = User.builder()
            .userId("user1")
            .username("중복유저명")
            .password("pass1")
            .build();
        member1.addRole(roleUser);
        userRepository.saveAndFlush(member1);

        User member2 = User.builder()
            .userId("user2")
            .username("중복유저명")
            .password("pass2")
            .build();
        member2.addRole(roleGuard);

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(member2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}