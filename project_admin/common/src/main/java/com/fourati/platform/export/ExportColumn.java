package com.fourati.platform.export;

import java.util.function.Function;

/**
 * Defines one column in an export: a header label and a value extractor.
 *
 * Usage:
 *   new ExportColumn<>("Name", ItemResponse::name)
 *   new ExportColumn<>("Active", r -> r.active() ? "Yes" : "No")
 */
public record ExportColumn<T>(String header, Function<T, ?> value) {

    public String extract(T row) {
        Object val = value.apply(row);
        return val != null ? val.toString() : "";
    }
}
