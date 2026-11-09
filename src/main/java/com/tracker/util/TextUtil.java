package com.tracker.util;

import java.math.BigDecimal;

public final class TextUtil {

    private TextUtil() {
    }

    /** "BLINKIT" or "blinkit" becomes "Blinkit" so one app is never split into two rows. */
    public static String tidy(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim().replaceAll("\\s+", " ");
        if (s.isEmpty() || !(s.equals(s.toUpperCase()) || s.equals(s.toLowerCase()))) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        for (String w : s.split(" ")) {
            if (w.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /** Guess how the payment was made from an SMS or bank statement line. */
    public static String detectPayment(String text) {
        String t = text == null ? "" : text.toLowerCase();
        if (t.contains("phonepe") || t.contains("@ybl") || t.contains("@ibl") || t.contains("@axl")) {
            return "PhonePe";
        }
        if (t.contains("google pay") || t.contains("gpay") || t.contains("@okicici")
                || t.contains("@okhdfcbank") || t.contains("@oksbi") || t.contains("@okaxis")) {
            return "Google Pay";
        }
        if (t.contains("card")) {
            return "Card";
        }
        return "UPI";
    }

    /** "Rs. 1,234.50" -> 1234.50. Returns null if there is no number. */
    public static BigDecimal parseAmount(String s) {
        if (s == null) {
            return null;
        }
        String cleaned = s.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty() || cleaned.equals(".")) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
