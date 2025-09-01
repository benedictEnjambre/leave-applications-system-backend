package com.synacy.leavesmanagement.leaveapplication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class LeaveApplicationDetails {

    private Long applicationId;
    private String employeeName;
    private String approverName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private LeaveStatus status;
    private String remarks;
}
