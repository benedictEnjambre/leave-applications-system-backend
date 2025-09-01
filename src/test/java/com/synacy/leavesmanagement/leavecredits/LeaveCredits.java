package com.synacy.leavesmanagement.leavecredits;

import com.synacy.leavesmanagement.user.User;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class LeaveCredits {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leave_credits_sequence")
    @SequenceGenerator(name = "leave_credits_sequence", sequenceName = "leave_credits_sequence", allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int totalCredits = 30;

    @Column(nullable = false)
    private int remainingCredits = 30;
}
