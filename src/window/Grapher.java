package window;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import util.Constants;

/**
 * Represents a single chart of data.
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class Grapher {
    private static List<List<Double>> prevData;
    private XYChart chart;
    private JPanel chartPanel;

    private ArrayList<Integer> size;
    private ArrayList<Integer> location;
    
    private String dataLegend;

    /**
     * Constructs a new Grapher.
     * @param title the title of the graph
     * @param xAxisTitle the title of the x-axis
     * @param yAxisTitle the title of the y-axis
     * @param dataLegend the name of the data being graphed (for the legend)
     */
    public Grapher (String title, String xAxisTitle, String yAxisTitle, String dataLegend, int sizeX, int sizeY,
            int locationX, int locationY) {
        this.dataLegend = dataLegend;
        
        prevData = new ArrayList<List<Double>>();
        prevData.add(new ArrayList<Double>());
        prevData.add(new ArrayList<Double>());
        chart = QuickChart.getChart(title, xAxisTitle, yAxisTitle, dataLegend,
                new double[] {0}, new double[] {0});
        chartPanel = new XChartPanel<XYChart>(chart);

        size = new ArrayList<Integer>();
        size.add(0);
        size.add(0);

        location = new ArrayList<Integer>();
        location.add(0);
        location.add(0);
        
        setSize(sizeX, sizeY);
        setLocation(locationX, locationY);
    }

    /**
     * Gets the current chart as a JPanel.
     * @return the chart, formatted as a JPanel
     */
    public JPanel getChartPanel () {
        chartPanel = new XChartPanel<XYChart>(chart);
        chartPanel.validate();
        chartPanel.setSize(size.get(Constants.GRAPH_X_INDEX), size.get(Constants.GRAPH_Y_INDEX));
        chartPanel.setLocation(location.get(Constants.GRAPH_X_INDEX), location.get(Constants.GRAPH_Y_INDEX));

        return chartPanel;
    }

    /**
     * Adds a new point to the graph.
     * @param x the x-value of the point to be added
     * @param y the y-value of the point to be added
     */
    public void addPoint (double x, double y) {
        List<Double> xData = prevData.get(Constants.GRAPH_X_INDEX);
        xData.add(x);

        List<Double> yData = prevData.get(Constants.GRAPH_Y_INDEX);
        yData.add(y);

        final double[][] data = getDataArr();

        System.out.println(Arrays.toString(data[0]));
        System.out.println(Arrays.toString(data[1]));
        System.out.println();

        chart.updateXYSeries(dataLegend, data[Constants.GRAPH_X_INDEX], data[Constants.GRAPH_Y_INDEX], null);

    }

    /**
     * Sets the size of the graph when displayed as a JPanel.
     * @param sizeX the x-value of the size
     * @param sizeY the y-value of the size
     */
    public void setSize (int sizeX, int sizeY) {
        size.set(Constants.GRAPH_X_INDEX, sizeX);
        size.set(Constants.GRAPH_Y_INDEX, sizeY);
    }

    /**
     * Sets the location of the graph when displayed as a JPanel.
     * @param locX the x-value of the location
     * @param locY the y-value of the location
     */
    public void setLocation (int locX, int locY) {
        location.set(Constants.GRAPH_X_INDEX, locX);
        location.set(Constants.GRAPH_Y_INDEX, locY);
    }

    /**
     * Converts the data into an array.
     * @return the data in array form
     */
    private double[][] getDataArr () {
        return new double[][] {toArray(prevData.get(Constants.GRAPH_X_INDEX)),
                toArray(prevData.get(Constants.GRAPH_Y_INDEX))};
    }

    /**
     * Converts a given ArrayList of doubles into an array of doubles.
     * @param list the ArrayList to convert
     * @return the ArrayList, converted into an array
     */
    private static double[] toArray (List<Double> list) {
        double[] toReturn = new double[list.size()];
        for (int i = 0; i < list.size(); i++)
            toReturn[i] = list.get(i);
        return toReturn;

    }

    public static void main (String[] args) {
        Grapher g = new Grapher("Test Graph", "x", "y", "parabola", 400, 400, 25, 25);
        JFrame frame = new JFrame();
        frame.setBounds(Constants.FRAME_LOCATION_X, Constants.FRAME_LOCATION_Y, Constants.FRAME_SIZE_X,
                Constants.FRAME_SIZE_Y); // 550 for exporting
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        

    }

}
