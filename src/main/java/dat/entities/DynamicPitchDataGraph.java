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
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/audioanalysis";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static void main(String[] args) {
        XYSeries series = new XYSeries("Pitch Data");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, resultdata FROM audioresults ORDER BY id")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String resultData = rs.getString("resultdata"); // Now it should be a plain string, not an OID

                if (resultData != null) {
                    // Parse the pitch values (assuming they are comma-separated)
                    String[] pitchValues = resultData.split(",");
                    for (String pitch : pitchValues) {
                        try {
                            double pitchValue = Double.parseDouble(pitch.trim()); // Convert string to double
                            series.add(id, pitchValue);  // Add to series for graphing
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping invalid pitch value: " + pitch);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error occurred while fetching pitch data: " + e.getMessage());
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
                "Pitch (Hz)",           // Y-axis label
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

