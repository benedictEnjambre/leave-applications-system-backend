package com.synacy.leavesmanagement.user

import com.synacy.leavesmanagement.leavecredits.LeaveCredits
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import spock.lang.Specification

class UserServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def userService = new UserService(userRepository)

    def "CreateUser"() {
        given:
        def request = new UserRequest("Alice", Role.HR, null, new LeaveCredits(10, 10))

        when:
        def result = userService.createUser(request)

        then:
        1 * userRepository.existsByName("Alice") >> false
        1 * userRepository.save(_ as User) >> { User u -> u }
        result.name == "Alice"
    }

    def "FetchUsers"() {
        given:
        def pageable = PageRequest.of(0, 5)
        def users = [new User("Bob", Role.HR, null, new LeaveCredits(5, 5))]
        userRepository.findAll(pageable) >> new PageImpl<>(users)

        when:
        Page<User> result = userService.fetchUsers(5, 0, null, null, null)

        then:
        result.content.size() == 1
        result.content[0].name == "Bob"
    }

    def "GetUserById"() {
        given:
        def user = new User("Charlie", Role.HR, null, new LeaveCredits(7, 7))
        userRepository.findById(1L) >> Optional.of(user)

        when:
        def result = userService.getUserById(1L)

        then:
        result.name == "Charlie"
    }

    def "GetManager"() {
        given:
        def manager = new User("Manager", Role.HR, null, new LeaveCredits(10, 10))
        def employee = new User("Emp", Role.EMPLOYEE, manager, new LeaveCredits(5, 5))
        userRepository.findById(manager.id) >> Optional.of(manager)

        when:
        def result = userService.getManager(employee)

        then:
        result.name == "Manager"
    }

    def "GetHR"() {
        given:
        def hr = new User("HR Guy", Role.HR, null, new LeaveCredits(12, 12))
        userRepository.findByRole(Role.HR) >> Optional.of(hr)

        when:
        def result = userService.getHR()

        then:
        result.role == Role.HR
    }
}

