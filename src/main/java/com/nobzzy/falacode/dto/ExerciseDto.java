package com.nobzzy.falacode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

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

    @NotNull(message = "Lesson ID is required")
    private Long lessonId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
