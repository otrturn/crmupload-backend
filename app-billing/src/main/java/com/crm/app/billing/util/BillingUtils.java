package com.crm.app.billing.util;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

public class BillingUtils {

    private BillingUtils() {
    }

    public static String[] getDayRangeForSeletionOfDealers(String refdate) {
        String[] dayRange = new String[2];
        dayRange[0] = refdate.substring(6, 8);
        dayRange[1] = BillingUtils.isLastDayOfMonth(refdate) ? "31" : dayRange[0];
        return dayRange;
    }

    public static boolean isLastDayOfMonth(String refdate) {
        int month = Integer.parseInt(refdate.substring(4, 6));
        int day = Integer.parseInt(refdate.substring(6, 8));
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> day == 31;
            case 2 -> day == 28 || day == 29;
            case 4, 6, 9, 11 -> day == 30;
            default -> false;
        };
    }

    public static float getTextWidth(PDType1Font font, int fontSize,
                                     String text) throws IOException {
        return (font.getStringWidth(text) / 1000.0f) * fontSize;
    }

}
