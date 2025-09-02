package com.synacy.leavesmanagement.leaveapplication;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.synacy.leavesmanagement.user.User;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leave_application_sequence")
    @SequenceGenerator(name = "leave_application_sequence", sequenceName = "leave_application_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User employee;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @ManyToOne
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    private String remarks;

    public LeaveApplication(LocalDate startDate, LocalDate endDate,
                            LeaveType leaveType, LeaveStatus status, String remarks) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.status = LeaveStatus.PENDING;
        this.remarks = remarks;
    }
}
