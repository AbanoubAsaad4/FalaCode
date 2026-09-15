package com.nobzzy.falacode.repository;

import com.nobzzy.falacode.entity.MasteryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasteryRecordRepository extends JpaRepository<MasteryRecord, Long> {
    List<MasteryRecord> findByUserId(Long userId);
    Optional<MasteryRecord> findByUserIdAndTopic(Long userId, String topic);
}