package com.synacy.leavesmanagement.leavecredits;

import com.synacy.leavesmanagement.user.User;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class LeaveCreditsService {

    private final LeaveCreditsRepository leaveCreditsRepository;

    public LeaveCreditsService(LeaveCreditsRepository leaveCreditsRepository) {
        this.leaveCreditsRepository = leaveCreditsRepository;
    }

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

    //  Deduct credits when leave is requested
    public int deductCredits(Long userId, LocalDate startDate, LocalDate endDate) {
        LeaveCredits credits = getCreditsOrThrow(userId);
        int days = calculateRequestedDays(startDate, endDate);

        if (credits.getRemainingCredits() < days) {
            throw new InsufficientCreditsException(userId, days, credits.getRemainingCredits());
        }

        credits.setRemainingCredits(credits.getRemainingCredits() - days);
        leaveCreditsRepository.save(credits);

        return credits.getRemainingCredits();
    }

    // Restore credits if leave is canceled/rejected
    public int refundCredits(Long userId, LocalDate startDate, LocalDate endDate) {
        LeaveCredits credits = getCreditsOrThrow(userId);
        int days = calculateRequestedDays(startDate, endDate);

        int newBalance = credits.getRemainingCredits() + days;
        if (newBalance > credits.getTotalCredits()) {
            newBalance = credits.getTotalCredits();
        }

        credits.setRemainingCredits(newBalance);
        leaveCreditsRepository.save(credits);

        return credits.getRemainingCredits();
    }

    // 🔹 Factory method for initializing new user credits
    public LeaveCredits newUserCredits(Integer totalCredits, Integer remainingCredits) {
        LeaveCredits credits = new LeaveCredits();

        int defaultTotal = (totalCredits != null) ? totalCredits : 15;
        int defaultRemaining = (remainingCredits != null) ? remainingCredits : defaultTotal;

        credits.setTotalCredits(defaultTotal);
        credits.setRemainingCredits(defaultRemaining);

        return credits;
    }

    // Update existing user credits
    // 🔹 Update existing credits safely
    public LeaveCredits updateCredits(LeaveCredits existing,
                                      Integer totalCredits,
                                      Integer remainingCredits) {
        if (existing == null) {
            existing = new LeaveCredits();
        }

        if (totalCredits != null) {
            existing.setTotalCredits(totalCredits);
        }
        if (remainingCredits != null) {
            // prevent setting higher than total
            int adjusted = (totalCredits != null && remainingCredits > totalCredits)
                    ? totalCredits
                    : remainingCredits;
            existing.setRemainingCredits(adjusted);
        }

        return existing;
    }

    //  Private helper to fetch LeaveCredits or throw exception
    private LeaveCredits getCreditsOrThrow(Long userId) {
        return leaveCreditsRepository.findByUserId(userId)
                .orElseThrow(() -> new LeaveCreditsNotFoundException(userId));
    }
}
