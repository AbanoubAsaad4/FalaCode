package com.nobzzy.falacode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDto {
    private Long id;

    @NotBlank(message = "Code submission cannot be empty")
    private String code;

    private String status;
    private Integer score;
    private String feedback;

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    private LocalDateTime createdAt;
}