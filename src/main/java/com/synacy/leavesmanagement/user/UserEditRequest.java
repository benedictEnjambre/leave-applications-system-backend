package com.synacy.leavesmanagement.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserEditRequest {

    private Long editorId;

    private String name;
    private Role role;
    private Long managerId; // instead of full User
    private Integer totalCredits;     // 🔹 now nullable
    private Integer remainingCredits; // 🔹 now nullable
}
