package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.LessonDto;
import com.nobzzy.falacode.entity.Lesson;
import com.nobzzy.falacode.entity.Module;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.LessonRepository;
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
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private LessonService lessonService;

    private Module sampleModule;
    private Lesson sampleLesson;
    private LessonDto sampleLessonDto;

    @BeforeEach
    void setUp() {
        sampleModule = Module.builder()
                .id(10L)
                .title("OOP Fundamentals")
                .build();

        sampleLesson = Lesson.builder()
                .id(100L)
                .title("Polymorphism")
                .displayOrder(1)
                .isPublished(true)
                .module(sampleModule)
                .build();

        sampleLessonDto = LessonDto.builder()
                .id(100L)
                .title("Polymorphism")
                .displayOrder(1)
                .published(true)
                .moduleId(10L)
                .build();
    }

    @Nested
    @DisplayName("Create Lesson Tests")
    class CreateLessonTests {

        @Test
        @DisplayName("Should create and return LessonDto when module exists")
        void createLesson_Success() {
            when(moduleRepository.findById(10L)).thenReturn(Optional.of(sampleModule));
            when(lessonRepository.save(any(Lesson.class))).thenReturn(sampleLesson);

            LessonDto result = lessonService.createLesson(sampleLessonDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getTitle()).isEqualTo("Polymorphism");
            assertThat(result.getModuleId()).isEqualTo(10L);
            verify(lessonRepository, times(1)).save(any(Lesson.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when module does not exist")
        void createLesson_ThrowsException_WhenModuleNotFound() {
            when(moduleRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.createLesson(sampleLessonDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with id: 10");

            verify(lessonRepository, never()).save(any(Lesson.class));
        }
    }

    @Nested
    @DisplayName("Read Lesson Tests")
    class ReadLessonTests {

        @Test
        @DisplayName("Should return all lessons")
        void getAllLessons_Success() {
            when(lessonRepository.findAll()).thenReturn(List.of(sampleLesson));

            List<LessonDto> results = lessonService.getAllLessons();

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getTitle()).isEqualTo("Polymorphism");
        }

        @Test
        @DisplayName("Should return lessons by module ID when module exists")
        void getLessonsByModuleId_Success() {
            when(moduleRepository.existsById(10L)).thenReturn(true);
            when(lessonRepository.findByModuleId(10L)).thenReturn(List.of(sampleLesson));

            List<LessonDto> results = lessonService.getLessonsByModuleId(10L);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getModuleId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when module ID does not exist")
        void getLessonsByModuleId_ThrowsException_WhenModuleNotFound() {
            when(moduleRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> lessonService.getLessonsByModuleId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with id: 99");

            verify(lessonRepository, never()).findByModuleId(anyLong());
        }

        @Test
        @DisplayName("Should return lesson by ID when lesson exists")
        void getLessonById_Success() {
            when(lessonRepository.findById(100L)).thenReturn(Optional.of(sampleLesson));

            LessonDto result = lessonService.getLessonById(100L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lesson ID does not exist")
        void getLessonById_ThrowsException_WhenNotFound() {
            when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.getLessonById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lesson not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Update Lesson Tests")
    class UpdateLessonTests {

        @Test
        @DisplayName("Should update and return updated LessonDto when lesson exists")
        void updateLesson_Success() {
            LessonDto updateRequest = LessonDto.builder()
                    .title("Advanced Polymorphism")
                    .displayOrder(2)
                    .published(false)
                    .build();

            Lesson updatedLesson = Lesson.builder()
                    .id(100L)
                    .title("Advanced Polymorphism")
                    .displayOrder(2)
                    .isPublished(false)
                    .module(sampleModule)
                    .build();

            when(lessonRepository.findById(100L)).thenReturn(Optional.of(sampleLesson));
            when(lessonRepository.save(any(Lesson.class))).thenReturn(updatedLesson);

            LessonDto result = lessonService.updateLesson(100L, updateRequest);

            assertThat(result.getTitle()).isEqualTo("Advanced Polymorphism");
            assertThat(result.getDisplayOrder()).isEqualTo(2);
            assertThat(result.getPublished()).isFalse();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent lesson")
        void updateLesson_ThrowsException_WhenNotFound() {
            when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.updateLesson(999L, sampleLessonDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lesson not found with id: 999");

            verify(lessonRepository, never()).save(any(Lesson.class));
        }
    }

    @Nested
    @DisplayName("Delete Lesson Tests")
    class DeleteLessonTests {

        @Test
        @DisplayName("Should delete lesson when ID exists")
        void deleteLessonById_Success() {
            when(lessonRepository.existsById(100L)).thenReturn(true);

            lessonService.deleteLessonById(100L);

            verify(lessonRepository, times(1)).deleteById(100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent lesson")
        void deleteLessonById_ThrowsException_WhenNotFound() {
            when(lessonRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> lessonService.deleteLessonById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lesson not found with id: 999");

            verify(lessonRepository, never()).deleteById(anyLong());
        }
    }
}