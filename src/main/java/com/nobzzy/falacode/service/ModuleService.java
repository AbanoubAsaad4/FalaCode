package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ModuleDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.CourseRepository;
import com.nobzzy.falacode.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public ModuleService(ModuleRepository moduleRepository,  CourseRepository courseRepository) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
    }

    // CREATE
    @Transactional
    public ModuleDto createModule(Long courseId,ModuleDto moduleDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        Module module = mapToEntity(moduleDto);
        module.setCourse(course);

        Module savedModule = moduleRepository.save(module);
        return mapToDto(savedModule);
    }

    // READ ALL
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ ALL BY COURSE ID
    public List<ModuleDto> getModulesByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        return moduleRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public ModuleDto getModuleById(Long id) {
        return mapToDto(moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + id)));
    }

    // UPDATE
    @Transactional
    public ModuleDto updateModule(Long id,ModuleDto moduleDto) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + id));

        module.setTitle(moduleDto.getTitle());
        module.setDescription(moduleDto.getDescription());
        module.setPublished(moduleDto.isPublished());
        module.setDisplayOrder(moduleDto.getDisplayOrder());

        Module savedModule = moduleRepository.save(module);
        return mapToDto(savedModule);
    }

    // UPDATE MODULE'S COURSE
    @Transactional
    public ModuleDto moveModuleToCourse(Long moduleId,Long courseId) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        Course newCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        module.setCourse(newCourse);
        return mapToDto(moduleRepository.save(module));
    }

    // DELETE
    @Transactional
    public void deleteModuleById(Long id) {
        if(!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module not found with ID: " + id);
        }
        moduleRepository.deleteById(id);
    }

    // MAPPERS
    private Module mapToEntity(ModuleDto moduleDto) {
        return Module.builder()
                .id(moduleDto.getId())
                .title(moduleDto.getTitle())
                .description(moduleDto.getDescription())
                .isPublished(moduleDto.isPublished())
                .displayOrder(moduleDto.getDisplayOrder())
                .build();
    }

    private ModuleDto mapToDto(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .isPublished(module.isPublished())
                .displayOrder(module.getDisplayOrder())
                .courseId(module.getCourse() != null ? module.getCourse().getId() : null)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}
