package com.synacy.leavesmanagement.leavecredits;

public class LeaveCreditsNotFoundException extends RuntimeException {
    public LeaveCreditsNotFoundException(Long userId) {
        super("Leave credits not found for user with ID: " + userId);
    }
}
