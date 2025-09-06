package com.synacy.leavesmanagement.leaveapplication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class LeaveApplicationRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String remarks;
}
