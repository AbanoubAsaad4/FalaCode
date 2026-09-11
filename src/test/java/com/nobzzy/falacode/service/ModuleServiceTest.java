package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.ModuleDto;
import com.nobzzy.falacode.entity.Course;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.CourseRepository;
import com.nobzzy.falacode.repository.ModuleRepository;
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
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ModuleService moduleService;

    private Course sampleCourse;
    private Module sampleModule;
    private ModuleDto sampleModuleDto;

    @BeforeEach
    void setUp() {
        sampleCourse = Course.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Introductory Course")
                .isPublished(true)
                .build();

        sampleModule = Module.builder()
                .id(10L)
                .title("OOP Concepts")
                .description("Learn Polymorphism & Encapsulation")
                .isPublished(true)
                .displayOrder(1)
                .course(sampleCourse)
                .build();

        sampleModuleDto = ModuleDto.builder()
                .title("OOP Concepts")
                .description("Learn Polymorphism & Encapsulation")
                .published(true)
                .displayOrder(1)
                .build();
    }

    @Nested
    @DisplayName("Create Module Tests")
    class CreateModuleTests {

        @Test
        @DisplayName("Should create module under existing course")
        void shouldCreateModule_Successfully() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(moduleRepository.save(any(Module.class))).thenReturn(sampleModule);

            ModuleDto result = moduleService.createModule(1L, sampleModuleDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getCourseId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("OOP Concepts");
            verify(moduleRepository, times(1)).save(any(Module.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when parent course does not exist")
        void shouldThrowException_WhenCourseNotFoundOnCreate() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.createModule(99L, sampleModuleDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Course not found with ID: 99");

            verify(moduleRepository, never()).save(any(Module.class));
        }
    }

    @Nested
    @DisplayName("Read Module Tests")
    class ReadModuleTests {

        @Test
        @DisplayName("Should return module DTO when ID exists")
        void shouldGetModuleById_Successfully() {
            when(moduleRepository.findById(10L)).thenReturn(Optional.of(sampleModule));

            ModuleDto result = moduleService.getModuleById(10L);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("OOP Concepts");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when module ID not found")
        void shouldThrowException_WhenModuleNotFound() {
            when(moduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.getModuleById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with ID: 99");
        }

        @Test
        @DisplayName("Should return modules by course ID when course exists")
        void shouldGetModulesByCourseId_Successfully() {
            when(courseRepository.existsById(1L)).thenReturn(true);
            when(moduleRepository.findByCourseId(1L)).thenReturn(List.of(sampleModule));

            List<ModuleDto> results = moduleService.getModulesByCourseId(1L);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getCourseId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when querying modules for non-existent course")
        void shouldThrowException_WhenQueryingModulesForNonExistentCourse() {
            when(courseRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> moduleService.getModulesByCourseId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Course not found with ID: 99");

            verify(moduleRepository, never()).findByCourseId(anyLong());
        }
    }

    @Nested
    @DisplayName("Update & Move Module Tests")
    class UpdateAndMoveModuleTests {

        @Test
        @DisplayName("Should update module details successfully")
        void shouldUpdateModule_Successfully() {
            ModuleDto updateDto = ModuleDto.builder()
                    .title("Advanced OOP")
                    .description("Updated Description")
                    .published(false)
                    .displayOrder(2)
                    .build();

            Module updatedModule = Module.builder()
                    .id(10L)
                    .title("Advanced OOP")
                    .description("Updated Description")
                    .isPublished(false)
                    .displayOrder(2)
                    .course(sampleCourse)
                    .build();

            when(moduleRepository.findById(10L)).thenReturn(Optional.of(sampleModule));
            when(moduleRepository.save(any(Module.class))).thenReturn(updatedModule);

            ModuleDto result = moduleService.updateModule(10L, updateDto);

            assertThat(result.getTitle()).isEqualTo("Advanced OOP");
            assertThat(result.getPublished()).isFalse();
            assertThat(result.getDisplayOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should move module to new course")
        void shouldMoveModuleToCourse_Successfully() {
            Course newCourse = Course.builder().id(2L).title("Spring Boot 101").build();
            Module movedModule = Module.builder()
                    .id(10L)
                    .title("OOP Concepts")
                    .course(newCourse)
                    .build();

            when(moduleRepository.findById(10L)).thenReturn(Optional.of(sampleModule));
            when(courseRepository.findById(2L)).thenReturn(Optional.of(newCourse));
            when(moduleRepository.save(any(Module.class))).thenReturn(movedModule);

            ModuleDto result = moduleService.moveModuleToCourse(10L, 2L);

            assertThat(result.getCourseId()).isEqualTo(2L);
            verify(moduleRepository, times(1)).save(any(Module.class));
        }
    }

    @Nested
    @DisplayName("Delete Module Tests")
    class DeleteModuleTests {

        @Test
        @DisplayName("Should delete module when ID exists")
        void shouldDeleteModule_Successfully() {
            when(moduleRepository.existsById(10L)).thenReturn(true);

            moduleService.deleteModuleById(10L);

            verify(moduleRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent module")
        void shouldThrowException_WhenDeletingNonExistentModule() {
            when(moduleRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> moduleService.deleteModuleById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(moduleRepository, never()).deleteById(anyLong());
        }
    }
}