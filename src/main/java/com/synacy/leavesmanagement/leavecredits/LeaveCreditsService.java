package com.synacy.leavesmanagement.leavecredits;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class LeaveCreditsService {

    private final LeaveCreditsRepository leaveCreditsRepository;

    public LeaveCreditsService(LeaveCreditsRepository leaveCreditsRepository) {
        this.leaveCreditsRepository = leaveCreditsRepository;
    }


//      Count working days (excluding weekends)
    public int calculateRequestedDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (!isWeekend(date.getDayOfWeek())) {
                workingDays++;
            }
        }
        return workingDays;
    }

    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }


//      Update leave credits: deduct when applying, restore if rejected

    public int updateCredits(Long userId, LocalDate startDate, LocalDate endDate, boolean isDeduction) {
        LeaveCredits credits = leaveCreditsRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new LeaveCreditsNotFoundException(userId));

        int days = calculateRequestedDays(startDate, endDate);

        if (isDeduction) {
            // Deduct immediately when applying
            if (credits.getRemainingCredits() < days) {
                throw new InsufficientCreditsException(userId, days, credits.getRemainingCredits());
            }
            credits.setRemainingCredits(credits.getRemainingCredits() - days);
        } else {
            // Restore if leave request is rejected
            int newBalance = credits.getRemainingCredits() + days;
            if (newBalance > credits.getTotalCredits()) {
                newBalance = credits.getTotalCredits();
            }
            credits.setRemainingCredits(newBalance);
        }

        leaveCreditsRepository.save(credits);
        return credits.getRemainingCredits();
    }
}
