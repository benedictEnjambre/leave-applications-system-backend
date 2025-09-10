package com.synacy.leavesmanagement.leaveapplication

import com.synacy.leavesmanagement.leavecredits.InsufficientCreditsException
import com.synacy.leavesmanagement.leavecredits.LeaveCreditsService
import com.synacy.leavesmanagement.user.User
import com.synacy.leavesmanagement.user.Role
import com.synacy.leavesmanagement.user.UserService
import com.synacy.leavesmanagement.web.apierror.ResourceNotFoundException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable


import java.time.LocalDate
import spock.lang.Specification

class LeaveApplicationServiceSpec extends Specification {

    LeaveCreditsService leaveCreditsService
    LeaveApplicationService leaveApplicationService
    LeaveApplicationRepository leaveApplicationRepository
    UserService userService

    User hr
    User manager
    User employee

    def setup(){
        leaveCreditsService = Mock()
        leaveApplicationRepository = Mock()
        userService = Mock()
        leaveApplicationService = new LeaveApplicationService(leaveApplicationRepository, leaveCreditsService, userService)

        hr = new User()
        hr.setId(1L)
        hr.setName("HR")
        hr.setRole(Role.HR)

        manager = new User()
        manager.setName("Manager")
        manager.setId(2L)
        manager.setRole(Role.MANAGER)
        manager.setManager(null)

        employee = new User()
        employee.setId(3L)
        employee.setName("Employee")
        employee.setRole(Role.EMPLOYEE)
        employee.setManager(manager)

        userService.getHR() >> hr
    }

    def "applyLeave should return a valid leave application"() {
        given:
        def request = new LeaveApplicationRequest(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 5),
                "Going on vacation"
        )

        userService.getUserById(employee.getId()) >> employee
        leaveCreditsService.calculateRequestedDays(request.startDate, request.endDate) >> 5
        leaveCreditsService.deductCredits(employee.getId(), request.startDate, request.endDate) >> 5
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        def result = leaveApplicationService.applyLeave(employee.getId(), request)

        then:
        result.employee == employee
        result.approver == manager
        result.totalDays == 5
        result.remarks == "Going on vacation"
    }

    def "applyLeave should save the leave application with correct fields"() {
        given:
        def request = new LeaveApplicationRequest(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 5),
                "Going on vacation"
        )

        userService.getUserById(employee.getId()) >> employee
        leaveCreditsService.calculateRequestedDays(request.startDate, request.endDate) >> 5
        leaveCreditsService.deductCredits(employee.getId(), request.startDate, request.endDate) >> 5
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        leaveApplicationService.applyLeave(employee.getId(), request)

        then:
        1 * leaveApplicationRepository.save({ LeaveApplication newLeaveApplication ->
            assert newLeaveApplication.employee == employee
            assert newLeaveApplication.approver == manager
            assert newLeaveApplication.totalDays == 5
            assert newLeaveApplication.remarks == "Going on vacation"
            return true
        })
    }


    def "applyLeave should throw an exception if user doesn't exist"(){
        given:
        userService.getUserById(44L) >> { throw new ResourceNotFoundException("404 NOT_FOUND", "This user doesn't exits") }

        when:
        leaveApplicationService.applyLeave(44L, new LeaveApplicationRequest(LocalDate.now(), LocalDate.now(), "Test"))

        then:
        thrown(ResourceNotFoundException)
    }

    def "applyLeave should fallback approver to HR when manager has no manager"() {
        given:
        def request = new LeaveApplicationRequest(
                LocalDate.of(2025, 9, 8),
                LocalDate.of(2025, 9, 12),
                "Going on vacation"
        )

        userService.getUserById(manager.getId()) >> manager
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        def result = leaveApplicationService.applyLeave(manager.getId(), request)

        then:
        result.approver == hr
    }

    def "applyLeave should throw InsufficientCreditsException when user has insufficient leave"() {
        given:
        def request = new LeaveApplicationRequest(
                LocalDate.of(2025, 9, 8),
                LocalDate.of(2025, 9, 12),
                "Going on vacation"
        )
        userService.getUserById(employee.getId()) >> employee
        leaveCreditsService.calculateRequestedDays(request.startDate, request.endDate) >> 5
        leaveCreditsService.deductCredits(employee.getId(), request.startDate, request.endDate) >> {
            throw new InsufficientCreditsException("User " + userId + "requested  " + daysRequested + " days but only has " + remainingDays + "credits left.")
        }

        when:
        leaveApplicationService.applyLeave(employee.getId(), request)

        then:
        thrown(InsufficientCreditsException)
    }

    def "cancelLeave should cancel leave and refund credits when employee cancels own leave"() {
        given:
        def leaveApplication = new LeaveApplication(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 5),
                "Trip"
        )
        leaveApplication.setId(100L)
        leaveApplication.setEmployee(employee)
        leaveApplication.setStatus(LeaveStatus.PENDING)

        leaveApplicationRepository.findById(100L) >> Optional.of(leaveApplication)
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        def result = leaveApplicationService.cancelLeave(employee.getId(), 100L)

        then:
        result.status == LeaveStatus.CANCELLED
        1 * leaveCreditsService.refundCredits(employee.getId(), leaveApplication.startDate, leaveApplication.endDate)
    }

    def "cancelLeave should throw AccessDeniedException when user tries to cancel another user's leave"() {
        given:
        def leaveApplication = new LeaveApplication(LocalDate.now(), LocalDate.now(), "Test")
        leaveApplication.setId(200L)
        leaveApplication.setEmployee(employee)

        leaveApplicationRepository.findById(200L) >> Optional.of(leaveApplication)

        when:
        leaveApplicationService.cancelLeave(manager.getId(), 200L)

        then:
        thrown(AccessDeniedException)
    }

    def "cancelLeave should throw ResourceNotFoundException when leave does not exist"() {
        given:
        leaveApplicationRepository.findById(400L) >> Optional.empty()

        when:
        leaveApplicationService.cancelLeave(employee.getId(), 400L)

        then:
        thrown(ResourceNotFoundException)
    }

    def "approveLeave should allow direct manager to approve leave"() {
        given:
        def leaveApplication = new LeaveApplication(LocalDate.now(), LocalDate.now(), "Test")
        leaveApplication.setEmployee(employee)

        leaveApplicationRepository.findById(1L) >> Optional.of(leaveApplication)
        userService.getUserById(manager.getId()) >> manager
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        def result = leaveApplicationService.approveLeave(manager.getId(), 1L)

        then:
        result.status == LeaveStatus.APPROVED
    }

    def "approveLeave should throw AccessDeniedException when non-manager non-HR tries"() {
        given:
        def leaveApplication = new LeaveApplication(LocalDate.now(), LocalDate.now(), "Test")
        leaveApplication.setEmployee(employee)

        def anotherEmployee = new User(id: 4L, role: Role.EMPLOYEE)

        leaveApplicationRepository.findById(1L) >> Optional.of(leaveApplication)
        userService.getUserById(anotherEmployee.getId()) >> anotherEmployee

        when:
        leaveApplicationService.approveLeave(anotherEmployee.getId(), 1L)

        then:
        thrown(AccessDeniedException)
    }

    def "rejectLeave should allow direct manager to reject leave"() {
        given:
        def leaveApplication = new LeaveApplication(LocalDate.now(), LocalDate.now(), "Test")
        leaveApplication.setEmployee(employee)

        leaveApplicationRepository.findById(1L) >> Optional.of(leaveApplication)
        userService.getUserById(manager.getId()) >> manager
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        def result = leaveApplicationService.rejectLeave(manager.getId(), 1L)

        then:
        result.status == LeaveStatus.REJECTED
        1 * leaveCreditsService.refundCredits(employee.getId(), leaveApplication.startDate, leaveApplication.endDate)
    }

    def "rejectLeave should allow HR to reject leave and refund credits"() {
        given:
        def leaveApplication = new LeaveApplication(LocalDate.now(), LocalDate.now(), "Test")
        leaveApplication.setEmployee(employee)

        leaveApplicationRepository.findById(4L) >> Optional.of(leaveApplication)
        userService.getUserById(hr.getId()) >> hr
        leaveApplicationRepository.save(_ as LeaveApplication) >> { LeaveApplication app -> app }

        when:
        def result = leaveApplicationService.rejectLeave(hr.getId(), 4L)

        then:
        result.status == LeaveStatus.REJECTED
        1 * leaveCreditsService.refundCredits(employee.getId(), leaveApplication.startDate, leaveApplication.endDate)
    }

    def "fetchOwnLeaveApplication should allow employee to fetch their own leave"() {
        given:
        def leave1 = new LeaveApplication(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5), "Vacation")
        leave1.setEmployee(employee)
        leave1.setApprover(manager)

        def leave2 = new LeaveApplication(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 3), "Medical")
        leave2.setEmployee(employee)
        leave2.setApprover(manager)

        def pageResult = new PageImpl<>([leave1, leave2])

        userService.getUserById(employee.getId()) >> employee
        leaveApplicationRepository.findByEmployee(employee, _ as Pageable) >> pageResult

        when:
        def result = leaveApplicationService.fetchOwnLeaveApplication(employee.getId(), 1, 10)

        then:
        result.content.size() == 2
    }


    def "fetchOwnLeaveApplication should throw AccessDeniedException for invalid role"() {
        given:
        def outsider = new User(id: 88L, role: null)
        userService.getUserById(outsider.getId()) >> outsider

        when:
        leaveApplicationService.fetchOwnLeaveApplication(outsider.getId(), 1, 10)

        then:
        thrown(AccessDeniedException)
    }

    def "fetchTeamLeaveApplication should allow manager to fetch only their team's leave applications"() {
        given:
        def employee1 = new User(id: 100L, name: "Team Employee", role: Role.EMPLOYEE, manager: manager)
        def leaveApp1 = new LeaveApplication(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 2), "Team Vacation")
        leaveApp1.setEmployee(employee1)
        leaveApp1.setApprover(manager)

        def otherManager = new User(id: 200L, name: "Other Manager", role: Role.MANAGER)
        def employee2 = new User(id: 101L, name: "Other Employee", role: Role.EMPLOYEE, manager: otherManager)
        def leaveApp2 = new LeaveApplication(LocalDate.of(2025, 9, 3), LocalDate.of(2025, 9, 4), "Other Vacation")
        leaveApp2.setEmployee(employee2)
        leaveApp2.setApprover(otherManager)

        def pageData = new PageImpl([leaveApp1])

        userService.getUserById(manager.getId()) >> manager
        leaveApplicationRepository.findByApprover(manager, _ as Pageable) >> pageData

        when:
        def result = leaveApplicationService.fetchTeamLeaveApplication(manager.getId(), 1, 10)

        then:
        result.content.size() == 1
        result.content[0].employee == employee1
        result.content[0].approver == manager
    }

    def "fetchAllLeaveApplication should allow HR to fetch all leave applications with data"() {
        given:
        def leaveApp1 = new LeaveApplication(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 3), "Vacation")
        def leaveApp2 = new LeaveApplication(LocalDate.of(2025, 9, 10), LocalDate.of(2025, 9, 12), "Conference")

        def pageData = new PageImpl([leaveApp1, leaveApp2])

        userService.getUserById(hr.getId()) >> hr
        leaveApplicationRepository.findAll(_ as Pageable) >> pageData

        when:
        def result = leaveApplicationService.fetchAllLeaveApplication(hr.getId(), 1, 10)

        then:
        result.content.size() == 2
        result.content.containsAll([leaveApp1, leaveApp2])
    }
}
