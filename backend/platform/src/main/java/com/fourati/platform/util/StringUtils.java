package com.fourati.platform.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * String utilities beyond what Apache Commons / JDK provide.
 */
public final class StringUtils {

	private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s+]");
	private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");

    private StringUtils() {}

    /** Returns true if value is null, empty, or only whitespace. */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Returns defaultValue when value is blank, otherwise trims and returns it. */
    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    /**
     * Converts any string to a URL-safe slug.
     * "Hello World! Ça va?" → "hello-world-ca-va"
     */
    public static String slugify(String input) {
        if (isBlank(input)) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return MULTI_DASH.matcher(NON_LATIN.matcher(WHITESPACE.matcher(normalized.toLowerCase()).replaceAll("-")).replaceAll("")
        ).replaceAll("-").replaceAll("^-|-$", "");
    }

    /**
     * Truncates to maxLength, appending "…" if cut.
     * truncate("Hello World", 7) → "Hello W…"
     */
    public static String truncate(String value, int maxLength) {
		if (isBlank(value) || value.length() <= maxLength)
			return value;
        return value.substring(0, maxLength) + "…";
    }

    /**
     * Masks an email address for safe logging.
     * "john.smith@outlook.com" → "jo***@outlook.com"
     */
    public static String maskEmail(String email) {
		if (isBlank(email) || !email.contains("@"))
			return "***";
		String[] parts = email.split("@", 2);
		String local = parts[0];
		String visible = local.length() > 2 ? local.substring(0, 2) : local.substring(0, 1);
		return visible + "***@" + parts[1];
    }

    /**
     * Masks all but the last 4 digits of a card/ID number.
     * "1234567890123456" → "************3456"
     */
	public static String maskNumber(String value, int visibleSuffix) {
		if (isBlank(value) || value.length() <= visibleSuffix)
			return value;
		return "*".repeat(value.length() - visibleSuffix) + value.substring(value.length() - visibleSuffix);
	}

    /**
     * Capitalizes the first letter of each word.
     * "hello world" → "Hello World"
     */
    public static String toTitleCase(String input) {
		if (isBlank(input))
			return input;
		return Pattern.compile("\\b(\\w)").matcher(input.toLowerCase()).replaceAll(m -> m.group().toUpperCase());
    }

    /**
     * Converts camelCase to snake_case.
     * "myFieldName" → "my_field_name"
     */
	public static String toSnakeCase(String camel) {
		if (isBlank(camel))
			return camel;
		return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}

    /**
     * Pads a string on the left to the given width.
     * leftPad("42", 6, '0') → "000042"
     */
	public static String leftPad(String value, int width, char pad) {
		if (value == null)
			value = "";
		if (value.length() >= width)
			return value;
		return String.valueOf(pad).repeat(width - value.length()) + value;
	}
}
