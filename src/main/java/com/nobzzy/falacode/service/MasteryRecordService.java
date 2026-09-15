package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.MasteryRecordDto;
import com.nobzzy.falacode.entity.MasteryRecord;
import com.nobzzy.falacode.entity.User;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.MasteryRecordRepository;
import com.nobzzy.falacode.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MasteryRecordService {

    private final MasteryRecordRepository masteryRecordRepository;
    private final UserRepository userRepository;

    public MasteryRecordService(MasteryRecordRepository masteryRecordRepository, UserRepository userRepository) {
        this.masteryRecordRepository = masteryRecordRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    @Transactional
    public MasteryRecordDto createMasteryRecord(Long userId, MasteryRecordDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        MasteryRecord record = mapToEntity(dto);
        record.setUser(user);

        return mapToDto(masteryRecordRepository.save(record));
    }

    // READ ALL
    public List<MasteryRecordDto> getAllMasteryRecords() {
        return masteryRecordRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // READ BY ID
    public MasteryRecordDto getMasteryRecordById(Long id) {
        MasteryRecord masteryRecord = masteryRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasteryRecord not found with id: " + id));

        return mapToDto(masteryRecord);
    }

    // READ BY USER ID
    public List<MasteryRecordDto> getMasteryRecordsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return masteryRecordRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // UPDATE
    @Transactional
    public MasteryRecordDto updateMasteryRecord(Long id, MasteryRecordDto dto) {
        MasteryRecord record = masteryRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasteryRecord not found with id: " + id));

        record.setTopic(dto.getTopic());
        record.setScore(dto.getScore());
        record.setLastReviewed(dto.getLastReviewed());

        return mapToDto(masteryRecordRepository.save(record));
    }

    // UPSERT
    @Transactional
    public MasteryRecordDto recordProgress(Long userId, String topic, Integer score) {
        MasteryRecord record = masteryRecordRepository.findByUserIdAndTopic(userId, topic)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    return MasteryRecord.builder().topic(topic).user(user).score(0).build();
                });

        record.setScore(score);
        record.setLastReviewed(LocalDateTime.now());

        return mapToDto(masteryRecordRepository.save(record));
    }

    // DELETE
    @Transactional
    public void deleteMasteryRecordById(Long id) {
        if (!masteryRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("MasteryRecord not found with id: " + id);
        }
        masteryRecordRepository.deleteById(id);
    }

    // MAPPER
    private MasteryRecordDto mapToDto(MasteryRecord record) {
        return MasteryRecordDto.builder()
                .id(record.getId())
                .topic(record.getTopic())
                .score(record.getScore())
                .lastReviewed(record.getLastReviewed())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private MasteryRecord mapToEntity(MasteryRecordDto dto) {
        return MasteryRecord.builder()
                .id(dto.getId())
                .topic(dto.getTopic())
                .score(dto.getScore())
                .lastReviewed(dto.getLastReviewed() != null
                        ? dto.getLastReviewed()
                        : LocalDateTime.now())
                .build();
    }
}