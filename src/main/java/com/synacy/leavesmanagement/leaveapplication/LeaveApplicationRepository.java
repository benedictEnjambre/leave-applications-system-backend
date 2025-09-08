package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication,Long> {

    Page<LeaveApplication> findByEmployee(User employee, Pageable pageable);
    Page<LeaveApplication> findByApprover(User approver, Pageable pageable);



}
