package com.synacy.leavesmanagement.web.apierror;

import lombok.AccessLevel;
import lombok.Getter;

public class ResourceNotFoundException extends RuntimeException {
    @Getter(AccessLevel.PACKAGE)
    private final Long errorCode;

    @Getter(AccessLevel.PACKAGE)
    private final String errorMessage;

    public ResourceNotFoundException(Long errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
