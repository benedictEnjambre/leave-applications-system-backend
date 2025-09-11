package com.synacy.leavesmanagement.leaveapplication;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class LeaveApplicationResponse {

    private final Long id;
    private final String employeeName;
    private final String approverName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LeaveStatus status;
    private final int days;
    private final String remarks;
    private final int availableCredits;


    public LeaveApplicationResponse(LeaveApplication leaveApplication) {
        this.id = leaveApplication.getId();
        this.employeeName = leaveApplication.getEmployee().getName();
        this.approverName = leaveApplication.getApprover().getName();
        this.startDate = leaveApplication.getStartDate();
        this.endDate = leaveApplication.getEndDate();
        this.status = leaveApplication.getStatus();
        this.days = leaveApplication.getTotalDays();
        this.remarks = leaveApplication.getRemarks();
        this.availableCredits = leaveApplication.getAvailableCredits();
    }
}
