package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
    @SequenceGenerator(name = "user_sequence", sequenceName = "user_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "manager_id",  nullable = true)
    private User manager;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LeaveCredits leaveCredits;
}
