package com.synacy.leavesmanagement.leaveapplication;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LeaveApplicationRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String remarks;
}
