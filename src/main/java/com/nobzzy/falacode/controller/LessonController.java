package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.LessonDto;
import com.nobzzy.falacode.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    // CREATE
    @PostMapping("/lessons")
    public ResponseEntity<LessonDto> createLesson(@Valid @RequestBody LessonDto lessonDto) {
        return new ResponseEntity<>(lessonService.createLesson(lessonDto), HttpStatus.CREATED);
    }

    // READ ALL
    @GetMapping("/lessons")
    public ResponseEntity<List<LessonDto>> getAllLessons() {
        return ResponseEntity.ok(lessonService.getAllLessons());
    }

    // READ ALL BY MODULE ID
    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<List<LessonDto>> getLessonsByModuleId(@PathVariable Long moduleId) {
        return ResponseEntity.ok(lessonService.getLessonsByModuleId(moduleId));
    }

    // READ BY ID
    @GetMapping("/lessons/{id}")
    public ResponseEntity<LessonDto> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    // UPDATE
    @PutMapping("/lessons/{id}")
    public ResponseEntity<LessonDto> updateLesson(@Valid @RequestBody LessonDto lessonDto,@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.updateLesson(id, lessonDto));
    }

    // DELETE
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLessonById(id);
        return ResponseEntity.noContent().build();
    }
}
