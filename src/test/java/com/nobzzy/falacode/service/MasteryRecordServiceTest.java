package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.MasteryRecordDto;
import com.nobzzy.falacode.entity.MasteryRecord;
import com.nobzzy.falacode.entity.User;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.MasteryRecordRepository;
import com.nobzzy.falacode.repository.UserRepository;
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
class MasteryRecordServiceTest {

    @Mock
    private MasteryRecordRepository masteryRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MasteryRecordService masteryRecordService;

    private User testUser;
    private MasteryRecord testRecord;
    private MasteryRecordDto testRecordDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).build();

        testRecord = MasteryRecord.builder()
                .id(100L)
                .topic("loops")
                .score(40)
                .user(testUser)
                .build();

        testRecordDto = MasteryRecordDto.builder()
                .id(100L)
                .topic("loops")
                .score(40)
                .userId(1L)
                .build();
    }

    @Nested
    @DisplayName("Create MasteryRecord Operations")
    class CreateTests {

        @Test
        @DisplayName("Should create mastery record when user exists")
        void shouldCreate_WhenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(masteryRecordRepository.save(any(MasteryRecord.class))).thenReturn(testRecord);

            MasteryRecordDto result = masteryRecordService.createMasteryRecord(1L, testRecordDto);

            assertThat(result.getTopic()).isEqualTo("loops");
            assertThat(result.getUserId()).isEqualTo(1L);
            verify(masteryRecordRepository, times(1)).save(any(MasteryRecord.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrow_WhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masteryRecordService.createMasteryRecord(99L, testRecordDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 99");

            verify(masteryRecordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read MasteryRecord Operations")
    class ReadTests {

        @Test
        @DisplayName("Should return all mastery records")
        void shouldReturnAll() {
            when(masteryRecordRepository.findAll()).thenReturn(List.of(testRecord));

            List<MasteryRecordDto> result = masteryRecordService.getAllMasteryRecords();

            assertThat(result).hasSize(1);
            verify(masteryRecordRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return record by id when found")
        void shouldReturnById_WhenFound() {
            when(masteryRecordRepository.findById(100L)).thenReturn(Optional.of(testRecord));

            MasteryRecordDto result = masteryRecordService.getMasteryRecordById(100L);

            assertThat(result.getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when record not found")
        void shouldThrow_WhenNotFound() {
            when(masteryRecordRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masteryRecordService.getMasteryRecordById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return records by user id")
        void shouldReturnByUserId() {
            when(userRepository.existsById(1L)).thenReturn(true);

            when(masteryRecordRepository.findByUserId(1L)).thenReturn(List.of(testRecord));

            List<MasteryRecordDto> result = masteryRecordService.getMasteryRecordsByUserId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Upsert Progress Operations")
    class RecordProgressTests {

        @Test
        @DisplayName("Should update existing record when one exists for user+topic")
        void shouldUpdate_WhenRecordExists() {
            when(masteryRecordRepository.findByUserIdAndTopic(1L, "loops")).thenReturn(Optional.of(testRecord));
            when(masteryRecordRepository.save(any(MasteryRecord.class))).thenReturn(testRecord);

            MasteryRecordDto result = masteryRecordService.recordProgress(1L, "loops", 75);

            assertThat(result).isNotNull();
            verify(userRepository, never()).findById(any());
            verify(masteryRecordRepository, times(1)).save(any(MasteryRecord.class));
        }

        @Test
        @DisplayName("Should create new record when none exists for user+topic")
        void shouldCreate_WhenNoRecordExists() {
            when(masteryRecordRepository.findByUserIdAndTopic(1L, "recursion")).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(masteryRecordRepository.save(any(MasteryRecord.class))).thenReturn(testRecord);

            MasteryRecordDto result = masteryRecordService.recordProgress(1L, "recursion", 20);

            assertThat(result).isNotNull();
            verify(userRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Delete MasteryRecord Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete when record exists")
        void shouldDelete_WhenExists() {
            when(masteryRecordRepository.existsById(100L)).thenReturn(true);
            doNothing().when(masteryRecordRepository).deleteById(100L);

            masteryRecordService.deleteMasteryRecordById(100L);

            verify(masteryRecordRepository, times(1)).deleteById(100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent record")
        void shouldThrow_WhenDeletingNonExistent() {
            when(masteryRecordRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> masteryRecordService.deleteMasteryRecordById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}