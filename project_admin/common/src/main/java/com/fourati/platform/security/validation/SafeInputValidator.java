package com.fourati.platform.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Constraint validator for @SafeInput.
 * Shares the same pattern sets as SqlInjectionFilter so behaviour is consistent
 * whether the check triggers at the filter layer or the bean validation layer.
 */
public class SafeInputValidator implements ConstraintValidator<SafeInput, String> {

    private boolean allowHtml;
    private boolean allowSql;

    // SQL injection patterns 

    private static final List<Pattern> SQL_PATTERNS = List.of(
        compile("('.+--)|(--)|(;)"),
        compile("\\b(UNION)(\\s)+(SELECT|ALL)\\b"),
        compile("\\b(AND|OR)\\s+\\d+\\s*=\\s*\\d+"),
        compile("\\b(DROP|DELETE|INSERT|UPDATE|EXEC|EXECUTE|TRUNCATE|ALTER|CREATE)\\b"),
        compile("\\b(xp_|sp_|sys\\.)"),
        compile("(/\\*[\\s\\S]*?\\*/)|(--[^\\n]*)"),
        compile("0x[0-9a-fA-F]+"),
        compile("\\b(SLEEP|WAITFOR\\s+DELAY|BENCHMARK)\\b"),
        compile("\\bCHAR\\s*\\(\\s*\\d+"),
        compile(";\\s*(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE)")
    );

    // XSS patterns 

    private static final List<Pattern> XSS_PATTERNS = List.of(
        compile("<\\s*script[^>]*>"),
        compile("javascript\\s*:"),
        compile("on\\w+\\s*="),
        compile("<\\s*(iframe|object|embed|form)[^>]*>"),
        compile("&#[xX]?[0-9a-fA-F]+;"),
        compile("expression\\s*\\("),
        compile("vbscript\\s*:")
    );

    @Override
    public void initialize(SafeInput annotation) {
        this.allowHtml = annotation.allowHtml();
        this.allowSql  = annotation.allowSql();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;

        String decoded = decode(value);

        if (!allowSql) {
            for (Pattern p : SQL_PATTERNS) {
                if (p.matcher(decoded).find()) {
                    buildMessage(ctx, "Input contains a SQL injection pattern");
                    return false;
                }
            }
        }

        if (!allowHtml) {
            for (Pattern p : XSS_PATTERNS) {
                if (p.matcher(decoded).find()) {
                    buildMessage(ctx, "Input contains an XSS pattern");
                    return false;
                }
            }
        }

        return true;
    }

    private String decode(String input) {
        try {
            String decoded = URLDecoder.decode(input, StandardCharsets.UTF_8);
            return decoded.replaceAll("&#?[xX]?[0-9a-fA-F]+;", " ");
        } catch (Exception e) {
            return input;
        }
    }

    private void buildMessage(ConstraintValidatorContext ctx, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private static Pattern compile(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }
}
