package com.example.memory_guard.user.service;

import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardManagementResponseDto;
import com.example.memory_guard.user.dto.GuardUserDto;
import com.example.memory_guard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserSettingService {

    private final UserRepository userRepository;

    public List<GuardUserDto> getAllGuards(User ward) {
        return ward.getGuardians().stream()
                .map(GuardUserDto::fromEntity)
                .toList();
    }

    public GuardManagementResponseDto getManagement(User ward) {
        return GuardManagementResponseDto.fromEntity(
                ward.getGuardians(),
                ward.getReceivedRequests(),
                ward.getSentRequests()
        );
    }

    public Optional<User> getGuard(String userId) {
        return userRepository.findByUserProfileUserId(userId)
                .filter(user -> user.getRoles().contains("ROLE_GUARD"));
    }
}
