package com.synacy.leavesmanagement.user

import com.synacy.leavesmanagement.leavecredits.LeaveCredits
import com.synacy.leavesmanagement.web.PageResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class UserControllerSpec extends Specification {

    UserController userController
    UserService userService

    void setup() {
        userService = Mock(UserService)
        userController = new UserController(userService)
    }

    def "fetchUser should return the response for the correct user given the id"() {
        given:
        def leaveCredits = new LeaveCredits(totalCredits: 10, remainingCredits: 10)

        def user = Mock(User)
        user.getId() >> 1L
        user.getName() >> "Alice"
        user.getLeaveCredits() >> leaveCredits
        user.getManager() >> null
        user.getRole() >> Role.EMPLOYEE

        userService.getUserById(1L) >> user

        when:
        UserResponse response = userController.fetchUser(1L)

        then:
        response.id == 1L
        response.name == "Alice"
        response.role == Role.EMPLOYEE
    }

  /*  def "createUser should create a new user and return UserResponse"() {
        given:
        UserRequest userRequest = Mock(UserRequest)
        User user = new User(name: "Bob", role: Role.EMPLOYEE)
        userService.createUser(userRequest) >> user

        when:
        UserResponse response = userController.createUser(userRequest)

        then:
        response.getName() == "Bob"
        response.getRole() == Role.EMPLOYEE
    }

    def "updateUser should update an existing user and return UserResponse"() {
        given:
        Long userId = 5L
        UserEditRequest editRequest = new UserEditRequest(
                99L, "Charlie", Role.MANAGER, null, 20, 15
        )
        User updatedUser = new User(name: "Charlie", role: Role.MANAGER)
        userService.updateUser(userId, editRequest) >> updatedUser

        when:
        UserResponse response = userController.updateUser(userId, editRequest)

        then:
        response.getName() == "Charlie"
        response.getRole() == Role.MANAGER
    }

    def "fetchUsers should return a page response of users"() {
        given:
        User u1 = new User(name: "Daisy", role: Role.HR)
        User u2 = new User(name: "Evan", role: Role.EMPLOYEE)
        List<User> users = [u1, u2]
        Page<User> page = new PageImpl<>(users, PageRequest.of(0, 2), users.size())
        userService.fetchUsers(2, 0, null, null, null) >> page

        when:
        PageResponse<UserResponse> response = userController.fetchUsers(2, 1, null, null, null)

        then:
        response.getTotal() == 2
        response.getContent()[0].getName() == "Daisy"
        response.getContent()[1].getName() == "Evan"
    }*/
}
