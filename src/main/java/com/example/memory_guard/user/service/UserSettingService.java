package com.example.memory_guard.user.service;

import com.example.memory_guard.user.domain.GuardRequest;
import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardManagementResponseDto;
import com.example.memory_guard.user.dto.GuardRequestDto;
import com.example.memory_guard.user.dto.GuardUserDto;
import com.example.memory_guard.user.repository.GuardRequestRepository;
import com.example.memory_guard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserSettingService {

    private final UserRepository userRepository;
    private final GuardRequestRepository guardRequestRepository;

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

    public void sendGuardRequest(User ward, GuardRequestDto guardRequestDto) {
        User guard = userRepository.findByUserProfileUserId(guardRequestDto.getReceiverUserId())
                .orElseThrow(() -> new IllegalArgumentException("요청 대상 보호자를 찾을 수 없습니다."));

        GuardRequest guardRequest = GuardRequestDto.toEntity(guard, ward);
        ward.getReceivedRequests().add(guardRequest);
        guard.getSentRequests().add(guardRequest);

        guardRequestRepository.save(guardRequest);
    }
}
