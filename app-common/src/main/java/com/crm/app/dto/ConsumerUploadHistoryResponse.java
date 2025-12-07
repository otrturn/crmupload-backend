package com.crm.app.dto;

import java.io.Serializable;
import java.util.List;

public record ConsumerUploadHistoryResponse(List<ConsumerUploadHistory> consumerUploadHistory) implements Serializable {
}