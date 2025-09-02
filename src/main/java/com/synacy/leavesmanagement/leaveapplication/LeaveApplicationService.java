package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.user.User;
import com.synacy.leavesmanagement.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveCreditRepository leaveCreditRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeaveApplicationService(LeaveApplicationRepository leaveApplicationRepository, LeaveCreditRepository leaveCreditRepository, UserRepository userRepository) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveCreditRepository = leaveCreditRepository;
        this.userRepository = userRepository;
    }

    public LeaveApplication applyLeave(Long userId, LeaveApplicationRequest request){
        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int requestedDays = leaveCreditRepository.deductCredits(employee, request.getStartDate(), request.getEndDate());
        if (requestedDays <= 0) {
            throw new IllegalStateException("Insufficient leave credits");
        }


        // Not Final
        User approver = null;
        if (employee.getRole().isEmployee()){
            approver = employee.getManager();
        }

        LeaveApplication leaveApplication = new LeaveApplication(
                request.getStartDate(),
                request.getEndDate(),
                request.getLeaveType(),
                LeaveStatus.PENDING,
                request.getRemarks()
        );
        leaveApplication.setEmployee(employee);
        leaveApplication.setApprover(approver);

        return leaveApplicationRepository.save(leaveApplication);
        }


    }
