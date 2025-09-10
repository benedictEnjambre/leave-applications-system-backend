package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.leavecredits.LeaveCredits;
import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService;
import com.synacy.leavesmanagement.web.apierror.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        // 2. Validate role
        if (userRequest.getRole() == null) {
            throw new RoleNotFoundException("null");
        }

        // 3. Validate manager (if any)
        User manager = validateAndGetManager(userRequest.getManagerId(), userRequest.getRole());

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

        return userRepository.save(user);
    }


    public User updateUser(Long id, UserEditRequest userEditRequest) {
        boolean validationResult = isHr(userEditRequest.getEditorId());
        if (!validationResult) {
            throw new InvalidOperationException("403","ONLY HR ADMIN CAN EDIT!!!");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        existingUser.setName(userEditRequest.getName());

        Role oldRole = existingUser.getRole();

        // Update role if provided
        if (userEditRequest.getRole() != null) {
            existingUser.setRole(userEditRequest.getRole());
        }

        Role effectiveRole = userEditRequest.getRole() != null
                ? userEditRequest.getRole()
                : existingUser.getRole();

        // If user was MANAGER and is no longer a MANAGER → detach employees
        if (Role.MANAGER.equals(oldRole) && !Role.MANAGER.equals(effectiveRole)) {
            // fetch all employees under this manager
            var subordinates = userRepository.findByManager_Id(existingUser.getId());
            for (User subordinate : subordinates) {
                subordinate.setManager(null);
            }
            userRepository.saveAll(subordinates);
        }

        // Update manager
        User manager = validateAndGetManager(userEditRequest.getManagerId(), effectiveRole);
        existingUser.setManager(manager);

        // Update leave credits
        LeaveCredits updatedCredits = leaveCreditsService.updateCredits(
                existingUser.getLeaveCredits(),
                userEditRequest.getTotalCredits(),
                userEditRequest.getRemainingCredits()
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
                .orElseThrow(() -> new RoleNotFoundException("This person is not a hr"));
    }

    /**
     * Helper to fetch manager and ensure user has MANAGER role.
     * Returns null if no managerId provided.
     */
    private User validateAndGetManager(Long managerId, Role role) {
        // 🚨 HRs must not have managers
        if (Role.HR.equals(role) && managerId != null) {
            throw new InvalidManagerAssignmentException("HRs cannot have managers");
        }

        if (managerId == null) {
            return null;
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ManagerNotFoundException("Manager with id " + managerId + " does not exist."));

        if (!Role.MANAGER.equals(manager.getRole())) {
            throw new InvalidManagerAssignmentException(
                    "User with id " + managerId + " cannot be assigned as manager because they are not a MANAGER"
            );
        }

        return manager;
    }
    /*
    * HR validation
    * */

    private boolean isHr(Long idForValidation){
        //validate role HR only
        User userForValidation  = userRepository.findById(idForValidation).orElseThrow(() -> new UserNotFoundException(idForValidation));

        return userForValidation.getRole().equals(Role.HR);
    }

    public Optional<User> getPrimaryHR() {
        return userRepository.findFirstByRoleOrderByIdAsc(Role.HR);
    }

}
