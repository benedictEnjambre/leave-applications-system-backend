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

//    // Create leave credits for a new user
//    public LeaveCredits createCreditsForUser(Long userId, int totalCredits) {
//        LeaveCredits credits = new LeaveCredits();
//        credits.setUserId(userId);
//        credits.setTotalCredits(totalCredits);
//        credits.setRemainingCredits(totalCredits);
//        return leaveCreditsRepository.save(credits);
//    }

    // Count working days (excluding weekends)
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

    // Deduct credits when leave is approved
    public int deductCreditsForApprovedLeave(Long userId, LocalDate startDate, LocalDate endDate) {
        LeaveCredits credits = leaveCreditsRepository.findByUserId(userId)
                .orElseThrow(() -> new LeaveCreditsNotFoundException(userId));

        int daysRequested = calculateRequestedDays(startDate, endDate);

        if (credits.getRemainingCredits() < daysRequested) {
            throw new InsufficientCreditsException(userId, daysRequested, credits.getRemainingCredits());
        }

        credits.setRemainingCredits(credits.getRemainingCredits() - daysRequested);
        leaveCreditsRepository.save(credits);

        return credits.getRemainingCredits();
    }

    // Restore credits if leave is canceled
    public void restoreCreditsForCanceledLeave(Long userId, LocalDate startDate, LocalDate endDate) {
        LeaveCredits credits = leaveCreditsRepository.findByUserId(userId)
                .orElseThrow(() -> new LeaveCreditsNotFoundException(userId));

        int daysToRestore = calculateRequestedDays(startDate, endDate);

        int newBalance = credits.getRemainingCredits() + daysToRestore;
        if (newBalance > credits.getTotalCredits()) {
            newBalance = credits.getTotalCredits();
        }

        credits.setRemainingCredits(newBalance);
        leaveCreditsRepository.save(credits);
    }
}
