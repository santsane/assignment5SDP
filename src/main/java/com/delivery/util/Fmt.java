package com.delivery.util;

import java.util.Locale;

/**
 * Number formatting helpers. We force Locale.US so the output has a dot
 * as the decimal separator on every computer (some locales print 12,50).
 */
public final class Fmt {

    private Fmt() {
    }

    public static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    public static String dec1(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    public static String dec2(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
