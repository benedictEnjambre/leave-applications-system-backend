package com.synacy.leavesmanagement.leavecredits;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveCreditsRepository extends JpaRepository<LeaveCredits, Long> {
    Optional<LeaveCredits> findByUserId(Long userId);
}
