package com.synacy.leavesmanagement.leavecredits;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveCreditsRepository extends JpaRepository<LeaveCredits, Long> {
    LeaveCredits findByUserId(Long userId);
}
//public interface LeaveCreditsRepository extends JpaRepository<LeaveCredits, Long> {
//    Optional<LeaveCredits> findByUser_UserId(Long userId);
//}
