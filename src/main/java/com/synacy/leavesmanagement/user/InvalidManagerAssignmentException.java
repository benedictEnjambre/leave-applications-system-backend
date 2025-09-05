package com.synacy.leavesmanagement.user;

public class InvalidManagerAssignmentException extends RuntimeException {
    public InvalidManagerAssignmentException(Long userId)
    {
        super("User with id " + userId + " cannot be assigned as manager because they are not a MANAGER");
    }

}
