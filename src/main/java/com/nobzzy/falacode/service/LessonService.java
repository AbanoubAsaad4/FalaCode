package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.LessonDto;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.LessonRepository;
import com.nobzzy.falacode.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    public LessonService(LessonRepository lessonRepository, ModuleRepository moduleRepository) {
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
    }

    // CREATE
    @Transactional
    public LessonDto createLesson(LessonDto lessonDto) {
        Module module = moduleRepository.findById(lessonDto.getModuleId()).
                orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + lessonDto.getModuleId()));

        Lesson lesson = mapToEntity(lessonDto);
        lesson.setModule(module);

        Lesson savedLesson = lessonRepository.save(lesson);
        return mapToDto(savedLesson);
    }

    // READ ALL
    public List<LessonDto> getAllLessons() {
        return lessonRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    //READ ALL BY MODULE ID
    public List<LessonDto> getLessonsByModuleId(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module not found with id: " + id);
        }
        
        return lessonRepository.findByModuleId(id).stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
        return mapToDto(lesson);
    }

    // UPDATE
    @Transactional
    public LessonDto updateLesson(Long id, LessonDto lessonDto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        lesson.setTitle(lessonDto.getTitle());
        lesson.setDisplayOrder(lessonDto.getDisplayOrder());
        lesson.setPublished(lessonDto.getPublished());

        Lesson updatedLesson = lessonRepository.save(lesson);
        return mapToDto(updatedLesson);
    }

    // DELETE
    @Transactional
    public void deleteLessonById(Long id) {
        if(!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + id);
        }
        lessonRepository.deleteById(id);
    }

    Lesson mapToEntity(LessonDto lessonDto) {
        return Lesson.builder()
                .id(lessonDto.getId())
                .title(lessonDto.getTitle())
                .displayOrder(lessonDto.getDisplayOrder())
                .isPublished(lessonDto.getPublished())
                .build();
    }

    LessonDto mapToDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .displayOrder(lesson.getDisplayOrder())
                .published(lesson.isPublished())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
