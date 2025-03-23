package dat.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PitchDataGraph {

    public static void main(String[] args) {
        // Pitch data (replace with your actual data)
        double[] pitchData = {
                132.72, 132.01, 131.94, 132.66, 177.53, 176.51, 176.25, 176.54, 265.39, 265.37,
                262.88, 263.69, 44.83, 324.51, 329.20, 333.42, 23280.96, 395.27, 395.12, 393.50,
                406.91, 44.90, 324.40, 332.95, 360.35, 23232.38, 266.78, 265.29, 265.33, 262.40,
                23313.43, 23334.84, 177.56, 177.03, 176.88, 23323.17, 23213.73, 23217.11, 23216.43,
                23216.06, 23209.02, 23208.14, 177.60, 177.52, 176.23, 178.93, 265.36, 265.40, 262.76,
                263.73, 23296.10, 52.00, 324.55, 328.94, 329.09, 23295.07, 395.18, 399.27, 393.56,
                406.60, 23323.91, 23246.91, 23224.53, 23228.30, 23210.82, 23212.25, 23204.22, 23207.79,
                23244.19, 23344.16, 431.96, 23269.72, 11101.95, 11125.54, 132.73, 132.73, 132.64, 131.89,
                131.95, 177.54, 177.52, 176.18, 176.40, 265.38, 265.35, 263.00, 263.56, 23298.39, 51.90,
                324.76, 328.78, 328.98, 23317.22, 23472.84, 395.41, 399.27, 393.74, 399.83, 44.89, 327.91,
                329.26, 338.02, 23231.68, 270.21, 264.84, 265.09, 262.79, 23381.78, 177.62, 177.49, 176.01,
                23377.80, 23221.50, 23214.55, 23220.56, 23212.28, 23210.38, 23209.49, 177.63, 177.57, 176.16,
                177.58, 265.39, 265.36, 263.03, 263.39, 23316.27, 324.98, 328.76, 332.96, 23338.18, 395.17,
                399.26, 393.70, 399.33, 23279.04, 23244.03, 23233.76, 23226.81, 23209.62, 23213.58, 23202.12,
                23205.78, 23232.83, 23336.54, 440.05, 11114.54, 23253.72, 11150.87
        };

        // Create a dataset
        XYSeries series = new XYSeries("Pitch Data");
        for (int i = 0; i < pitchData.length; i++) {
            series.add(i, pitchData[i]); // X-axis: time/index, Y-axis: pitch value
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Pitch Data Over Time", // Chart title
                "Time (Index)",         // X-axis label
                "Pitch (Hz)",          // Y-axis label
                dataset,                // Data
                PlotOrientation.VERTICAL,
                true,                   // Include legend
                true,
                false
        );

        // Display the chart
        ChartFrame frame = new ChartFrame("Pitch Data Graph", chart);
        frame.pack();
        frame.setVisible(true);
    }
}