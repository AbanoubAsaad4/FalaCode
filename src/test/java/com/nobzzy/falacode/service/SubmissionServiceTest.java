package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.SubmissionDto;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Submission;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private Exercise sampleExercise;
    private Submission sampleSubmission;
    private SubmissionDto sampleSubmissionDto;

    @BeforeEach
    void setUp() {
        sampleExercise = Exercise.builder()
                .id(20L)
                .title("Two Sum")
                .build();

        sampleSubmission = Submission.builder()
                .id(200L)
                .code("class Solution {}")
                .status("PASSED")
                .score(100)
                .feedback("All tests passed")
                .exercise(sampleExercise)
                .build();

        sampleSubmissionDto = SubmissionDto.builder()
                .id(200L)
                .code("class Solution {}")
                .status("PASSED")
                .score(100)
                .feedback("All tests passed")
                .exerciseId(20L)
                .build();
    }

    @Nested
    @DisplayName("Create Submission Tests")
    class CreateSubmissionTests {

        @Test
        @DisplayName("Should create and return SubmissionDto when exercise exists")
        void createSubmission_Success() {
            when(exerciseRepository.findById(20L)).thenReturn(Optional.of(sampleExercise));
            when(submissionRepository.save(any(Submission.class))).thenReturn(sampleSubmission);

            SubmissionDto result = submissionService.createSubmission(sampleSubmissionDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(200L);
            assertThat(result.getCode()).isEqualTo("class Solution {}");
            assertThat(result.getExerciseId()).isEqualTo(20L);
            verify(submissionRepository, times(1)).save(any(Submission.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when exercise does not exist")
        void createSubmission_ThrowsException_WhenExerciseNotFound() {
            when(exerciseRepository.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.createSubmission(sampleSubmissionDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Exercise not found with id: 20");

            verify(submissionRepository, never()).save(any(Submission.class));
        }
    }

    @Nested
    @DisplayName("Read Submission Tests")
    class ReadSubmissionTests {

        @Test
        @DisplayName("Should return all submissions")
        void getAllSubmissions_Success() {
            when(submissionRepository.findAll()).thenReturn(List.of(sampleSubmission));

            List<SubmissionDto> results = submissionService.getAllSubmissions();

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getCode()).isEqualTo("class Solution {}");
        }

        @Test
        @DisplayName("Should return submissions by exercise ID when exercise exists")
        void getSubmissionsByExerciseId_Success() {
            when(exerciseRepository.existsById(20L)).thenReturn(true);
            when(submissionRepository.findByExerciseId(20L)).thenReturn(List.of(sampleSubmission));

            List<SubmissionDto> results = submissionService.getSubmissionsByExerciseId(20L);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getExerciseId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when exercise ID does not exist")
        void getSubmissionsByExerciseId_ThrowsException_WhenExerciseNotFound() {
            when(exerciseRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> submissionService.getSubmissionsByExerciseId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Exercise not found with id: 99");

            verify(submissionRepository, never()).findByExerciseId(anyLong());
        }

        @Test
        @DisplayName("Should return submission by ID when submission exists")
        void getSubmissionById_Success() {
            when(submissionRepository.findById(200L)).thenReturn(Optional.of(sampleSubmission));

            SubmissionDto result = submissionService.getSubmissionById(200L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when submission ID does not exist")
        void getSubmissionById_ThrowsException_WhenNotFound() {
            when(submissionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.getSubmissionById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Update Submission Tests")
    class UpdateSubmissionTests {

        @Test
        @DisplayName("Should update and return updated SubmissionDto when submission exists")
        void updateSubmission_Success() {
            SubmissionDto updateRequest = SubmissionDto.builder()
                    .code("class Solution { public void main() {} }")
                    .status("FAILED")
                    .score(50)
                    .feedback("Syntax error")
                    .exerciseId(20L)
                    .build();

            Submission updatedSubmission = Submission.builder()
                    .id(200L)
                    .code("class Solution { public void main() {} }")
                    .status("FAILED")
                    .score(50)
                    .feedback("Syntax error")
                    .exercise(sampleExercise)
                    .build();

            when(submissionRepository.findById(200L)).thenReturn(Optional.of(sampleSubmission));
            when(submissionRepository.save(any(Submission.class))).thenReturn(updatedSubmission);

            SubmissionDto result = submissionService.updateSubmission(200L, updateRequest);

            assertThat(result.getCode()).isEqualTo("class Solution { public void main() {} }");
            assertThat(result.getStatus()).isEqualTo("FAILED");
            assertThat(result.getScore()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent submission")
        void updateSubmission_ThrowsException_WhenNotFound() {
            when(submissionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.updateSubmission(999L, sampleSubmissionDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found with id: 999");

            verify(submissionRepository, never()).save(any(Submission.class));
        }
    }

    @Nested
    @DisplayName("Delete Submission Tests")
    class DeleteSubmissionTests {

        @Test
        @DisplayName("Should delete submission when ID exists")
        void deleteSubmissionById_Success() {
            when(submissionRepository.existsById(200L)).thenReturn(true);

            submissionService.deleteSubmissionById(200L);

            verify(submissionRepository, times(1)).deleteById(200L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent submission")
        void deleteSubmissionById_ThrowsException_WhenNotFound() {
            when(submissionRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> submissionService.deleteSubmissionById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found with id: 999");

            verify(submissionRepository, never()).deleteById(anyLong());
        }
    }
}