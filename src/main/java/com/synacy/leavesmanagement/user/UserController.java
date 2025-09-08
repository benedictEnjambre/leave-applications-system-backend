package com.synacy.leavesmanagement.user;

import com.synacy.leavesmanagement.web.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/api/v1/user")
    public PageResponse<UserResponse> fetchUsers(
            @RequestParam(value = "max", defaultValue = "5") int max,
            @RequestParam(value = "page", defaultValue = "1") int page, // 1-based
            @RequestParam(value = "manager", required = false) Long manager,
            @RequestParam(value = "totalCredits", required = false) Integer totalCredits,
            @RequestParam(value = "remainingCredits", required = false) Integer remainingCredits
    ) {

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

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/api/v1/user/{id}")
    public UserResponse fetchUser(@PathVariable Long id){
        User user = userService.getUserById(id);
        return new UserResponse(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/user")
    public UserResponse createUser(@RequestBody UserRequest userRequest){
        User user =  userService.createUser(userRequest);
        return new UserResponse(user);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/api/v1/user/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UserEditRequest userEditRequest
    ) {
        User user = userService.updateUser(id,userEditRequest);
        return new UserResponse(user);
    }
}
