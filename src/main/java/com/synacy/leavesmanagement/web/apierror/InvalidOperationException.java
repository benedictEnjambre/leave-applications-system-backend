package com.synacy.leavesmanagement.web.apierror;

public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
