package com.crm.app.dto;

import java.io.Serializable;
import java.util.List;

public record CrmUploadHistoryResponse(List<CrmUploadHistory> crmUploadHistory) implements Serializable {
}