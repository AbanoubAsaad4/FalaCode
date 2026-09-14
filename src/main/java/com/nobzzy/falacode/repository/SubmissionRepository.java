package com.nobzzy.falacode.repository;

import com.nobzzy.falacode.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByExerciseId(Long exerciseId);
}