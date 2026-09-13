package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.ExerciseDto;
import com.nobzzy.falacode.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    // CREATE
    @PostMapping("/exercises")
    public ResponseEntity<ExerciseDto> createExercise(@Valid @RequestBody ExerciseDto exerciseDto) {
        return new ResponseEntity<>(exerciseService.createExercise(exerciseDto), HttpStatus.CREATED);
    }

    // READ ALL
    @GetMapping("/exercises")
    public ResponseEntity<List<ExerciseDto>> getAllExercises() {
        return new ResponseEntity<>(exerciseService.getAllExercises(), HttpStatus.OK);
    }

    // READ BY ID
    @GetMapping("/exercises/{id}")
    public ResponseEntity<ExerciseDto> getExerciseById(@PathVariable Long id) {
        return new ResponseEntity<>(exerciseService.getExerciseById(id), HttpStatus.OK);
    }

    // READ BY LESSON ID
    @GetMapping("/lessons/{lessonId}/exercises")
    public ResponseEntity<Iterable<ExerciseDto>> getExercisesByLessonId(@PathVariable Long lessonId) {
        return new ResponseEntity<>(exerciseService.getExercisesByLessonId(lessonId), HttpStatus.OK);
    }

    // UPDATE
    @PutMapping("/exercises/{id}")
    public ResponseEntity<ExerciseDto> updateExercise(@PathVariable Long id,@Valid @RequestBody ExerciseDto exerciseDto) {
        return new ResponseEntity<>(exerciseService.updateExercise(id, exerciseDto), HttpStatus.OK);
    }

    // DELETE
    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExerciseById(id);
        return ResponseEntity.noContent().build();
    }
}
