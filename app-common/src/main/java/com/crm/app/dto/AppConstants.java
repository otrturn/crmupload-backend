package com.crm.app.dto;

import java.util.List;

public class AppConstants {
    private AppConstants() {
    }

    public static final String PRODUCT_CRM_UPLOAD = "crm-upload";
    public static final String PRODUCT_DUPLICATE_CHECK = "duplicate-check";

    public static List<String> availableProducts() {
        return List.of(PRODUCT_CRM_UPLOAD, PRODUCT_DUPLICATE_CHECK);
    }
}
