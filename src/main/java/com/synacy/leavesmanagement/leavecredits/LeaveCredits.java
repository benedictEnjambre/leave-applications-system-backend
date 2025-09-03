package com.synacy.leavesmanagement.leavecredits;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.synacy.leavesmanagement.user.User;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class LeaveCredits {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leave_credits_sequence")
    @SequenceGenerator(name = "leave_credits_sequence", sequenceName = "leave_credits_sequence", allocationSize = 1)
    private Long creditId;


    @OneToOne(mappedBy = "leaveCredits")
    private User user;

    @Column(nullable = false)
    private int totalCredits;

    @Column(nullable = false)
    private int remainingCredits;
}
