package com.fourati.platform.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Number formatting and arithmetic utilities.
 */
public final class NumberUtils {

    private NumberUtils() {}

    /**
     * Rounds a BigDecimal to the given number of decimal places using HALF_UP.
     * round(new BigDecimal("2.345"), 2) → 2.35
     */
    public static BigDecimal round(BigDecimal value, int scale) {
		if (value == null)
			return null;
		return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /** Rounds a double, returns BigDecimal. */
    public static BigDecimal round(double value, int scale) {
        return round(BigDecimal.valueOf(value), scale);
    }

    /**
     * Formats a number as currency string.
     * formatCurrency(1234.5, "TND") → "TND 1 234,500"  (locale-dependent)
     * formatCurrency(1234.5, "USD") → "$1,234.50"
     */
    public static String formatCurrency(BigDecimal amount, String currencyCode) {
		if (amount == null)
			return "";
		NumberFormat fmt = NumberFormat.getCurrencyInstance();
		fmt.setCurrency(java.util.Currency.getInstance(currencyCode));
		return fmt.format(amount);
    }

    /**
     * Formats a number with grouping separators.
     * formatNumber(1234567.89, 2) → "1,234,567.89"
     */
    public static String formatNumber(BigDecimal value, int decimalPlaces) {
		if (value == null)
			return "";
		NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
		fmt.setMinimumFractionDigits(decimalPlaces);
		fmt.setMaximumFractionDigits(decimalPlaces);
		return fmt.format(value);
    }

    /**
     * Formats bytes as human-readable size.
     * formatBytes(1048576) → "1.0 MB"
     */
    public static String formatBytes(long bytes) {
		if (bytes < 1024)
			return bytes + " B";
		if (bytes < 1_048_576)
			return String.format("%.1f KB", bytes / 1_024.0);
		if (bytes < 1_073_741_824)
			return String.format("%.1f MB", bytes / 1_048_576.0);
		return String.format("%.2f GB", bytes / 1_073_741_824.0);
    }

    /**
     * Formats a percentage.
     * formatPercent(0.1234, 1) → "12.3%"
     */
    public static String formatPercent(double ratio, int decimalPlaces) {
		NumberFormat fmt = NumberFormat.getPercentInstance();
		fmt.setMinimumFractionDigits(decimalPlaces);
		fmt.setMaximumFractionDigits(decimalPlaces);
		return fmt.format(ratio);
    }

    /**
     * Safe integer parsing — returns defaultValue on invalid input.
     * parseInt("abc", 0) → 0
     */
    public static int parseInt(String value, int defaultValue) {
		if (value == null || value.isBlank())
			return defaultValue;
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
    }

    /**
     * Safe long parsing.
     */
    public static long parseLong(String value, long defaultValue) {
		if (value == null || value.isBlank())
			return defaultValue;
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
    }

    /**
     * Safe BigDecimal parsing.
     */
	public static BigDecimal parseBigDecimal(String value, BigDecimal defaultValue) {
		if (value == null || value.isBlank())
			return defaultValue;
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
    }

    /**
     * Clamps a value between min and max (inclusive).
     * clamp(150, 0, 100) → 100
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calculates percentage: what percent is part of total?
     * percentage(25, 200) → 12.5
     */
    public static double percentage(double part, double total) {
        if (total == 0) return 0;
        return (part / total) * 100;
    }
}
