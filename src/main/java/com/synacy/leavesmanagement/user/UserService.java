package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import com.synacy.leavesmanagement.leavecredits.LeaveCreditsRepository;
import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LeaveCreditsService leaveCreditsService;

    @Autowired
    public UserService(UserRepository userRepository,  LeaveCreditsService leaveCreditsService) {
        this.userRepository = userRepository;
        this.leaveCreditsService = leaveCreditsService;
    }

    public User createUser(UserRequest userRequest) {
        // 1. Validate name
        if (userRequest.getName() == null || userRequest.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (userRepository.existsByName(userRequest.getName())) {
            throw new DuplicateUserNameException(userRequest.getName());
        }

        // 2. Validate role
        if (userRequest.getRole() == null) {
            throw new RoleNotFoundException("null");
        }

        // 3. Get manager (if provided)
        User manager = null;
        if (userRequest.getManagerId() != null) {
            manager = userRepository.findById(userRequest.getManagerId())
                    .orElseThrow(() -> new ManagerNotFoundException(userRequest.getManagerId()));
        }

        // 4. Delegate leave credits setup
        LeaveCredits leaveCredits = leaveCreditsService.newUserCredits(
                userRequest.getTotalCredits(),
                userRequest.getRemainingCredits()
        );

        // 5. Create user
        User user = new User(
                userRequest.getName(),
                userRequest.getRole(),
                manager,
                leaveCredits
        );

        return userRepository.save(user); // cascade saves credits
    }


    public User updateUser(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Update name
        if (userRequest.getName() != null && !userRequest.getName().isBlank()) {
            if (!existingUser.getName().equals(userRequest.getName())
                    && userRepository.existsByName(userRequest.getName())) {
                throw new DuplicateUserNameException(userRequest.getName());
            }
            existingUser.setName(userRequest.getName());
        }

        // Update role
        if (userRequest.getRole() != null) {
            existingUser.setRole(userRequest.getRole());
        }

        // Update manager
        if (userRequest.getManagerId() != null) {
            User manager = userRepository.findById(userRequest.getManagerId())
                    .orElseThrow(() -> new ManagerNotFoundException(userRequest.getManagerId()));
            existingUser.setManager(manager);
        } else {
            existingUser.setManager(null);
        }

        // 🔹 Delegate credits update to service
        LeaveCredits updatedCredits = leaveCreditsService.updateCredits(
                existingUser.getLeaveCredits(),
                userRequest.getTotalCredits(),
                userRequest.getRemainingCredits()
        );

        existingUser.setLeaveCredits(updatedCredits);

        return userRepository.save(existingUser);
    }



    // ✅ Pagination + filters for users
    public Page<User> fetchUsers(int max, int page, Long manager, Integer totalCredits, Integer remainingCredits) {
        Pageable pageable = PageRequest.of(page, max);

        if (manager != null) {
            return userRepository.findByManager_Id(manager, pageable);
        }

        if (totalCredits != null) {
            return userRepository.findByLeaveCredits_TotalCredits(totalCredits, pageable);
        }

        if (remainingCredits != null) {
            return userRepository.findByLeaveCredits_RemainingCredits(remainingCredits, pageable);
        }

        // default: no filters
        return userRepository.findAll(pageable);
    }

    // 🔹 Get a user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // 🔹 Get the manager of a given employee
    public User getManager(User employee) {
        if (employee.getManager() == null) {
            throw new ManagerNotFoundException(null);
        }
        return getUserById(employee.getManager().getId());
    }

    // 🔹 Get an HR user (assuming role is stored in a Role enum or String)
    public User getHR() {
        return userRepository.findByRole(Role.HR)
                .orElseThrow(() -> new RoleNotFoundException("HR"));
    }

    public boolean isHr(User employee) {
        return employee.getRole().equals(Role.HR);
    }
    public boolean isManager(User employee) {
        return employee.getRole().equals(Role.MANAGER);
    }
    public boolean isEmployee(User employee) {
        return employee.getRole().equals(Role.EMPLOYEE);
    }
}
