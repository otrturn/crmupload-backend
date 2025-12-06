package com.crm.app.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public record ConsumerUploadHistoryResponse(List<ConsumerUploadHistory> consumerUploadHistory) implements Serializable {
}