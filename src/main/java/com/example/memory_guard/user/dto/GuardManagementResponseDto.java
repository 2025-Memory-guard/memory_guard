package com.example.memory_guard.user.dto;

import com.example.memory_guard.user.domain.GuardRequest;
import com.example.memory_guard.user.domain.GuardUserLink;
import lombok.Builder;

import java.util.List;

@Builder
public class GuardManagementResponseDto {
    //현재 연결된 모든 보호자
    private List<GuardUserDto> allGuards;
    //받은 요청 list
    private List<GuardRequestDto> allReceivedRequests;
    //보낸 요청 list
    private List<GuardRequestDto> allSentRequests;

    public static GuardManagementResponseDto fromEntity(List<GuardUserLink> allGuards, List<GuardRequest> allReceivedRequests, List<GuardRequest> allSentRequests) {
        List<GuardUserDto> guardsDto = allGuards.stream()
                .map(GuardUserDto::fromEntity)
                .toList();

        List<GuardRequestDto> receivedRequestDtos = allReceivedRequests.stream()
                .map(GuardRequestDto::fromEntity)
                .toList();

        List<GuardRequestDto> sentRequestDtos = allSentRequests.stream()
                .map(GuardRequestDto::fromEntity)
                .toList();

        return GuardManagementResponseDto.builder()
                .allGuards(guardsDto)
                .allReceivedRequests(receivedRequestDtos)
                .allSentRequests(sentRequestDtos)
                .build();
    }
}
