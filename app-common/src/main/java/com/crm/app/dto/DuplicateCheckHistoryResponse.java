package com.crm.app.dto;

import java.io.Serializable;
import java.util.List;

public record DuplicateCheckHistoryResponse(List<DuplicateCheckHistory> duplicateCheckHistory) implements Serializable {
}