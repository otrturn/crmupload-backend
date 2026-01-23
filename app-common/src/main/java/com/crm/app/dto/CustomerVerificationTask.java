package com.crm.app.dto;

public record CustomerVerificationTask(
        Long customerId,
        Long verificationTaskId,
        String taskDescription) {
}

