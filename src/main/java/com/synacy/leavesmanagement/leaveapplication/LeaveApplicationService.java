package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService;
import com.synacy.leavesmanagement.user.User;
import com.synacy.leavesmanagement.user.UserService;
import com.synacy.leavesmanagement.web.apierror.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidLeaveOperationException("Start date cannot be after end date.");
        }

        int requestedDays = leaveCreditsService.calculateRequestedDays(
                request.getStartDate(),
                request.getEndDate()
        );

        int totalDays = leaveCreditsService.deductCredits(employee.getId(), request.getStartDate(), request.getEndDate());
        User approver = determineApprover(employee);

        LeaveApplication leaveApplication = new LeaveApplication(
                request.getStartDate(),
                request.getEndDate(),
                request.getRemarks()
        );
        leaveApplication.setEmployee(employee);
        leaveApplication.setApprover(approver);
        leaveApplication.setAvailableCredits(totalDays);
        leaveApplication.setTotalDays(requestedDays);

        return leaveApplicationRepository.save(leaveApplication);
    }

    private User determineApprover(User employee) {
        if (employee.getManager() != null) {
            return employee.getManager();
        }

        return userService.getPrimaryHR()
                .orElseThrow(() -> new InvalidLeaveOperationException(
                        "No manager or HR available to act as approver."
                ));
    }

    public Page<LeaveApplication> fetchOwnLeaveApplication(Long userId, int page, int max) {
        Pageable pageable = PageRequest.of(page - 1, max);

        User employee = userService.getUserById(userId);
        if (employee.getRole() != Role.EMPLOYEE && employee.getRole() != Role.MANAGER && employee.getRole() != Role.HR) {
            throw new AccessDeniedException("Invalid role for fetching own leave applications.");
        }
        return leaveApplicationRepository.findByEmployee(employee, pageable);
    }

    public Page<LeaveApplication> fetchTeamLeaveApplication(Long userId, int page, int max) {
        Pageable pageable = PageRequest.of(page - 1, max, Sort.by("startDate").descending());

        User manager = userService.getUserById(userId);
        if (manager.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only Managers can fetch team leave applications.");
        }
        return leaveApplicationRepository.findByApprover(manager, pageable);
    }

    public Page<LeaveApplication> fetchAllLeaveApplication(Long userId, int page, int max) {
        Pageable pageable = PageRequest.of(page - 1, max, Sort.by(Sort.Direction.DESC, "startDate"));

        User hr = userService.getUserById(userId);
        if (hr.getRole() != Role.HR) {
            throw new AccessDeniedException("Only HR can fetch all leave applications.");
        }

        return leaveApplicationRepository.findAll(pageable);
    }

    public LeaveApplication cancelLeave(Long userId, Long leaveApplicationId) {
        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("ID_NOT_FOUND", "Leave Application with id" + leaveApplicationId + "not found"));

        if (!leaveApplication.getEmployee().getId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own leave.");
        }
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

    public LeaveApplication approveLeave(Long managerId, Long leaveApplicationId) {
        return processLeave(managerId, leaveApplicationId, LeaveStatus.APPROVED);
    }

    public LeaveApplication rejectLeave(Long managerId, Long leaveApplicationId) {
        LeaveApplication leaveApplication = processLeave(managerId, leaveApplicationId, LeaveStatus.REJECTED);
        leaveCreditsService.refundCredits(
                leaveApplication.getEmployee().getId(),
                leaveApplication.getStartDate(),
                leaveApplication.getEndDate()
        );

        return leaveApplication;
    }

    private LeaveApplication processLeave(Long managerId, Long leaveApplicationId, LeaveStatus status) {
        User manager = userService.getUserById(managerId);
        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("ID_NOT_FOUND", "Leave Application with id" + leaveApplicationId + "not found"));

        User employee = leaveApplication.getEmployee();
        boolean isHr = manager.getRole() == Role.HR;
        boolean isDirectManager = manager.getRole() == Role.MANAGER && employee.getManager().getId().equals(managerId);

        if (!isHr && !isDirectManager) {
            throw new AccessDeniedException("Only the employee's direct manager or HR can process this leave.");
        }

        leaveApplication.setStatus(status);
        return leaveApplicationRepository.save(leaveApplication);
    }
}
