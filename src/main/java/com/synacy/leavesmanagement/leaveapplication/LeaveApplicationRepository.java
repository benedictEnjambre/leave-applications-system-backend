package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication,Long> {

    List<LeaveApplication> findByEmployee(User employee);

    List<LeaveApplication> findByApprover(User approver);

}
