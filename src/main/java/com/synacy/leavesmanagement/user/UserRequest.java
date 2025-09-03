package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRequest {
    private String name;
    private Role role;
    private Long managerId; // instead of full User
    private int totalCredits;
    private int remainingCredits;
}

