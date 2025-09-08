package com.synacy.leavesmanagement.user;

import lombok.Getter;

@Getter
public class UserEditRequest {

    private Long editorId;

    private String name;
    private Role role;
    private Long managerId; // instead of full User
    private Integer totalCredits;     // 🔹 now nullable
    private Integer remainingCredits; // 🔹 now nullable
}
