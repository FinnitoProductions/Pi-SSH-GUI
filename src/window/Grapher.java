package window;

import java.awt.Component;
import util.Point;
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
    private static List<Point> prevData;

    private XYChart chart;
    private JPanel chartPanel;

    private ArrayList<Integer> size;
    private ArrayList<Integer> location;

    private String dataLegend;
    private String title;

    private long startTimeMs;

    /**
     * 
     * @author Finn Frankis
     * @version Aug 9, 2018
     */
    public enum DataAxis {
        X, Y
    }

    /**
     * Constructs a new Grapher.
     * @param title the title of the graph
     * @param xAxisTitle the title of the x-axis
     * @param yAxisTitle the title of the y-axis
     * @param dataLegend the name of the data being graphed (for the legend)
     * @param sizeX the size of the graph, in the x-direction
     * @param sizeY the size of the graph, in the y-direction
     * @param locationX the x-position of the graph
     * @param locationY the y-position of the graph
     */
    public Grapher (String title, String xAxisTitle, String yAxisTitle, String dataLegend, int sizeX, int sizeY,
            int locationX, int locationY) {
        this.dataLegend = dataLegend;
        this.title = title;

        prevData = new ArrayList<Point>();

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

        startTimeMs = System.currentTimeMillis();
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
        prevData.add(new Point(x, y));

        final Point[] data = getDataArr();

        chart.updateXYSeries(dataLegend, getData(DataAxis.X), getData(DataAxis.Y), null);

    }

    /**
     * Adds a new point to the graph.
     * @param p the point to be added
     */
    public void addPoint (Point p) {
        addPoint(p.getX(), p.getY());
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
    private Point[] getDataArr () {
        return toArray(prevData);
    }

    /**
     * Converts a given ArrayList of doubles into an array of doubles.
     * @param list the ArrayList to convert
     * @return the ArrayList, converted into an array
     */
    private static Point[] toArray (List<Point> list) {
        Point[] toReturn = new Point[list.size()];
        for (int i = 0; i < list.size(); i++)
            toReturn[i] = list.get(i);
        return toReturn;

    }

    public static void main (String[] args) {
        Grapher g = new Grapher("Test Graph", "x", "y", "parabola", 400, 400, 25, 25);
        JFrame frame = new JFrame();
        frame.setBounds(Constants.FRAME_LOC_X, Constants.FRAME_LOC_Y, Constants.FRAME_SIZE_X,
                Constants.FRAME_SIZE_Y); // 550 for exporting
        frame.getContentPane().add(g.getChartPanel());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        g.addPoint(new Point (0, 0));
        g.addPoint(new Point(1, 1));
        while (true)
        {
            List<Point> prevData = g.prevData;
            g.addPoint(new Point (prevData.get(prevData.size() - 1).getX() + 1, 
                    (2 * prevData.get(prevData.size()-1).getY() - prevData.get(prevData.size()-2).getY())));
            frame.repaint();
            try {
                Thread.sleep(100l);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the startTimeMs.
     * @return the startTimeMs
     */
    public long getStartTimeMs () {
        return startTimeMs;
    }

    /**
     * Gets the title.
     * @return the title
     */
    public String getTitle () {
        return title;
    }

    /**
     * Sets title to a given value.
     * @param title the title to set
     *
     * @postcondition the title has been changed to the newly passed in title
     */
    public void setTitle (String title) {
        this.title = title;
    }

    /**
     * Gets the previous point plotted on the graph.
     * @return the previous point
     */
    public Point getPrevPoint () {
        return prevData.get(prevData.size() - 1);
    }

    /**
     * Gets the data plotted on one of the axes.
     * @param axis the axis to be returned (x, y)
     * @return an ArrayList containing the data
     */
    public ArrayList<Double> getData (DataAxis axis) {
        ArrayList<Double> data = new ArrayList<Double>();
        for (Point p : prevData)
            data.add(axis == DataAxis.X ? p.getX() : p.getY());
        return data;
    }

}
