package com.fourati.platform.util;

import java.util.regex.Pattern;

/**
 * Input sanitization utilities.
 *
 * Use these when you want to CLEAN input rather than REJECT it.
 * For rejecting dangerous input use @SafeInput or SqlInjectionFilter.
 *
 * Rule of thumb:
 *   User-facing display text  → stripXss()
 *   Values going into SQL     → use parameterized queries — NEVER string concatenation
 *   Filenames                 → sanitizeFilename()
 *   Free-text search input    → stripSqlSpecialChars() + parameterized query
 */
public final class SanitizationUtils {

	private static final Pattern SQL_META = Pattern.compile("[';\\-\\-/*\\\\%_]");
	private static final Pattern XSS_SCRIPT = Pattern.compile("<\\s*script[^>]*>[\\s\\S]*?<\\s*/\\s*script\\s*>", Pattern.CASE_INSENSITIVE);
	private static final Pattern XSS_TAGS = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
	private static final Pattern XSS_EVENTS = Pattern.compile("on\\w+\\s*=\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
	private static final Pattern XSS_JS_PROTO = Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE);
	private static final Pattern UNSAFE_FILENAME = Pattern.compile("[^a-zA-Z0-9._\\-]");
	private static final Pattern MULTI_SPACE = Pattern.compile("\\s{2,}");
	private static final Pattern NULL_BYTES = Pattern.compile("\\x00");

    private SanitizationUtils() {}

    /**
     * Strips all HTML tags and XSS vectors from user input.
     * Safe for storing text that will be rendered in a browser.
     *
     * stripXss("<script>alert(1)</script>Hello") → "Hello"
     */
    public static String stripXss(String input) {
		if (input == null)
			return null;
		String v = NULL_BYTES.matcher(input).replaceAll("");
		v = XSS_SCRIPT.matcher(v).replaceAll("");
		v = XSS_EVENTS.matcher(v).replaceAll("");
		v = XSS_JS_PROTO.matcher(v).replaceAll("");
		v = XSS_TAGS.matcher(v).replaceAll("");
		return v.trim();
    }

    /**
     * Escapes SQL meta-characters so a value can be used in a LIKE clause.
     *
     * ⚠ This is NOT a substitute for parameterized queries.
     * Only use this when building dynamic LIKE patterns:
     *   String safe = SanitizationUtils.escapeSqlLike(userInput);
     *   repo.findByNameContaining(safe); // Spring Data still uses a prepared statement
     *
     * escapeSqlLike("50% off") → "50\% off"
     */
    public static String escapeSqlLike(String input) {
		if (input == null)
			return null;
		return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * Strips SQL special characters entirely.
     * Use when you need a clean alphanumeric string for identifiers.
     *
     * stripSqlSpecialChars("'; DROP TABLE users--") → " DROP TABLE users"
     */
    public static String stripSqlSpecialChars(String input) {
		if (input == null)
			return null;
		return SQL_META.matcher(input).replaceAll("").trim();
    }

    /**
     * HTML-encodes a string for safe rendering in HTML context.
     * Converts &, <, >, ", ' to their HTML entities.
     *
     * encodeHtml("<b>Hello & 'World'</b>") → "&lt;b&gt;Hello &amp; &#39;World&#39;&lt;/b&gt;"
     */
    public static String encodeHtml(String input) {
        if (input == null) 
        	return null;
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Sanitizes a filename: allows only alphanumeric, dots, hyphens, underscores.
     * Prevents path traversal and shell injection via filenames.
     *
     * sanitizeFilename("../../etc/passwd") → "......etcpasswd"
     * sanitizeFilename("my file (1).pdf") → "my_file__1_.pdf"
     */
    public static String sanitizeFilename(String filename) {
		if (filename == null)
			return "file";
        return UNSAFE_FILENAME.matcher(filename).replaceAll("_");
    }

    /**
     * Normalizes whitespace: collapses multiple spaces into one.
     * normalizeWhitespace("Hello   World") → "Hello World"
     */
    public static String normalizeWhitespace(String input) {
		if (input == null)
			return null;
		return MULTI_SPACE.matcher(input.trim()).replaceAll(" ");
    }

    /**
     * Removes null bytes (\0) which can truncate strings in some databases/filesystems.
     */
    public static String removeNullBytes(String input) {
		if (input == null)
			return null;
        return NULL_BYTES.matcher(input).replaceAll("");
    }

    /**
     * Applies all safe-for-storage transformations:
     *   1. Remove null bytes
     *   2. Strip XSS
     *   3. Normalize whitespace
     *
     * Use for general user-supplied text fields before storing.
     */
    public static String sanitize(String input) {
		if (input == null)
			return null;
        return normalizeWhitespace(stripXss(removeNullBytes(input)));
    }
}
