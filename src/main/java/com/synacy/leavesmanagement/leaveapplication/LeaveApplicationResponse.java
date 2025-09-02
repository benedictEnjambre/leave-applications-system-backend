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
    private final LeaveType leaveType;
    private final LeaveStatus status;
    private final String remarks;

    public LeaveApplicationResponse(LeaveApplicationDetails leaveApplicationDetails) {
        this.id = leaveApplicationDetails.getId();
        this.employeeName = leaveApplicationDetails.getEmployeeName();
        this.approverName = leaveApplicationDetails.getApproverName();
        this.startDate = leaveApplicationDetails.getStartDate();
        this.endDate = leaveApplicationDetails.getEndDate();
        this.leaveType = leaveApplicationDetails.getLeaveType();
        this.status = leaveApplicationDetails.getStatus();
        this.remarks = leaveApplicationDetails.getRemarks();
    }
}
