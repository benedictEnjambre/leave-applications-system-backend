package com.synacy.leavesmanagement.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByName(String name);

    Optional<User> findByRole(Role role);

    Optional<User> findFirstByRoleOrderByIdAsc(Role role);
    // Manager filter
    Page<User> findByManager_Id(Long managerId, Pageable pageable);

    // Filter by total credits - use nested property syntax
    Page<User> findByLeaveCredits_TotalCredits(int totalCredits, Pageable pageable);

    // Filter by remaining credits - use nested property syntax
    Page<User> findByLeaveCredits_RemainingCredits(int remainingCredits, Pageable pageable);
}
