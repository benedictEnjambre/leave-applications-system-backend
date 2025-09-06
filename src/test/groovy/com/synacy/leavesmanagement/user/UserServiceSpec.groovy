package com.synacy.leavesmanagement.user

import com.synacy.leavesmanagement.leavecredits.LeaveCredits
import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class UserServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def leaveCreditsService = Mock(LeaveCreditsService)
    def userService = new UserService(userRepository, leaveCreditsService)

    def "CreateUser - success"() {
        given:
        def request = new UserRequest("Alice", Role.HR, null, 10, 10)

        def credits = new LeaveCredits(totalCredits: 10, remainingCredits: 10)
        leaveCreditsService.newUserCredits(10, 10) >> credits
        userRepository.existsByName("Alice") >> false
        userRepository.save(_ as User) >> { User u -> u }

        when:
        def result = userService.createUser(request)

        then:
        result.name == "Alice"
        result.role == Role.HR
        result.leaveCredits.totalCredits == 10
    }

    def "CreateUser - duplicate name throws exception"() {
        given:
        def request = new UserRequest("Alice", Role.HR, null, 10, 10)
        userRepository.existsByName("Alice") >> true

        when:
        userService.createUser(request)

        then:
        thrown(DuplicateUserNameException)
    }

    def "FetchUsers - no filters"() {
        given:
        def pageable = PageRequest.of(0, 5)
        def users = [new User("Bob", Role.HR, null, new LeaveCredits(totalCredits: 5, remainingCredits: 5))]
        userRepository.findAll(pageable) >> new PageImpl<>(users)

        when:
        Page<User> result = userService.fetchUsers(5, 0, null, null, null)

        then:
        result.content.size() == 1
        result.content[0].name == "Bob"
    }

    def "GetUserById - found"() {
        given:
        def user = new User("Charlie", Role.HR, null, new LeaveCredits(totalCredits: 7, remainingCredits: 7))
        userRepository.findById(1L) >> Optional.of(user)

        when:
        def result = userService.getUserById(1L)

        then:
        result.name == "Charlie"
    }

    def "GetUserById - not found throws exception"() {
        given:
        userRepository.findById(99L) >> Optional.empty()

        when:
        userService.getUserById(99L)

        then:
        thrown(UserNotFoundException)
    }

    def "GetManager - success"() {
        given:
        def manager = new User("Manager", Role.MANAGER, null, new LeaveCredits(totalCredits: 10, remainingCredits: 10))
        manager.id = 1L
        def employee = new User("Emp", Role.EMPLOYEE, manager, new LeaveCredits(totalCredits: 5, remainingCredits: 5))

        userRepository.findById(1L) >> Optional.of(manager)

        when:
        def result = userService.getManager(employee)

        then:
        result.name == "Manager"
    }

    def "GetManager - employee has no manager"() {
        given:
        def employee = new User("Emp", Role.EMPLOYEE, null, new LeaveCredits(totalCredits: 5, remainingCredits: 5))

        when:
        userService.getManager(employee)

        then:
        thrown(ManagerNotFoundException)
    }

    def "GetHR - success"() {
        given:
        def hr = new User("HR Guy", Role.HR, null, new LeaveCredits(totalCredits: 12, remainingCredits: 12))
        userRepository.findByRole(Role.HR) >> Optional.of(hr)

        when:
        def result = userService.getHR()

        then:
        result.role == Role.HR
    }

    def "GetHR - not found throws exception"() {
        given:
        userRepository.findByRole(Role.HR) >> Optional.empty()

        when:
        userService.getHR()

        then:
        thrown(RoleNotFoundException)
    }
}
