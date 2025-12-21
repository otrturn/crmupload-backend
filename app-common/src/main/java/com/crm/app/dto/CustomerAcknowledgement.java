package com.crm.app.dto;

public record CustomerAcknowledgement(
        Long customerId,
        boolean isAgbAccepted,
        boolean isEntrepreneur,
        boolean requestImmediateServiceStart,
        boolean acknowledgeWithdrawalLoss,
        String termsVersion,
        String ipAddress,
        String userAgent) {
}

