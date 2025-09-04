package com.synacy.leavesmanagement.leaveapplication;

public class InvalidLeaveOperationException extends RuntimeException {
    public InvalidLeaveOperationException(String message) {
        super(message);
    }
}