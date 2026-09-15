package com.nobzzy.falacode.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasteryRecordDto {
    private Long id;

    @NotBlank(message = "Topic is required")
    @Size(max = 100, message = "Topic cannot exceed 100 characters")
    private String topic;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score cannot be less than 0")
    @Max(value = 100, message = "Score cannot exceed 100")
    private Integer score;

    private LocalDateTime lastReviewed;

    @NotNull(message = "User id is required")
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}