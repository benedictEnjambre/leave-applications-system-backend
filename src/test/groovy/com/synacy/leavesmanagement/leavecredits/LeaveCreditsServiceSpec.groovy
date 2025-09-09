package com.synacy.leavesmanagement.leavecredits

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class LeaveCreditsServiceSpec extends Specification {

         LeaveCreditsRepository leaveCreditsRepository = Mock()
        LeaveCreditsService leaveCreditsService = new LeaveCreditsService(leaveCreditsRepository)


        @Unroll
        def "calculateRequestedDays counts only weekdays between #start and #end"() {
            expect:
            leaveCreditsService.calculateRequestedDays(start, end) == expected

            where:
            start                           | end                             || expected
            LocalDate.of(2025, 9, 8)        | LocalDate.of(2025, 9, 14)      || 5  // Mon-Sun (Mon-Fri counted)
            LocalDate.of(2025, 9, 6)        | LocalDate.of(2025, 9, 7)       || 0  // Weekend only (Sat-Sun)
            LocalDate.of(2025, 9, 10)       | LocalDate.of(2025, 9, 12)      || 3  // Wed-Fri
            LocalDate.of(2025, 9, 12)       | LocalDate.of(2025, 9, 10)      || 0  // startDate > endDate
            LocalDate.of(2025, 9, 12)       | LocalDate.of(2025, 9, 12)      || 1  // Single weekday (Friday)
            LocalDate.of(2025, 9, 13)       | LocalDate.of(2025, 9, 13)      || 0  // Single weekend day (Saturday)
            LocalDate.of(2025, 9, 14)       | LocalDate.of(2025, 9, 14)      || 0  // Single weekend day (Sunday)
            LocalDate.of(2025, 9, 15)       | LocalDate.of(2025, 9, 15)      || 1  // Single weekday (Monday)
        }


        def "deductCredits reduces remaining credits correctly"() {
            given:
            Long userId = 1L
            def credits = new LeaveCredits(totalCredits: 10, remainingCredits: 10)
            leaveCreditsRepository.findByUserId(userId) >> Optional.of(credits)

            when:
            int remaining = leaveCreditsService.deductCredits(userId,
                    LocalDate.of(2025, 9, 8),
                    LocalDate.of(2025, 9, 10))

            then:
            remaining == 7
            1 * leaveCreditsRepository.save(credits)
        }

        def "deductCredits throws InsufficientCreditsException when not enough credits"() {
            given:
            Long userId = 1L
            def credits = new LeaveCredits(totalCredits: 2, remainingCredits: 2)
            leaveCreditsRepository.findByUserId(userId) >> Optional.of(credits)

            when:
            leaveCreditsService.deductCredits(userId,
                    LocalDate.of(2025, 9, 8),
                    LocalDate.of(2025, 9, 12))

            then:
            thrown(InsufficientCreditsException)
        }

        def "deductCredits throws LeaveCreditsNotFoundException if user not found"() {
            given:
            Long userId = 99L
            leaveCreditsRepository.findByUserId(userId) >> Optional.empty()

            when:
            leaveCreditsService.deductCredits(userId,
                    LocalDate.of(2025, 9, 8),
                    LocalDate.of(2025, 9, 10))

            then:
            thrown(LeaveCreditsNotFoundException)
        }



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

        def "refundCredits throws LeaveCreditsNotFoundException if user not found"() {
            given:
            Long userId = 99L
            leaveCreditsRepository.findByUserId(userId) >> Optional.empty()

            when:
            leaveCreditsService.refundCredits(userId,
                    LocalDate.of(2025, 9, 8),
                    LocalDate.of(2025, 9, 10))

            then:
            thrown(LeaveCreditsNotFoundException)
        }


        def "newUserCredits sets defaults if null is passed"() {
            when:
            LeaveCredits credits = leaveCreditsService.newUserCredits(null, null)

            then:
            credits.totalCredits == 15
            credits.remainingCredits == 15
        }

        def "newUserCredits uses provided values if given"() {
            when:
           LeaveCredits credits = leaveCreditsService.newUserCredits(20, 10)

            then:
            credits.totalCredits == 20
            credits.remainingCredits == 10
        }


        def "updateCredits adjusts remainingCredits if it exceeds new totalCredits"() {
            given:
            LeaveCredits existing = new LeaveCredits(totalCredits: 10, remainingCredits: 8)

            when:
             LeaveCredits updated = leaveCreditsService.updateCredits(existing, 5, null)

            then:
            updated.totalCredits == 5
            updated.remainingCredits == 5
        }

        @Unroll
        def "updateCredits throws IllegalArgumentException when remainingCredits=#remaining"() {
            given:
            LeaveCredits existing = new LeaveCredits(totalCredits: 10, remainingCredits: 8)

            when:
            leaveCreditsService.updateCredits(existing, null, remaining)

            then:
            IllegalArgumentException ex = thrown(IllegalArgumentException)
            ex.message.contains("cannot")

            where:
            remaining << [15, -1]
        }

        def "updateCredits creates new LeaveCredits if existing is null"() {
            when:
            LeaveCredits updated = leaveCreditsService.updateCredits(null, 5, 3)

            then:
            updated.totalCredits == 5
            updated.remainingCredits == 3
        }
    }
