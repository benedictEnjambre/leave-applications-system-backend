package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService;
import com.synacy.leavesmanagement.user.User;
import com.synacy.leavesmanagement.user.UserService;
import com.synacy.leavesmanagement.web.apierror.ResourceNotFoundException;
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

    public LeaveApplication cancelLeave(Long userId, Long leaveApplicationId) {
        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new LeaveApplicationNotFoundException(leaveApplicationId));

        // ✅ Ensure only the employee who applied can cancel
        if (!leaveApplication.getEmployee().getId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own leave.");
        }

        // ✅ Prevent canceling already rejected or cancelled applications
        if (leaveApplication.getStatus() == LeaveStatus.REJECTED ||
                leaveApplication.getStatus() == LeaveStatus.CANCELLED) {
            throw new InvalidLeaveOperationException("Cannot cancel rejected or already cancelled leave.");
        }


        leaveApplication.setStatus(LeaveStatus.CANCELLED);
        leaveCreditsService.refundCredits(
                userId,
                leaveApplication.getStartDate(),
                leaveApplication.getEndDate()
        );
        return leaveApplicationRepository.save(leaveApplication);
    }

//    public LeaveApplication rejectLeave(Long managerId, Long leaveApplicationId) {
//        User manager = userService.getUserById(managerId);
//        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.HR) {
//            throw new AccessDeniedException("Only Managers or HR can reject leave applications.");
//        }
//
//        LeaveApplication application = leaveApplicationRepository.findById(leaveApplicationId)
//                .orElseThrow(() -> new ResourceNotFoundException(leaveApplicationId, "LeaveApplication"));
//
//        application.setStatus(LeaveStatus.REJECTED);
//        return leaveApplicationRepository.save(application);
//    }
//
//    public LeaveApplication approveLeave(Long managerId, Long leaveApplicationId) {
//        User manager = userService.getUserById(managerId);
//        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.HR) {
//            throw new AccessDeniedException("Only Managers or HR can approve leave applications.");
//        }
//
//        LeaveApplication application = leaveApplicationRepository.findById(leaveApplicationId)
//                .orElseThrow(() -> new ResourceNotFoundException(leaveApplicationId, "LeaveApplication"));
//
//        application.setStatus(LeaveStatus.APPROVED);
//        return leaveApplicationRepository.save(application);
//    }

    // Private helper to handle access check and status update
    private LeaveApplication updateLeaveStatus(Long managerId, Long leaveApplicationId, LeaveStatus status) {
        User manager = userService.getUserById(managerId);
        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException(leaveApplicationId, "LeaveApplication"));

        User employee = leaveApplication.getEmployee();
        boolean isHr = manager.getRole() == Role.HR;
        boolean isDirectManager = manager.getRole() == Role.MANAGER && employee.getManager().getId().equals(managerId);

        if (!isHr && !isDirectManager) {
            throw new AccessDeniedException("Only the employee's direct manager or HR can process this leave.");
        }

        leaveApplication.setStatus(status);
        return leaveApplicationRepository.save(leaveApplication);

    }

    public LeaveApplication approveLeave(Long managerId, Long leaveApplicationId) {
        return updateLeaveStatus(managerId, leaveApplicationId, LeaveStatus.APPROVED);
    }

    public LeaveApplication rejectLeave(Long managerId, Long leaveApplicationId) {
        LeaveApplication leaveApplication = updateLeaveStatus(managerId, leaveApplicationId, LeaveStatus.REJECTED);
        leaveCreditsService.refundCredits(
                leaveApplication.getEmployee().getId(),
                leaveApplication.getStartDate(),
                leaveApplication.getEndDate()
        );

        return leaveApplication;
    }
}
