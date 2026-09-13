package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ExerciseDto;
import com.nobzzy.falacode.entity.Exercise;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.ExerciseRepository;
import com.nobzzy.falacode.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;

    public ExerciseService(ExerciseRepository exerciseRepository, LessonRepository lessonRepository) {
        this.exerciseRepository = exerciseRepository;
        this.lessonRepository = lessonRepository;
    }

    // CREATE
    @Transactional
    public ExerciseDto createExercise(ExerciseDto exerciseDto) {
        Lesson lesson = lessonRepository.findById(exerciseDto.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + exerciseDto.getLessonId()));

        Exercise exercise = mapToEntity(exerciseDto);
        exercise.setLesson(lesson);

        Exercise savedExercise = exerciseRepository.save(exercise);
        return mapToDto(savedExercise);
    }

    // READ ALL
    public List<ExerciseDto> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public ExerciseDto getExerciseById(Long id) {
        return mapToDto(exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id)));
    }

    // READ BY LESSON ID
    public List<ExerciseDto> getExercisesByLessonId(Long lessonId) {
        return exerciseRepository.findByLessonId(lessonId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // UPDATE
    @Transactional
    public ExerciseDto updateExercise(Long id, ExerciseDto exerciseDto) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));

        exercise.setTitle(exerciseDto.getTitle());
        exercise.setInstructions(exerciseDto.getInstructions());
        exercise.setStarterCode(exerciseDto.getStarterCode());
        exercise.setSolutionCode(exerciseDto.getSolutionCode());
        exercise.setDifficulty(exerciseDto.getDifficulty());
        exercise.setDisplayOrder(exerciseDto.getDisplayOrder());
        exercise.setPoints(exerciseDto.getPoints());

        Exercise updatedExercise = exerciseRepository.save(exercise);
        return mapToDto(updatedExercise);
    }

    // DELETE
    @Transactional
    public void deleteExerciseById(Long id) {
        if(!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise not found with id: " + id);
        }

        exerciseRepository.deleteById(id);
    }

    // MAPPERS
    private Exercise mapToEntity(ExerciseDto exerciseDto) {
        return Exercise.builder()
                .id(exerciseDto.getId())
                .title(exerciseDto.getTitle())
                .instructions(exerciseDto.getInstructions())
                .starterCode(exerciseDto.getStarterCode())
                .solutionCode(exerciseDto.getSolutionCode())
                .difficulty(exerciseDto.getDifficulty())
                .displayOrder(exerciseDto.getDisplayOrder())
                .points(exerciseDto.getPoints())
                .createdAt(exerciseDto.getCreatedAt())
                .updatedAt(exerciseDto.getUpdatedAt())
                .build();
    }

    private ExerciseDto mapToDto(Exercise exercise) {
        return ExerciseDto.builder()
                .id(exercise.getId())
                .title(exercise.getTitle())
                .instructions(exercise.getInstructions())
                .starterCode(exercise.getStarterCode())
                .solutionCode(exercise.getSolutionCode())
                .difficulty(exercise.getDifficulty())
                .displayOrder(exercise.getDisplayOrder())
                .points(exercise.getPoints())
                .lessonId(exercise.getLesson() != null ? exercise.getLesson().getId() : null)
                .build();
    }
}
