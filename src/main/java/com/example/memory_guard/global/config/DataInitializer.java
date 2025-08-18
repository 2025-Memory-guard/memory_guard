package com.example.memory_guard.global.config;

import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import com.example.memory_guard.user.domain.repository.RoleRepository;
import com.example.memory_guard.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {
    Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() ->
        roleRepository.save(Role.builder().name("ROLE_USER").build())
    );
    Role guardRole = roleRepository.findByName("ROLE_GUARD").orElseGet(() ->
        roleRepository.save(Role.builder().name("ROLE_GUARD").build())
    );

    if (userRepository.findByUserProfileUserId("guard1").isEmpty()) {
      UserProfile guardianProfile = UserProfile.builder()
          .userId("guard1")
          .username("가디언1")
          .password(passwordEncoder.encode("guard1"))
          .build();
      User guardian = User.builder()
          .userProfile(guardianProfile)
          .build();
      guardian.addRole(guardRole);
      userRepository.save(guardian);

      if (userRepository.findByUserProfileUserId("user1").isEmpty()) {
        UserProfile userProfile = UserProfile.builder()
            .userId("user1")
            .username("사용자1")
            .password(passwordEncoder.encode("user1"))
            .build();
        User user = User.builder()
            .userProfile(userProfile)
            .build();
        user.addRole(userRole);

        guardian.addWard(user);

        userRepository.save(user);
      }
    }
  }
}