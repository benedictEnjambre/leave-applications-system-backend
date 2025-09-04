package com.synacy.leavesmanagement.leaveapplication;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
