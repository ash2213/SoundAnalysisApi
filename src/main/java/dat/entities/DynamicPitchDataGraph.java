package dat.entities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DynamicPitchDataGraph {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/audioanalysis"; // Replace with your DB URL
    private static final String DB_USER = "postgres"; // Replace with your DB username
    private static final String DB_PASSWORD = "postgres"; // Replace with your DB password

    public static void main(String[] args) {
        // Fetch pitch data from the `audioresults` table
        XYSeries series = new XYSeries("Pitch Data");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, resultdata FROM audioresults ORDER BY id")) {

            while (rs.next()) {
                int id = rs.getInt("id"); // X-axis: time/index (or use `created_at` if available)
                double pitchValue = rs.getDouble("resultdata"); // Y-axis: pitch value (from `resultdata`)
                series.add(id, pitchValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Create a dataset
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