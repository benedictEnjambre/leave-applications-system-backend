package com.synacy.leavesmanagement.user;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String role) {
        super("Role does not exist: " + role);
    }
}
