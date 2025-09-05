package com.synacy.leavesmanagement.leaveapplication;

public class LeaveApplicationNotFoundException extends RuntimeException {
    public LeaveApplicationNotFoundException(Long leaveId) {
        super("Leave application not found with id: " + leaveId);
    }
}