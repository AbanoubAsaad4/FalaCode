package com.nobzzy.falacode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotBlank(message = "Sender is required")
    @Size(max = 20, message = "Sender type cannot exceed 20 characters")
    private String sender;

    @NotNull(message = "Chat Session ID is required")
    private Long chatSessionId;

    private LocalDateTime createdAt;
}