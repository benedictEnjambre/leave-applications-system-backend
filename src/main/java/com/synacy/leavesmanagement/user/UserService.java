package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        // 4. Create LeaveCredits
        LeaveCredits leaveCredits = new LeaveCredits();
        leaveCredits.setTotalCredits(userRequest.getTotalCredits());
        leaveCredits.setRemainingCredits(userRequest.getRemainingCredits());

        // 5. Create user
        User user = new User(
                userRequest.getName(),
                userRequest.getRole(),
                manager,
                leaveCredits
        );

        return userRepository.save(user);
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
}
