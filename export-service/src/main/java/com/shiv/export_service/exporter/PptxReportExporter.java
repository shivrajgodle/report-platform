package com.shiv.export_service.exporter;

import com.aspose.slides.*;
import com.shiv.export_service.exception.ExportException;
import com.shiv.reportplatform.common.dto.ExportFormat;
import com.shiv.reportplatform.common.dto.ReportDto;
import com.shiv.reportplatform.common.dto.ReportRowDto;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Exports a report to PowerPoint (.pptx) using Aspose.Slides.
 *
 * Deck structure:
 *   1. Title slide (title + description)
 *   2..N. Table slides (max ROWS_PER_SLIDE rows each, so long reports never overflow)
 *   Last. Column chart of the first MAX_CHART_POINTS rows
 *
 * All coordinates are in points on a 16:9 slide (720 x 405).
 * Note: Aspose.Slides collections use get_Item(index), not get(index).
 */
@Component
public class PptxReportExporter implements ReportExporter {

    private static final int ROWS_PER_SLIDE = 9;
    private static final int MAX_CHART_POINTS = 12; // more bars than this becomes unreadable
    private static final float MARGIN = 40f;
    private static final float CONTENT_WIDTH = 640f;  // 720 - 2 * MARGIN



    @Override
    public ExportFormat supportedFormat() {
        return ExportFormat.PPTX;
    }

    @Override
    public byte[] export(ReportDto report) {
        // Presentation holds native (unmanaged) resources, so we MUST call dispose().
        // Skipping it leaks memory on every export, which kills a long-running service.
        Presentation pres = new Presentation();
        try{
            pres.getSlideSize().setSize(SlideSizeType.OnScreen16x9, SlideSizeScaleType.EnsureFit);

            // A new Presentation contains one slide with placeholder shapes.
            // We add all our slides from a blank layout and delete that first slide at the end.
            ILayoutSlide blank = blankLayout(pres);

            addTitleSlide(pres,blank,report);
            addTableSlides(pres, blank, report);
            addChartSlides(pres, blank,report);

            pres.getSlides().removeAt(0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pres.save(out, SaveFormat.Pptx);
            return out.toByteArray();
        } catch(Exception ex){
            throw new ExportException("Failed to generate PPTX for report" + report.id(), ex);
        } finally {
            pres.dispose();
        }
    }

    private void addChartSlides(Presentation pres, ILayoutSlide layout, ReportDto report) {
        ISlide slide = pres.getSlides().addEmptySlide(layout);
        addText(slide, "Overview", MARGIN, 20, CONTENT_WIDTH, 40, 24, true);

        IChart chart = slide.getShapes().addChart(
                ChartType.ClusteredColumn, MARGIN, 70, CONTENT_WIDTH, 310);

        IChartData data = chart.getChartData();
        IChartDataWorkbook wb = data.getChartDataWorkbook();

        // A new chart ships with demo data. Remove it before adding ours.
        data.getSeries().clear();
        data.getCategories().clear();

        // wb.getCell(sheetIndex, row, column, value) writes into the chart's hidden Excel sheet
        data.getSeries().add(wb.getCell(0, 0, 1, "Value"), chart.getType());
        IChartSeries series = data.getSeries().get_Item(0);

        List<ReportRowDto> points = report.rows().subList(
                0, Math.min(report.rows().size(), MAX_CHART_POINTS));

        for (int i = 0; i < points.size(); i++) {
            ReportRowDto r = points.get(i);
            data.getCategories().add(wb.getCell(0, i + 1, 0, r.label()));
            series.getDataPoints().addDataPointForBarSeries(wb.getCell(0, i + 1, 1, r.value().doubleValue()));
        }

        chart.hasLegend();   // one series only, a legend would just be noise
    }


        private void addTableSlides(Presentation pres, ILayoutSlide layout, ReportDto report) {
      List<ReportRowDto> rows = report.rows();
      int pages = (int) Math.ceil(rows.size() / (double) ROWS_PER_SLIDE);

      for(int page = 0; page < pages; page++){
          ISlide slide = pres.getSlides().addEmptySlide(layout);
          addText(slide, "Data (" + (page + 1) + "/" + pages + ")", MARGIN, 20, CONTENT_WIDTH, 40, 24, true);

          // Sublist slices this page's rows without copying them
          List<ReportRowDto> chunk = rows.subList(
                  page * ROWS_PER_SLIDE, Math.min(rows.size(), (page + 1) * ROWS_PER_SLIDE));
          double[] colWidths = {300, 160, 180};
          double[] rowHeights = new double[chunk.size() + 1];   // +1 for the header row
          java.util.Arrays.fill(rowHeights, 28);

          ITable table = slide.getShapes().addTable(MARGIN, 70, colWidths, rowHeights);

          // One call styles the text of the whole table (font size 12)
          PortionFormat tableText = new PortionFormat();
          tableText.setFontHeight(12);
          table.setTextFormat(tableText);

          // table.get_Item(column, row): note the column comes first
          setCell(table, 0, 0, "Label");
          setCell(table, 1, 0, "Value");
          setCell(table, 2, 0, "Category");

          for (int i = 0; i < chunk.size(); i++) {
              ReportRowDto r = chunk.get(i);
              setCell(table, 0, i + 1, r.label());
              // Locale.US so "12,500.50" never turns into "12.500,50" on a server with another locale
              setCell(table, 1, i + 1, String.format(Locale.US, "%,.2f", r.value()));
              setCell(table, 2, i + 1, r.category() == null ? "" : r.category());
          }
      }
    }

    // ---------------------------------------------------------------- slides

    private void addTitleSlide(Presentation pres , ILayoutSlide layout, ReportDto report){
        ISlide slide = pres.getSlides().addEmptySlide(layout);
        addText(slide,report.title(),MARGIN,120,CONTENT_WIDTH,80,36,true);
        if(report.description() != null && !report.description().isBlank()){
            addText(slide, report.description(), MARGIN, 205, CONTENT_WIDTH,60, 18,false);
        }
    }

    /** Adds a borderless, transparent, left-aligned text box. */
    private IAutoShape addText(ISlide slide, String text, float x, float y, float w, float h, float fontSize, boolean bold){
        IAutoShape shape = slide.getShapes().addAutoShape(ShapeType.Rectangle, x , y, w, h);
        shape.getFillFormat().setFillType(FillType.NoFill);
        shape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);

        ITextFrame frame = shape.getTextFrame();
        frame.setText(text);
        frame.getParagraphs().get_Item(0).getParagraphFormat().setAlignment(TextAlignment.Left);

        IPortionFormat format = frame.getParagraphs().get_Item(0).getPortions().get_Item(0).getPortionFormat();
        format.setFontHeight(fontSize);
        format.setFontBold(bold ? NullableBool.True : NullableBool.False);
        // Shapes default to white text, so we set a visible color explicitly
        format.getFillFormat().setFillType(FillType.Solid);
        format.getFillFormat().getSolidFillColor().setColor(Color.DARK_GRAY);
        return shape;
    }

    private void setCell(ITable table, int col, int row, String text) {
        table.get_Item(col, row).getTextFrame().setText(text);
    }

    /** Finds the empty layout. Falls back to the first layout if the template has none. */
    private ILayoutSlide blankLayout(Presentation pres){
        ILayoutSlide blank = pres.getLayoutSlides().getByType(SlideLayoutType.Blank);
        return blank != null ? blank : pres.getLayoutSlides().get_Item(0);
    }
}
