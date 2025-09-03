package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String name;
    private final String managerName;
    private final Role role;
    //  private final LeaveCredits leaveCredits;

    UserResponse(User user) {
        this.name = user.getName();
        this.role = user.getRole();
        this.managerName = user.getManager() != null ? user.getManager().getName() : null;
       // this.leaveCredits = user.getLeaveCredits() != null ? new LeaveCreditsResponse(user.getLeaveCredits()) : null;
    }
}
