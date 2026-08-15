package com.fourati.platform.export;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

/**
 * Generic export service — converts any list of objects to CSV, PDF, or JSON bytes.
 *
 * Usage from a controller:
 * <pre>
 *   var columns = List.of(
 *       new ExportColumn&lt;&gt;("ID",   r -&gt; r.id().toString()),
 *       new ExportColumn&lt;&gt;("Name", ItemResponse::name)
 *   );
 *   byte[] bytes = exportService.export(rows, columns, format, "Items");
 * </pre>
 *
 * Note: columns are used for CSV and PDF only. JSON serializes the full row object via Jackson.
 */
@RequiredArgsConstructor
public class ExportService {

    private final ObjectMapper objectMapper;

    public <T> byte[] export(List<T> rows, List<ExportColumn<T>> columns, ExportFormat format, String title) {
        return switch (format) {
            case CSV  -> toCsv(rows, columns);
            case PDF  -> toPdf(rows, columns, title);
            case JSON -> toJson(rows);
        };
    }

    //  CSV

    /**
     * Streaming CSV overload — writes directly to {@code out} without buffering all rows in memory.
     * Prefer this when exporting large datasets to an HTTP response stream.
     *
     * <pre>
     *   exportService.streamCsv(rows, columns, response.getOutputStream());
     * </pre>
     *
     * The caller is responsible for closing {@code out}.
     */
    public <T> void streamCsv(List<T> rows, List<ExportColumn<T>> columns, OutputStream out) {
        String[] headers = columns.stream().map(ExportColumn::header).toArray(String[]::new);
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            for (T row : rows) {
                Object[] values = columns.stream().map(c -> c.extract(row)).toArray();
                printer.printRecord(values);
            }
        } catch (IOException e) {
            throw new ExportException("CSV generation failed", e);
        }
    }

    private <T> byte[] toCsv(List<T> rows, List<ExportColumn<T>> columns) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String[] headers = columns.stream().map(ExportColumn::header).toArray(String[]::new);

        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader(headers).build())) {

            for (T row : rows) {
                Object[] values = columns.stream().map(c -> c.extract(row)).toArray();
                printer.printRecord(values);
            }
        } catch (IOException e) {
            throw new ExportException("CSV generation failed", e);
        }
        return out.toByteArray();
    }

    //  PDF 

    private <T> byte[] toPdf(List<T> rows, List<ExportColumn<T>> columns, String title) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            doc.add(new com.lowagie.text.Paragraph(title, titleFont));
            doc.add(new com.lowagie.text.Paragraph(" "));

            PdfPTable table = new PdfPTable(columns.size());
            table.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            for (ExportColumn<T> col : columns) {
                PdfPCell cell = new PdfPCell(new Phrase(col.header(), headerFont));
                cell.setBackgroundColor(new Color(41, 128, 185));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            boolean shade = false;
            for (T row : rows) {
                Color bg = shade ? new Color(235, 245, 251) : Color.WHITE;
                for (ExportColumn<T> col : columns) {
                    PdfPCell cell = new PdfPCell(new Phrase(col.extract(row), dataFont));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(5);
                    table.addCell(cell);
                }
                shade = !shade;
            }

            doc.add(table);
        } catch (Exception e) {
            throw new ExportException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    //  JSON 

    private <T> byte[] toJson(List<T> rows) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(rows);
        } catch (IOException e) {
            throw new ExportException("JSON generation failed", e);
        }
    }
}
