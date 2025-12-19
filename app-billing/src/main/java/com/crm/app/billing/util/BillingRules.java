package com.crm.app.billing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BillingRules {

    public static final int MONEY_SCALE = 2;
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private BillingRules() {
    }

    public static BigDecimal roundMoney(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING)
                : value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}