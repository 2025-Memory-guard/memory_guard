package com.example.memory_guard.global.config;

import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
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

    if (userRepository.findByUserId("guard1").isEmpty()) {
      User guardian = User.builder()
          .userId("guard1")
          .username("가디언1")
          .password(passwordEncoder.encode("guard1"))
          .build();
      guardian.addRole(guardRole);
      userRepository.save(guardian);

      if (userRepository.findByUserId("user1").isEmpty()) {
        User user = User.builder()
            .userId("user1")
            .username("사용자1")
            .password(passwordEncoder.encode("user1"))
            .build();
        user.addRole(userRole);

        guardian.addWard(user);

        userRepository.save(user);
      }
    }
  }
}