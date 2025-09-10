package com.synacy.leavesmanagement.user

import com.synacy.leavesmanagement.leavecredits.LeaveCredits
import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService
import com.synacy.leavesmanagement.web.apierror.InvalidOperationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class UserServiceSpec extends Specification {

    UserRepository userRepository
    LeaveCreditsService leaveCreditsService
    UserService userService

    void setup() {
        userRepository = Mock()
        leaveCreditsService = Mock()
        userService = new UserService(userRepository, leaveCreditsService)
    }

    def "createUser should succeed when valid request is given"() {
        given:
        def request = new UserRequest("Alice", Role.HR, null, 10, 10)
        def credits = new LeaveCredits(totalCredits: 10, remainingCredits: 10)

        leaveCreditsService.newUserCredits(10, 10) >> credits
        userRepository.save(_ as User) >> { User u -> u }

        when:
        def result = userService.createUser(request)

        then:
        result.name == "Alice"
        result.role == Role.HR
        result.leaveCredits.totalCredits == 10
    }

    def "createUser should throw IllegalArgumentException when name is empty"() {
        given:
        def request = new UserRequest("", Role.HR, null, 10, 10)

        when:
        userService.createUser(request)

        then:
        thrown(IllegalArgumentException)
    }

    def "updateUser should throw InvalidOperationException if editor is not HR"() {
        given:
        def editRequest = new UserEditRequest(
                editorId: 99L,
                name: "NewName",
                role: Role.EMPLOYEE,
                managerId: null,
                totalCredits: 10,
                remainingCredits: 5
        )

        def nonHr = new User("John", Role.EMPLOYEE, null, new LeaveCredits(totalCredits: 10, remainingCredits: 10)
        )

        userRepository.findById(99L) >> Optional.of(nonHr)

        when:
        userService.updateUser(1L, editRequest)

        then:
        def e = thrown(InvalidOperationException)
        e.message.contains("ONLY HR ADMIN")
    }

    def "fetchUsers should return all users when no filters applied"() {
        given:
        def pageable = PageRequest.of(0, 5)
        def users = [new User("Bob", Role.HR, null, new LeaveCredits(totalCredits: 10, remainingCredits: 10))]
        userRepository.findAll(pageable) >> new PageImpl<>(users)

        when:
        Page<User> result = userService.fetchUsers(5, 0, null, null, null)

        then:
        result.content.size() == 1
        result.content[0].name == "Bob"
    }

    def "getUserById should return user if found"() {
        given:
        def user = new User("Charlie", Role.HR, null, new LeaveCredits(totalCredits: 10, remainingCredits: 10)
        )
        userRepository.findById(1L) >> Optional.of(user)

        when:
        def result = userService.getUserById(1L)

        then:
        result.name == "Charlie"
    }

    def "getUserById should throw exception if not found"() {
        given:
        userRepository.findById(99L) >> Optional.empty()

        when:
        userService.getUserById(99L)

        then:
        thrown(UserNotFoundException)
    }

    def "getManager should return manager if employee has one"() {
        given:
        LeaveCredits leaveCredits = new LeaveCredits(totalCredits: 10, remainingCredits: 10)
        LeaveCredits leaveCredits2 = new LeaveCredits(totalCredits: 5, remainingCredits: 5)

        def manager = new User("Manager", Role.MANAGER, null, leaveCredits)
        manager.id = 1L
        def employee = new User("Emp", Role.EMPLOYEE, manager, leaveCredits2)

        userRepository.findById(1L) >> Optional.of(manager)

        when:
        def result = userService.getManager(employee)

        then:
        result.name == "Manager"
    }

    def "getManager should throw if employee has no manager"() {
        given:
        LeaveCredits leaveCredits2 = new LeaveCredits(totalCredits: 5, remainingCredits: 5)
        def employee = new User("Emp", Role.EMPLOYEE, null, leaveCredits2)

        when:
        userService.getManager(employee)

        then:
        thrown(ManagerNotFoundException)
    }

    def "getHR should return HR user if found"() {
        given:
        LeaveCredits leaveCredits3 = new LeaveCredits(totalCredits: 12, remainingCredits: 12)
        def hr = new User("HR Guy", Role.HR, null, leaveCredits3)
        userRepository.findByRole(Role.HR) >> Optional.of(hr)

        when:
        def result = userService.getHR()

        then:
        result.role == Role.HR
    }

    def "getHR should throw exception if not found"() {
        given:
        userRepository.findByRole(Role.HR) >> Optional.empty()

        when:
        userService.getHR()

        then:
        thrown(RoleNotFoundException)
    }
}
