package com.synacy.leavesmanagement.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/v1/user")
    public UserResponse createUser(@RequestBody UserRequest userRequest){
        User user =  userService.createUser(userRequest);
        return new UserResponse(user);
    }

}
