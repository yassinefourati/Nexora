package com.fourati.platform.util;

import java.util.regex.Pattern;

/**
 * Standalone validation helpers — no Spring context needed.
 * Use these inside service/domain logic where Jakarta annotations can't be applied.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL = Pattern.compile(
        "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_E164 = Pattern.compile(
        "^\\+[1-9]\\d{6,14}$");

    private static final Pattern URL = Pattern.compile(
        "^(https?|ftp)://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern UUID = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]+$");

    private static final Pattern NUMERIC = Pattern.compile("^\\d+$");

    private ValidationUtils() {}

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    /** Validates E.164 international phone format: +21612345678 */
    public static boolean isValidPhoneE164(String value) {
        return value != null && PHONE_E164.matcher(value.trim()).matches();
    }

    public static boolean isValidUrl(String value) {
        return value != null && URL.matcher(value.trim()).matches();
    }

    public static boolean isValidUuid(String value) {
        return value != null && UUID.matcher(value.trim()).matches();
    }

    /** Validates URL-safe slug: lowercase letters, digits, hyphens. "my-item-123" */
    public static boolean isValidSlug(String value) {
        return value != null && SLUG.matcher(value.trim()).matches();
    }

    public static boolean isAlphanumeric(String value) {
        return value != null && ALPHANUMERIC.matcher(value).matches();
    }

    public static boolean isNumeric(String value) {
        return value != null && NUMERIC.matcher(value).matches();
    }

    /**
     * Validates string length is within [min, max] (inclusive), ignoring null.
     */
    public static boolean isLengthBetween(String value, int min, int max) {
		if (value == null)
			return false;
		int len = value.length();
		return len >= min && len <= max;
    }

    /**
     * Luhn algorithm — validates credit card numbers.
     * isValidLuhn("4532015112830366") → true
     */
	public static boolean isValidLuhn(String number) {
		if (StringUtils.isBlank(number))
			return false;
		String digits = number.replaceAll("\\D", "");
		int sum = 0;
		boolean alternate = false;
		for (int i = digits.length() - 1; i >= 0; i--) {
			int n = Character.getNumericValue(digits.charAt(i));
			if (alternate) {
				n *= 2;
				if (n > 9)
					n -= 9;
			}
			sum += n;
			alternate = !alternate;
		}
		return sum % 10 == 0;
	}
}
