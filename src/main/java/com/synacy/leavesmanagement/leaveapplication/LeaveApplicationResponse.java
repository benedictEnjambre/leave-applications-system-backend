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

    public LeaveApplicationResponse(LeaveApplication leaveApplication) {
        this.id = leaveApplication.getId();
        this.employeeName = leaveApplication.getEmployeeName();
        this.approverName = leaveApplication.getApproverName();
        this.startDate = leaveApplication.getStartDate();
        this.endDate = leaveApplication.getEndDate();
        this.leaveType = leaveApplication.getLeaveType();
        this.status = leaveApplication.getStatus();
        this.remarks = leaveApplication.getRemarks();
    }
}
