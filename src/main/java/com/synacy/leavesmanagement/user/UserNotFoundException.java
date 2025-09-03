package com.synacy.leavesmanagement.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("This user doesn't exits ID of: "+userId);
    }
}
