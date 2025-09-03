package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.user.User;
import com.synacy.leavesmanagement.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveCreditRepository leaveCreditRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeaveApplicationService(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveCreditRepository leaveCreditRepository,
            UserRepository userRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveCreditRepository = leaveCreditRepository;
        this.userRepository = userRepository;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
    }

    public LeaveApplication applyLeave(Long userId, LeaveApplicationRequest request) {
        User employee = findUser(userId);

        int requestedDays = leaveCreditRepository.deductCredits(employee, request.getStartDate(), request.getEndDate());
        if (requestedDays <= 0) {
            throw new IllegalStateException("Insufficient leave credits");
        }

        // Assign approver only if the applicant is a regular employee
        User approver = null;
        if (employee.getRole().isEmployee()) {
            approver = employee.getManager(); // manager assigned
        }
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


    public List<LeaveApplicationResponse> getLeaveApplicationsForUser(Long userId) {
        User user = findUser(userId);

        List<LeaveApplication> applications;

        if (user.getRole().isHR()) {
            applications = leaveApplicationRepository.findAll(); // HR sees everything
        } else if (user.getRole().isManager()) {
            applications = leaveApplicationRepository.findByApprover(user); // employees reporting to manager
            applications.addAll(leaveApplicationRepository.findByEmployee(user)); // manager's own applications
        } else {
            applications = leaveApplicationRepository.findByEmployee(user); // regular employee only sees own
        }

        return applications.stream()
                .map(LeaveApplicationResponse::new)
                .toList();
    }


    public LeaveApplicationResponse updateLeaveStatus(Long approverId, Long leaveAppId, LeaveStatus status, String remarks) {
        User approver = findUser(approverId);

        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveAppId)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found"));

        // Validation: Employee leave → Manager or HR; Manager/HR leave → HR only
        if (leaveApplication.getApprover() != null) {
            // Approver is the manager
            if (!approver.equals(leaveApplication.getApprover()) && !approver.getRole().isHR()) {
                throw new IllegalStateException("You are not authorized to approve/reject this leave");
            }
        } else {
            // Approver is null → only HR can act
            if (!approver.getRole().isHR()) {
                throw new IllegalStateException("Only HR can approve/reject this leave");
            }
        }

        leaveApplication.setStatus(status);
        leaveApplication.setRemarks(remarks);
        leaveApplicationRepository.save(leaveApplication);

        // Refund credits if rejected
        if (status == LeaveStatus.REJECTED) {
            leaveCreditRepository.refundCredits(
                    leaveApplication.getEmployee(),
                    leaveApplication.getStartDate(),
                    leaveApplication.getEndDate()
            );
        }

        return new LeaveApplicationResponse(leaveApplication);
    }


    public LeaveApplicationResponse cancelLeave(Long userId, Long leaveAppId) {
        LeaveApplication leaveApplication = leaveApplicationRepository.findById(leaveAppId)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found"));

        if (!leaveApplication.getEmployee().getId().equals(userId)) {
            throw new IllegalStateException("You cannot cancel someone else's leave");
        }

        if (leaveApplication.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leaves can be cancelled");
        }

        // Refund credits on cancel
        leaveCreditRepository.refundCredits(
                leaveApplication.getEmployee(),
                leaveApplication.getStartDate(),
                leaveApplication.getEndDate()
        );

        leaveApplication.setStatus(LeaveStatus.CANCELLED);
        leaveApplicationRepository.save(leaveApplication);

        return new LeaveApplicationResponse(leaveApplication);
    }
}
