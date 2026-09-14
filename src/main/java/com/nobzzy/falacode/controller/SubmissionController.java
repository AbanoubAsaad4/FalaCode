package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.SubmissionDto;
import com.nobzzy.falacode.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // CREATE
    @PostMapping("/submissions")
    public ResponseEntity<SubmissionDto> createSubmission(@Valid @RequestBody SubmissionDto submissionDto) {
        return new ResponseEntity<>(submissionService.createSubmission(submissionDto), HttpStatus.CREATED);
    }

    // READ ALL
    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionDto>> getAllSubmissions() {
        return ResponseEntity.ok(submissionService.getAllSubmissions());
    }

    // READ ALL BY EXERCISE ID
    @GetMapping("/exercises/{exerciseId}/submissions")
    public ResponseEntity<List<SubmissionDto>> getSubmissionsByExerciseId(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByExerciseId(exerciseId));
    }

    // READ BY ID
    @GetMapping("/submissions/{id}")
    public ResponseEntity<SubmissionDto> getSubmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmissionById(id));
    }

    // UPDATE
    @PutMapping("/submissions/{id}")
    public ResponseEntity<SubmissionDto> updateSubmission(@Valid @RequestBody SubmissionDto submissionDto, @PathVariable Long id) {
        return ResponseEntity.ok(submissionService.updateSubmission(id, submissionDto));
    }

    // DELETE
    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        submissionService.deleteSubmissionById(id);
        return ResponseEntity.noContent().build();
    }
}