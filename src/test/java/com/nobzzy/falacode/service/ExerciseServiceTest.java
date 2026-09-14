package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ExerciseDto;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private Lesson testLesson;
    private Exercise testExercise;
    private ExerciseDto testExerciseDto;

    @BeforeEach
    void setUp() {
        testLesson = Lesson.builder()
                .id(1L)
                .title("Classes & Objects")
                .exercises(new ArrayList<>())
                .build();

        testExercise = Exercise.builder()
                .id(100L)
                .title("Create a Car Class")
                .instructions("Define a class named Car.")
                .starterCode("public class Car {}")
                .solutionCode("public class Car { private String color; }")
                .difficulty("EASY")
                .displayOrder(1)
                .points(10)
                .lesson(testLesson)
                .build();

        testExerciseDto = ExerciseDto.builder()
                .id(100L)
                .title("Create a Car Class")
                .instructions("Define a class named Car.")
                .starterCode("public class Car {}")
                .solutionCode("public class Car { private String color; }")
                .difficulty("EASY")
                .displayOrder(1)
                .points(10)
                .lessonId(1L)
                .build();
    }

    @Nested
    @DisplayName("Create Exercise Operations")
    class CreateExerciseTests {

        @Test
        @DisplayName("Should create exercise successfully when lesson exists")
        void shouldCreateExercise_WhenLessonExists() {
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(testLesson));
            when(exerciseRepository.save(any(Exercise.class))).thenReturn(testExercise);

            ExerciseDto createdDto = exerciseService.createExercise(1L, testExerciseDto);

            assertThat(createdDto).isNotNull();
            assertThat(createdDto.getTitle()).isEqualTo("Create a Car Class");
            assertThat(createdDto.getLessonId()).isEqualTo(1L);
            verify(lessonRepository, times(1)).findById(1L);
            verify(exerciseRepository, times(1)).save(any(Exercise.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lesson does not exist")
        void shouldThrowException_WhenLessonNotFound() {
            when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

            testExerciseDto.setLessonId(99L);

            assertThatThrownBy(() -> exerciseService.createExercise(99L, testExerciseDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lesson not found with id: 99");

            verify(exerciseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read Exercise Operations")
    class ReadExerciseTests {

        @Test
        @DisplayName("Should return all exercises")
        void shouldReturnAllExercises() {
            when(exerciseRepository.findAll()).thenReturn(List.of(testExercise));

            List<ExerciseDto> result = exerciseService.getAllExercises();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo(testExercise.getTitle());
            verify(exerciseRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return exercise by ID when found")
        void shouldReturnExerciseById_WhenFound() {
            when(exerciseRepository.findById(100L)).thenReturn(Optional.of(testExercise));

            ExerciseDto result = exerciseService.getExerciseById(100L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            verify(exerciseRepository, times(1)).findById(100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when exercise ID not found")
        void shouldThrowException_WhenExerciseNotFound() {
            when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> exerciseService.getExerciseById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Exercise not found with id: 999");
        }

        @Test
        @DisplayName("Should return exercises by lesson ID")
        void shouldReturnExercisesByLessonId() {
            when(exerciseRepository.findByLessonId(1L)).thenReturn(List.of(testExercise));

            List<ExerciseDto> result = exerciseService.getExercisesByLessonId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLessonId()).isEqualTo(1L);
            verify(exerciseRepository, times(1)).findByLessonId(1L);
        }
    }

    @Nested
    @DisplayName("Update Exercise Operations")
    class UpdateExerciseTests {

        @Test
        @DisplayName("Should update exercise successfully")
        void shouldUpdateExercise_WhenExerciseExists() {
            when(exerciseRepository.findById(100L)).thenReturn(Optional.of(testExercise));
            when(exerciseRepository.save(any(Exercise.class))).thenReturn(testExercise);

            ExerciseDto updateDto = ExerciseDto.builder()
                    .title("Updated Title")
                    .instructions("Updated Instructions")
                    .starterCode("int x = 5;")
                    .solutionCode("int x = 10;")
                    .difficulty("HARD")
                    .displayOrder(2)
                    .points(20)
                    .lessonId(1L)
                    .build();

            ExerciseDto updatedResult = exerciseService.updateExercise(100L, updateDto);

            assertThat(updatedResult).isNotNull();
            verify(exerciseRepository, times(1)).findById(100L);
            verify(exerciseRepository, times(1)).save(testExercise);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent exercise")
        void shouldThrowException_WhenUpdatingNonExistentExercise() {
            when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> exerciseService.updateExercise(999L, testExerciseDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Exercise not found with id: 999");

            verify(exerciseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Exercise Operations")
    class DeleteExerciseTests {

        @Test
        @DisplayName("Should delete exercise when ID exists")
        void shouldDeleteExercise_WhenExists() {
            when(exerciseRepository.existsById(100L)).thenReturn(true);
            doNothing().when(exerciseRepository).deleteById(100L);

            exerciseService.deleteExerciseById(100L);

            verify(exerciseRepository, times(1)).existsById(100L);
            verify(exerciseRepository, times(1)).deleteById(100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent exercise")
        void shouldThrowException_WhenDeletingNonExistentExercise() {
            when(exerciseRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> exerciseService.deleteExerciseById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Exercise not found with id: 999");

            verify(exerciseRepository, never()).deleteById(any());
        }
    }
}