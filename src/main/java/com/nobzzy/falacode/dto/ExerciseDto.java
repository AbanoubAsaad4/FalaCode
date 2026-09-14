package com.nobzzy.falacode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseDto {
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    private String instructions;
    private String starterCode;
    private String solutionCode;
    private String difficulty;
    private Integer displayOrder;
    private Integer points;
    private Long lessonId;
    private List<Long> submissions;
    private List<Long> chatSessions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
