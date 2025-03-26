package dat.service;

import dat.dtos.PitchPointDTO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;

public class ChartUtilsHelper {

    public static JFreeChart createChartFromPitchPoints(List<PitchPointDTO> pitchPoints) {
        XYSeries series = new XYSeries("Pitch over tid");

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (PitchPointDTO point : pitchPoints) {
            double time = point.getTimeInSeconds();
            double pitch = point.getPitch();

            series.add(time, pitch);
            if (pitch < min) min = pitch;
            if (pitch > max) max = pitch;
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Pitch Graph",
                "Tid (sekunder)",
                "Pitch (Hz)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        if (series.getItemCount() > 0) {
            chart.getXYPlot().getRangeAxis().setRange(min - 10, max + 10);
        }

        return chart;
    }
}
