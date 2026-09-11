package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.CourseDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course sampleCourse;
    private CourseDto sampleCourseDto;

    @BeforeEach
    void setUp() {
        sampleCourse = Course.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Learn core OOP principles")
                .isPublished(true)
                .displayOrder(1)
                .modules(List.of(Module.builder().id(101L).build()))
                .build();

        sampleCourseDto = CourseDto.builder()
                .title("Java Fundamentals")
                .description("Learn core OOP principles")
                .published(true)
                .displayOrder(1)
                .build();
    }

    @Nested
    @DisplayName("Create Course Tests")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create course successfully")
        void shouldCreateCourse_Successfully() {
            when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);

            CourseDto result = courseService.createCourse(sampleCourseDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Java Fundamentals");
            assertThat(result.getPublished()).isTrue();
            assertThat(result.getModules()).containsExactly(101L);
            verify(courseRepository, times(1)).save(any(Course.class));
        }
    }

    @Nested
    @DisplayName("Read Course Tests")
    class ReadCourseTests {

        @Test
        @DisplayName("Should return course DTO when ID exists")
        void shouldGetCourseById_Successfully() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            CourseDto result = courseService.getCourseById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Java Fundamentals");
            assertThat(result.getModules()).containsExactly(101L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when course ID does not exist")
        void shouldThrowException_WhenCourseByIdNotFound() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Course not found with id: 99");
        }

        @Test
        @DisplayName("Should return all courses")
        void shouldGetAllCourses() {
            Course course2 = Course.builder().id(2L).title("Spring Boot 101").build();
            when(courseRepository.findAll()).thenReturn(List.of(sampleCourse, course2));

            List<CourseDto> results = courseService.getAllCourses();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTitle()).isEqualTo("Java Fundamentals");
            assertThat(results.get(1).getTitle()).isEqualTo("Spring Boot 101");
        }
    }

    @Nested
    @DisplayName("Update Course Tests")
    class UpdateCourseTests {

        @Test
        @DisplayName("Should update course successfully when ID exists")
        void shouldUpdateCourse_Successfully() {
            CourseDto updateDto = CourseDto.builder()
                    .title("Advanced Java")
                    .description("Updated Description")
                    .published(false)
                    .displayOrder(2)
                    .build();

            Course updatedCourse = Course.builder()
                    .id(1L)
                    .title("Advanced Java")
                    .description("Updated Description")
                    .isPublished(false)
                    .displayOrder(2)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);

            CourseDto result = courseService.updateCourse(1L, updateDto);

            assertThat(result.getTitle()).isEqualTo("Advanced Java");
            assertThat(result.getPublished()).isFalse();
            assertThat(result.getDisplayOrder()).isEqualTo(2);
            verify(courseRepository, times(1)).save(any(Course.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent course")
        void shouldThrowException_WhenUpdatingNonExistentCourse() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(99L, sampleCourseDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Course not found with id: 99");

            verify(courseRepository, never()).save(any(Course.class));
        }
    }

    @Nested
    @DisplayName("Delete Course Tests")
    class DeleteCourseTests {

        @Test
        @DisplayName("Should delete course when ID exists")
        void shouldDeleteCourse_Successfully() {
            when(courseRepository.existsById(1L)).thenReturn(true);

            courseService.deleteCourse(1L);

            verify(courseRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent course")
        void shouldThrowException_WhenDeletingNonExistentCourse() {
            when(courseRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> courseService.deleteCourse(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Course not found with id: 99");

            verify(courseRepository, never()).deleteById(anyLong());
        }
    }
}