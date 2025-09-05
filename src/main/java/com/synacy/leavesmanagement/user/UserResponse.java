package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String name;
    private final String managerName;
    private final Role role;
    private final long id;
    private final Integer totalCredits;     // 🔹 now nullable
    private final Integer remainingCredits; // 🔹 now nullable

    UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole();
        this.managerName = user.getManager() != null ? user.getManager().getName() : null;
        this.totalCredits = user.getLeaveCredits().getTotalCredits();
        this.remainingCredits = user.getLeaveCredits().getRemainingCredits();
    }
}
