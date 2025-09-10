package com.synacy.leavesmanagement.leavecredits

import com.synacy.leavesmanagement.user.User
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class LeaveCreditsServiceSpec extends Specification {

    private LeaveCreditsService leaveCreditsService
    private LeaveCreditsRepository leaveCreditsRepository

    def setup() {
        leaveCreditsRepository = Mock()
        leaveCreditsService = new LeaveCreditsService(leaveCreditsRepository)
    }
// calculateRequestedDays
    def "calculateRequestedDays working days only (Mon-Fri)"() {


        when:
        int days = leaveCreditsService.calculateRequestedDays(startDate, endDate)

        then:
        days == expected

        where:
        startDate                 | endDate                   || expected
        LocalDate.of(2025, 9, 8)  | LocalDate.of(2025, 9, 14) || 5
        LocalDate.of(2025, 9, 6)  | LocalDate.of(2025, 9, 7)  || 0
        LocalDate.of(2025, 9, 10) | LocalDate.of(2025, 9, 12) || 3
        LocalDate.of(2025, 9, 12) | LocalDate.of(2025, 9, 10) || 0
        LocalDate.of(2025, 9, 12) | LocalDate.of(2025, 9, 12) || 1
        LocalDate.of(2025, 9, 13) | LocalDate.of(2025, 9, 13) || 0
        LocalDate.of(2025, 9, 14) | LocalDate.of(2025, 9, 14) || 0
        LocalDate.of(2025, 9, 15) | LocalDate.of(2025, 9, 15) || 1
    }

// for deduct
    def "deducts credits successfully when user has enough balance"() {
        given:
        Long userId = 1L
        LeaveCredits credits = new LeaveCredits(totalCredits: 10, remainingCredits: 10)
        LocalDate start = LocalDate.of(2025, 9, 8)
        LocalDate end = LocalDate.of(2025, 9, 10)
        leaveCreditsRepository.findByUserId(userId) >> Optional.of(credits)

        when:
        int remaining = leaveCreditsService.deductCredits(userId, start, end)

        then:
        remaining == 7
        credits.remainingCredits == 7
        1 * leaveCreditsRepository.save(credits)
    }

    def "throws InsufficientCreditsException when requested days exceed balance"() {
        given:
        Long userId = 1L
        int initialTotalCredits = 2
        int initialRemainingCredits = 2
        LocalDate startDate = LocalDate.of(2025, 9, 8)
        LocalDate endDate = LocalDate.of(2025, 9, 10)

        LeaveCredits credits = new LeaveCredits(totalCredits: initialTotalCredits, remainingCredits: initialRemainingCredits)
        leaveCreditsRepository.findByUserId(userId) >> Optional.of(credits)

        when:
        leaveCreditsService.deductCredits(userId, startDate, endDate)

        then:
        thrown(InsufficientCreditsException)

        and:
        0 * leaveCreditsRepository.save(_)
    }


// for refund
    def "refundCredits increases remaining credits correctly but never exceeds total"() {
        given:
        Long userId = 1L
        LeaveCredits credits = new LeaveCredits(totalCredits: 10, remainingCredits: 5)
        leaveCreditsRepository.findByUserId(userId) >> Optional.of(credits)

        when:
        int remaining = leaveCreditsService.refundCredits(userId,
                LocalDate.of(2025, 9, 8),
                LocalDate.of(2025, 9, 10))

        then:
        remaining == 8
        1 * leaveCreditsRepository.save(credits)
    }


    def "refundCredits does not exceed totalCredits"() {
        given:
        Long userId = 1L
        LeaveCredits credits = new LeaveCredits(totalCredits: 10, remainingCredits: 9)
        leaveCreditsRepository.findByUserId(userId) >> Optional.of(credits)

        when:
        int remaining = leaveCreditsService.refundCredits(userId,
                LocalDate.of(2025, 9, 8),
                LocalDate.of(2025, 9, 10))

        then:
        remaining == 10
        1 * leaveCreditsRepository.save(credits)
    }
// newUseCredits
    def "newUSerCredits should use provided total and remaining credits"(){
        given:
        Integer total = 20
        Integer remaining = 20


        when:
        LeaveCredits credits = leaveCreditsService.newUserCredits(total, remaining)


        then:
        credits.totalCredits == 20
        credits.remainingCredits == 20
    }
 def "newUserCredits should default remaining credits to total when null"() {
     given:
     Integer total = 12
     Integer remaining = null

     when:
     LeaveCredits credits = leaveCreditsService.newUserCredits(total, remaining)

     then:
     credits.totalCredits == 12
     credits.remainingCredits == 12
 }
    def "newUserCredits should default total credits to 15 when null"() {
        given:
        Integer total = null
        Integer remaining = null

        when:
        LeaveCredits credits = leaveCreditsService.newUserCredits(total, remaining)

        then:
        credits.totalCredits == 15
        credits.remainingCredits == 15
    }
    def "updateCredits throws exception if remainingCredits < 0"() {
        given:
        LeaveCredits existing = new LeaveCredits(totalCredits: 10, remainingCredits: 8)

        when:
        leaveCreditsService.updateCredits(existing, null, -1)

        then:

        thrown(IllegalArgumentException)
    }


    def "deductCredits throws LeaveCreditsNotFoundException if user not found"() {
        given:
        Long userId = 99L
        LocalDate startDate = LocalDate.of(2025, 9, 8)
        LocalDate endDate = LocalDate.of(2025, 9, 10)
        leaveCreditsRepository.findByUserId(userId) >> Optional.empty()

        when:
        leaveCreditsService.deductCredits(userId, startDate, endDate)

        then:

        thrown(LeaveCreditsNotFoundException)
    }


    def "refundCredits throws LeaveCreditsNotFoundException if user not found"() {
        given:
        Long userId = 99L
        LocalDate startDate = LocalDate.of(2025, 9, 8)
        LocalDate endDate = LocalDate.of(2025, 9, 10)
        leaveCreditsRepository.findByUserId(userId) >> Optional.empty()

        when:
        leaveCreditsService.refundCredits(userId, startDate, endDate)

        then:
        thrown(LeaveCreditsNotFoundException)
    }
}






