package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService;
import com.synacy.leavesmanagement.user.User;
import com.synacy.leavesmanagement.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.synacy.leavesmanagement.user.Role;

@Service
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveCreditsService leaveCreditsService;
    private final UserService userService;

    @Autowired
    public LeaveApplicationService(LeaveApplicationRepository leaveApplicationRepository, LeaveCreditsService leaveCreditsService, UserService userService) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveCreditsService = leaveCreditsService;
        this.userService = userService;
    }


    public LeaveApplication applyLeave(Long userId, LeaveApplicationRequest request) {
        User employee = userService.getUserById(userId);

        int requestedDays = leaveCreditsService.deductCredits(employee.getId(), request.getStartDate(), request.getEndDate());

        // Assign approver only if the applicant is a regular employee
        User approver = determineApprover(employee);
        // For manager & HR leave applications -> approver remains null (HR will handle)

        LeaveApplication leaveApplication = new LeaveApplication(
                request.getStartDate(),
                request.getEndDate(),
                request.getLeaveType(),
                request.getRemarks()
        );
        leaveApplication.setEmployee(employee);
        leaveApplication.setApprover(approver);

        return leaveApplicationRepository.save(leaveApplication);
    }

    private User determineApprover(User employee) {
        return (employee.getManager() != null)
                ? employee.getManager()
                : userService.getHR();
    }

    // Employee's own leaves
    public Page<LeaveApplication> fetchOwnLeaveApplication(Long userId, int page, int max) {
        Pageable pageable = PageRequest.of(page - 1, max);

        User employee = userService.getUserById(userId);
        if (employee.getRole() != Role.EMPLOYEE && employee.getRole() != Role.MANAGER && employee.getRole() != Role.HR) {
            throw new AccessDeniedException("Invalid role for fetching own leave applications.");
        }
        return leaveApplicationRepository.findByEmployee(employee, pageable);
    }

    // Manager: leaves of their team
    public Page<LeaveApplication> fetchTeamLeaveApplication(Long userId, int page, int max) {
        Pageable pageable = PageRequest.of(page - 1, max);

        User manager = userService.getUserById(userId);
        if (manager.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only Managers can fetch team leave applications.");
        }
        return leaveApplicationRepository.findByApprover(manager, pageable);
    }

    // HR: all leaves
    public Page<LeaveApplication> fetchAllLeaveApplication(Long userId, int page, int max) {
        Pageable pageable = PageRequest.of(page - 1, max);

        User hr = userService.getUserById(userId);
        if (hr.getRole() != Role.HR) {
            throw new AccessDeniedException("Only HR can fetch all leave applications.");
        }

        return leaveApplicationRepository.findAll(pageable);
    }
}

//    public LeaveApplicationResponse updateLeaveStatus(Long approverId, Long leaveAppId, LeaveStatus status, String remarks) {
//        User approver = userService.getUserById(approverId);
//
//        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveAppId)
//                .orElseThrow(() -> new IllegalArgumentException("Leave application not found"));
//
//        // Validation: Employee leave → Manager or HR; Manager/HR leave → HR only
//        if (leaveApplication.getApprover() != null) {
//            // Approver is the manager
//            if (!approver.equals(leaveApplication.getApprover()) && !approver.getRole().isHR()) {
//                throw new IllegalStateException("You are not authorized to approve/reject this leave");
//            }
//        } else {
//            // Approver is null → only HR can act
//            if (!approver.getRole().isHR()) {
//                throw new IllegalStateException("Only HR can approve/reject this leave");
//            }
//        }
//
//        leaveApplication.setStatus(status);
//        leaveApplication.setRemarks(remarks);
//        leaveApplicationRepository.save(leaveApplication);
//
//        // Refund credits if rejected
//        if (status == LeaveStatus.REJECTED) {
//            leaveCreditsService.refundCredits(
//                    leaveApplication.getEmployee(),
//                    leaveApplication.getStartDate(),
//                    leaveApplication.getEndDate()
//            );
//        }
//
//        return new LeaveApplicationResponse(leaveApplication);
//    }
//
//
//    public LeaveApplicationResponse cancelLeave(Long userId, Long leaveAppId) {
//        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveAppId)
//                .orElseThrow(() -> new IllegalArgumentException("Leave application not found"));
//
//        if (!leaveApplication.getEmployee().getId().equals(userId)) {
//            throw new IllegalStateException("You cannot cancel someone else's leave");
//        }
//
//        if (leaveApplication.getStatus() != LeaveStatus.PENDING) {
//            throw new IllegalStateException("Only pending leaves can be cancelled");
//        }
//
//        // Refund credits on cancel
//        leaveCreditsService.refundCredits(
//                leaveApplication.getEmployee(),
//                leaveApplication.getStartDate(),
//                leaveApplication.getEndDate()
//        );
//
//        leaveApplication.setStatus(LeaveStatus.CANCELLED);
//        leaveApplicationRepository.save(leaveApplication);
//
//        return new LeaveApplicationResponse(leaveApplication);
//    }
//}
