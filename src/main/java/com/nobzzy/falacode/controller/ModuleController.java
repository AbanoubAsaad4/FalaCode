package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.dto.ModuleDto;
import com.nobzzy.falacode.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    // CREATE
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<ModuleDto> createModule(@PathVariable Long courseId, @Valid @RequestBody ModuleDto moduleDto) {
        return new ResponseEntity<>(moduleService.createModule(courseId, moduleDto), HttpStatus.CREATED);
    }

    // READ ALL
    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDto>> getAllModules() {
        return new ResponseEntity<>(moduleService.getAllModules(), HttpStatus.OK);
    }

    // READ ALL BY COURSE ID
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<ModuleDto>> getModulesByCourseId(@PathVariable Long courseId) {
        return new ResponseEntity<>(moduleService.getModulesByCourseId(courseId), HttpStatus.OK);
    }

    // READ BY MODULE ID
    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable Long moduleId) {
        return new ResponseEntity<>(moduleService.getModuleById(moduleId), HttpStatus.OK);
    }

    // UPDATE
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<ModuleDto> updateModule(@PathVariable Long moduleId,@Valid @RequestBody ModuleDto moduleDto) {
        return new ResponseEntity<>(moduleService.updateModule(moduleId, moduleDto), HttpStatus.OK);
    }

    // MOVE MODULE TO COURSE
    @PutMapping("/modules/{moduleId}/move-to-course/{courseId}")
    public ResponseEntity<ModuleDto> moveModuleToCourse(@PathVariable Long moduleId,@PathVariable Long courseId) {
        return new ResponseEntity<>(moduleService.moveModuleToCourse(moduleId, courseId), HttpStatus.OK);
    }

    // DELETE
    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long moduleId) {
        moduleService.deleteModuleById(moduleId);
        return ResponseEntity.noContent().build();
    }
}
