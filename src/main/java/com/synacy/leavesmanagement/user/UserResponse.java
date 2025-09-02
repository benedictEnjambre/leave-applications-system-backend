package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String name;
    private final User manager;
    private final Role role;
    private final LeaveCredits leaveCredits;

    UserResponse(User user) {
        this.name = user.getName();
        this.manager = user.getManager();
        this.role = user.getRole();
        this.leaveCredits = user.getLeaveCredits();
    }

}
