package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import lombok.Getter;

@Getter
public class UserRequest {
    private String name;
    private Role role;
    private User manager;
    private LeaveCredits leaveCredits;
}
