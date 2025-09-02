package com.synacy.leavesmanagement.user;

public class DuplicateUserNameException extends RuntimeException {
    public DuplicateUserNameException(String name) {
        super("User with name '" + name + "' already exists.");
    }
}
