package com.synacy.leavesmanagement.user;

public class ManagerNotFoundException extends RuntimeException {
    public ManagerNotFoundException(Long managerId) {
        super("Manager with id " + managerId + " does not exist.");
    }
}
