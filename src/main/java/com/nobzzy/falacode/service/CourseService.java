package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.CourseDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // CREATE
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        Course course = mapToEntity(courseDto);
        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

    // READ ALL
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return mapToDto(course);
    }

    // UPDATE
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        existingCourse.setTitle(courseDto.getTitle());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setPublished(courseDto.getPublished());
        existingCourse.setDisplayOrder(courseDto.getDisplayOrder());

        Course updatedCourse = courseRepository.save(existingCourse);
        return mapToDto(updatedCourse);
    }

    // DELETE
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    // MAPPERS
    private Course mapToEntity(CourseDto courseDto) {
        return Course.builder()
                .id(courseDto.getId())
                .title(courseDto.getTitle())
                .description(courseDto.getDescription())
                .isPublished(courseDto.getPublished())
                .displayOrder(courseDto.getDisplayOrder())
                .build();
    }

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .published(course.isPublished())
                .displayOrder(course.getDisplayOrder())
                .modules(course.getModules() != null
                        ? course.getModules().stream().map(Module::getId).toList()
                        : List.of())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}