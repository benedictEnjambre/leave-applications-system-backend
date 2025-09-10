package com.synacy.leavesmanagement.user;

public class ManagerNotFoundException extends RuntimeException {
    public ManagerNotFoundException(String message) {
        super(message);
    }
}
