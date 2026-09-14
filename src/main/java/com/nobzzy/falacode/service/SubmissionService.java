package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.SubmissionDto;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Submission;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ExerciseRepository exerciseRepository;

    public SubmissionService(SubmissionRepository submissionRepository, ExerciseRepository exerciseRepository) {
        this.submissionRepository = submissionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // CREATE
    @Transactional
    public SubmissionDto createSubmission(Long exerciseId, SubmissionDto submissionDto) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + exerciseId));

        Submission submission = mapToEntity(submissionDto);
        submission.setExercise(exercise);

        if (submission.getStatus() == null) {
            submission.setStatus("PENDING");
        }

        Submission savedSubmission = submissionRepository.save(submission);
        return mapToDto(savedSubmission);
    }

    // READ ALL
    public List<SubmissionDto> getAllSubmissions() {
        return submissionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ ALL BY EXERCISE ID
    public List<SubmissionDto> getSubmissionsByExerciseId(Long exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ResourceNotFoundException("Exercise not found with id: " + exerciseId);
        }

        return submissionRepository.findByExerciseId(exerciseId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public SubmissionDto getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));
        return mapToDto(submission);
    }

    // UPDATE
    @Transactional
    public SubmissionDto updateSubmission(Long id, SubmissionDto submissionDto) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        submission.setCode(submissionDto.getCode());
        submission.setStatus(submissionDto.getStatus());
        submission.setScore(submissionDto.getScore());
        submission.setFeedback(submissionDto.getFeedback());

        Submission updatedSubmission = submissionRepository.save(submission);
        return mapToDto(updatedSubmission);
    }

    // DELETE
    @Transactional
    public void deleteSubmissionById(Long id) {
        if (!submissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Submission not found with id: " + id);
        }
        submissionRepository.deleteById(id);
    }

    Submission mapToEntity(SubmissionDto submissionDto) {
        return Submission.builder()
                .id(submissionDto.getId())
                .code(submissionDto.getCode())
                .status(submissionDto.getStatus())
                .score(submissionDto.getScore())
                .feedback(submissionDto.getFeedback())
                .build();
    }

    SubmissionDto mapToDto(Submission submission) {
        return SubmissionDto.builder()
                .id(submission.getId())
                .code(submission.getCode())
                .status(submission.getStatus())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .exerciseId(submission.getExercise() != null ? submission.getExercise().getId() : null)
                .createdAt(submission.getCreatedAt())
                .build();
    }
}