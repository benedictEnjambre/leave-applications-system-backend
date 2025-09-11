    package com.synacy.leavesmanagement.leavecredits;

    public class InsufficientCreditsException extends RuntimeException {
        public InsufficientCreditsException(long userId, int daysRequested, int remainingDays) {
            super("User " + userId + "requested  " + daysRequested + " days but only has " + remainingDays + "credits left.");
        }
    }
