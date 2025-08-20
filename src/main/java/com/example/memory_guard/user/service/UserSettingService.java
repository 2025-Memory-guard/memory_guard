package com.example.memory_guard.user.service;

import com.example.memory_guard.user.domain.GuardRequest;
import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.Status;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardManagementResponseDto;
import com.example.memory_guard.user.dto.GuardRequestDto;
import com.example.memory_guard.user.dto.GuardUserDto;
import com.example.memory_guard.user.repository.GuardRequestRepository;
import com.example.memory_guard.user.repository.GuardUserLinkRepository;
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
    private final GuardUserLinkRepository guardUserLinkRepository;

    public List<GuardUserDto> getAllGuards(User ward) {
        User persistUser = userRepository
            .findById(ward.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));
        return persistUser.getGuardians().stream()
                .map(GuardUserDto::fromEntity)
                .toList();
    }

    public GuardManagementResponseDto getManagement(User ward) {
        User persistUser = userRepository
            .findById(ward.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        return GuardManagementResponseDto.fromEntity(
                persistUser.getGuardians(),
                persistUser.getReceivedRequests(),
                persistUser.getSentRequests()
        );
    }

    public Optional<GuardUserDto> getGuard(String userId) {
        return userRepository.findByUserProfileUserId(userId)
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_GUARD")))
            .map(GuardUserDto::fromEntity);
    }

    public void sendGuardRequest(User ward, GuardRequestDto guardRequestDto) {
        User guard = userRepository.findByUserProfileUserId(guardRequestDto.getReceiverUserId())
                .orElseThrow(() -> new IllegalArgumentException("요청 대상 보호자를 찾을 수 없습니다."));

        User persistWard = userRepository
            .findById(ward.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        GuardRequest guardRequest = GuardRequestDto.toEntity(ward, guard);
        guard.getReceivedRequests().add(guardRequest);
        persistWard.getSentRequests().add(guardRequest);

        guardRequestRepository.save(guardRequest);
    }

    public void updateRequestStatus(Long requestId, Status status) {
        GuardRequest request = guardRequestRepository.findGuardRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수가 없습니다"));

        User guard = request.getRequester();
        User ward = request.getReceiver();

        //요청이 거절되었을 떄
        if (status == Status.REJECTED) {
            guard.getSentRequests().remove(request);
            ward.getReceivedRequests().remove(request);
            guardRequestRepository.delete(request);
        }

        //요청 수락되었을 떄
        if (status == Status.ACCEPTED) {
            // 수락을 하면 User와 Guard둘다 설정이
            GuardUserLink guardUserLink = guard.addWard(ward);
            guardUserLinkRepository.save(guardUserLink);

            ward.getSentRequests().remove(request);
            guard.getReceivedRequests().remove(request);
            guardRequestRepository.delete(request);
        }
    }
}
