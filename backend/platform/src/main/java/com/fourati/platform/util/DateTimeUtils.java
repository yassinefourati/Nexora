package com.fourati.platform.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date/time utilities — all conversions assume UTC unless a zone is given explicitly.
 */
public final class DateTimeUtils {

    public static final ZoneId UTC = ZoneId.of("UTC");
	public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	public static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(UTC);

    private DateTimeUtils() {}

    /** Current UTC instant. */
    public static Instant nowUtc() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    /** Today's date in UTC. */
    public static LocalDate todayUtc() {
        return LocalDate.now(UTC);
    }

    /** Converts an Instant to a LocalDateTime in UTC. */
    public static LocalDateTime toUtcDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, UTC);
    }

    /** Converts an Instant to a LocalDateTime in the given zone. */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zone) {
        return LocalDateTime.ofInstant(instant, zone);
    }

    /** Converts a LocalDateTime (assumed UTC) to Instant. */
    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    /** Converts a LocalDate to start-of-day Instant in UTC. */
    public static Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay(UTC).toInstant();
    }

    /** Converts a LocalDate to end-of-day Instant in UTC (23:59:59.999). */
    public static Instant toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atZone(UTC).toInstant();
    }

    /** Human-friendly format: "04 Jun 2026 11:30" */
    public static String format(Instant instant) {
        return instant == null ? "" : DISPLAY.format(instant);
    }

    /** ISO-8601 format: "2026-06-04T11:30:00Z" */
    public static String formatIso(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    /** Returns true if the instant is in the past. */
    public static boolean isPast(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }

    /** Returns true if the instant is in the future. */
    public static boolean isFuture(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }

    /** Elapsed time between two instants as a human-readable string: "2h 15m", "45s". */
    public static String elapsed(Instant from, Instant to) {
		long secs = ChronoUnit.SECONDS.between(from, to);
		if (secs < 0)
			secs = -secs;
		if (secs < 60)
			return secs + "s";
		if (secs < 3600)
			return (secs / 60) + "m " + (secs % 60) + "s";
		long h = secs / 3600;
		long m = (secs % 3600) / 60;
		return h + "h " + (m > 0 ? m + "m" : "");
    }
}
