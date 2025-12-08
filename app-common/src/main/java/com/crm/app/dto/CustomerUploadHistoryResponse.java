package com.crm.app.dto;

import java.io.Serializable;
import java.util.List;

public record CustomerUploadHistoryResponse(List<CustomerUploadHistory> customerUploadHistory) implements Serializable {
}