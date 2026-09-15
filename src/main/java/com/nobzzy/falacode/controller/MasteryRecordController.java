package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.MasteryRecordDto;
import com.nobzzy.falacode.service.MasteryRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MasteryRecordController {

    private final MasteryRecordService masteryRecordService;

    public MasteryRecordController(MasteryRecordService masteryRecordService) {
        this.masteryRecordService = masteryRecordService;
    }

    @PostMapping("/users/{userId}/mastery-records")
    public ResponseEntity<MasteryRecordDto> createMasteryRecord(@PathVariable Long userId, @Valid @RequestBody MasteryRecordDto dto) {
        return new ResponseEntity<>(masteryRecordService.createMasteryRecord(userId, dto), HttpStatus.CREATED);
    }

    @GetMapping("/mastery-records")
    public ResponseEntity<List<MasteryRecordDto>> getAllMasteryRecords() {
        return new ResponseEntity<>(masteryRecordService.getAllMasteryRecords(), HttpStatus.OK);
    }

    @GetMapping("/mastery-records/{id}")
    public ResponseEntity<MasteryRecordDto> getMasteryRecordById(@PathVariable Long id) {
        return new ResponseEntity<>(masteryRecordService.getMasteryRecordById(id), HttpStatus.OK);
    }

    @GetMapping("/users/{userId}/mastery-records")
    public ResponseEntity<List<MasteryRecordDto>> getMasteryRecordsByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(masteryRecordService.getMasteryRecordsByUserId(userId), HttpStatus.OK);
    }

    @PutMapping("/mastery-records/{id}")
    public ResponseEntity<MasteryRecordDto> updateMasteryRecord(@PathVariable Long id, @Valid @RequestBody MasteryRecordDto dto) {
        return new ResponseEntity<>(masteryRecordService.updateMasteryRecord(id, dto), HttpStatus.OK);
    }

    @PostMapping("/users/{userId}/mastery-records/progress")
    public ResponseEntity<MasteryRecordDto> recordProgress(@PathVariable Long userId, @RequestParam String topic, @RequestParam Integer score) {
        return new ResponseEntity<>(masteryRecordService.recordProgress(userId, topic, score), HttpStatus.OK);
    }

    @DeleteMapping("/mastery-records/{id}")
    public ResponseEntity<Void> deleteMasteryRecord(@PathVariable Long id) {
        masteryRecordService.deleteMasteryRecordById(id);
        return ResponseEntity.noContent().build();
    }
}