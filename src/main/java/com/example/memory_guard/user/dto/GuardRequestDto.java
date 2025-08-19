package com.example.memory_guard.user.dto;

import com.example.memory_guard.user.domain.GuardRequest;
import com.example.memory_guard.user.domain.Status;
import com.example.memory_guard.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuardRequestDto {
    private Long id;
    private Long requesterId;
    private String requesterUserId;
    private Long receiverId;
    private String receiverUserId;
    private Status status;

    public static GuardRequestDto fromEntity(GuardRequest request) {
        return GuardRequestDto.builder()
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterUserId(request.getRequester().getUserProfile().getUserId())
                .receiverId(request.getReceiver().getId())
                .receiverUserId(request.getReceiver().getUserProfile().getUserId())
                .status(request.getStatus())
                .build();
    }

    public static GuardRequest toEntity(User requester, User receiver) {
        return GuardRequest.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Status.PENDING)
                .build();
    }
}
