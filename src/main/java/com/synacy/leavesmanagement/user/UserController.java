package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.web.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/v1/users")
    public PageResponse<UserResponse> fetchUsers(
            @RequestParam(value = "max", defaultValue = "3") int max,
            @RequestParam(value = "page", defaultValue = "1") int page, // 1-based
            @RequestParam(value = "manager", required = false) Long manager,
            @RequestParam(value = "totalCredits", required = false) Integer totalCredits,
            @RequestParam(value = "remainingCredits", required = false) Integer remainingCredits
    ) {
/*        if (max < 1) {
            throw new ValidationException("INVALID_MAX_VALUE", "Max value must be greater than 0");
        }
        if (page < 1) {
            throw new ValidationException("INVALID_PAGE_VALUE", "Page value must be greater than 0");
        }*/

        Page<User> users = userService.fetchUsers(max, page - 1, manager, totalCredits, remainingCredits);

        List<UserResponse> userResponseList = users.getContent()
                .stream()
                .map(UserResponse::new)
                .toList();

        return new PageResponse<>(
                (int) users.getTotalElements(),
                page, // keep API 1-based
                userResponseList
        );
    }
    @PostMapping("/api/v1/user")
    public UserResponse createUser(@RequestBody UserRequest userRequest){
        User user =  userService.createUser(userRequest);
        return new UserResponse(user);
    }

    @PutMapping("/api/v1/user/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest userRequest
    ) {
        User user = userService.updateUser(id, userRequest);
        return new UserResponse(user);
    }



}
