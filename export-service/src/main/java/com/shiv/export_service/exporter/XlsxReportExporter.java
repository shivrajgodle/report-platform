package com.shiv.export_service.exporter;

import com.aspose.cells.*;
import com.shiv.export_service.exception.ExportException;
import com.shiv.reportplatform.common.dto.ExportFormat;
import com.shiv.reportplatform.common.dto.ReportDto;
import com.shiv.reportplatform.common.dto.ReportRowDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Exports a report to an Excel (.xlsx) file using Aspose.Cells.
 *
 * Layout:
 *   Row 1: title (bold)
 *   Row 3: column headers (grey background)
 *   Row 4+: data rows
 *   Last row: TOTAL with a SUM formula
 *
 * Note: Aspose uses 0-based row/column indexes in cells.get(row, col),
 * but Excel formulas use 1-based references like "B4". Watch for this.
 */
@Component   // Spring finds it and injects it into ExportService automatically
public class XlsxReportExporter implements ReportExporter {

    private static final int HEADER_ROW = 2;      // 0-based, so this is Excel row 3
    private static final int FIRST_DATA_ROW = 3;  // 0-based, so this is Excel row 4
    private static final int NUMBER_FORMAT_2_DECIMALS = 4; // built-in Excel format "#,##0.00"

    @Override
    public ExportFormat supportedFormat() {
        return ExportFormat.XLSX;
    }

    @Override
    public byte[] export(ReportDto report) {
        try {
            Workbook workbook = new Workbook();                 // new workbook has one empty sheet
            Worksheet sheet = workbook.getWorksheets().get(0);
            sheet.setName("Report");
            Cells cells = sheet.getCells();

            writeTitle(workbook, cells, report.title());
            writeHeader(workbook, cells);
            int lastDataRow = writeRows(workbook, cells, report.rows());
            writeTotal(workbook, cells, lastDataRow);

            sheet.autoFitColumns();                             // size columns to their content

            // Save into memory instead of a temp file, so nothing is left on disk
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.save(out, SaveFormat.XLSX);
            return out.toByteArray();
        } catch (Exception e) {
            // Aspose throws a checked Exception, so we wrap it in our own runtime exception
            throw new ExportException("Failed to generate XLSX for report " + report.id(), e);
        }
    }

    private void writeTitle(Workbook workbook, Cells cells, String title) {
        Style style = workbook.createStyle();
        style.getFont().setBold(true);
        style.getFont().setSize(14);
        cells.get(0, 0).putValue(title);
        cells.get(0, 0).setStyle(style);
    }

    private void writeHeader(Workbook workbook, Cells cells) {
        Style style = workbook.createStyle();
        style.getFont().setBold(true);
        style.setForegroundColor(Color.getLightGray());
        style.setPattern(BackgroundType.SOLID);                 // without this the color is not shown

        String[] headers = {"Label", "Value", "Category"};
        for (int col = 0; col < headers.length; col++) {
            cells.get(HEADER_ROW, col).putValue(headers[col]);
            cells.get(HEADER_ROW, col).setStyle(style);
        }
    }


    /** @return the 0-based index of the last data row written */
    private int writeRows(Workbook workbook, Cells cells, @NotEmpty(message = "at lease one row is required") List<@Valid ReportRowDto> rows) {
        Style numberStyle = workbook.createStyle();
        numberStyle.setNumber(NUMBER_FORMAT_2_DECIMALS);

        int rowIndex = FIRST_DATA_ROW;
        for (ReportRowDto row : rows) {
            cells.get(rowIndex, 0).putValue(row.label());
            // putValue(double) keeps the cell numeric so Excel can sum it.
            // doubleValue() can lose precision on huge numbers, which is fine for display.
            cells.get(rowIndex, 1).putValue(row.value().doubleValue());
            cells.get(rowIndex, 1).setStyle(numberStyle);
            cells.get(rowIndex, 2).putValue(row.category() == null ? "" : row.category());
            rowIndex++;
        }
        return rowIndex - 1;
    }

    private void writeTotal(Workbook workbook, Cells cells, int lastDataRow) {
        Style boldNumber = workbook.createStyle();
        boldNumber.getFont().setBold(true);
        boldNumber.setNumber(NUMBER_FORMAT_2_DECIMALS);

        int totalRow = lastDataRow + 1;
        cells.get(totalRow, 0).putValue("TOTAL");
        // Excel rows are 1-based: first data row = FIRST_DATA_ROW + 1, last = lastDataRow + 1
        cells.get(totalRow, 1).setFormula("=SUM(B" + (FIRST_DATA_ROW + 1) + ":B" + (lastDataRow + 1) + ")");
        cells.get(totalRow, 1).setStyle(boldNumber);
    }


}
