package window;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

/**
 * 
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class Grapher {
    private static List<List<Double>> prevData;


    public static void main (String[] args) throws Exception {
        prevData = new ArrayList<List<Double>>();
        prevData.add(new ArrayList<Double>());
        prevData.add(new ArrayList<Double>());

        prevData.get(0).add(0d);
        prevData.get(1).add(0d);

        double phase = 0;

        double[][] initData = getData();
        // Create Chart
        final XYChart chart = QuickChart.getChart("Simple XChart Real-time Demo", "Time", "Position", "sine",
                initData[0], initData[1]);

        // Show it
        final SwingWrapper<XYChart> sw = new SwingWrapper<XYChart>(chart);

        sw.displayChart();

        while (true) {

            Thread.sleep(100);

            final double[][] data = getData();

            System.out.println(Arrays.toString(data[0]));
            System.out.println(Arrays.toString(data[1]));
            System.out.println();

            chart.updateXYSeries("sine", data[0], data[1], null);
            sw.repaintChart();
        }

    }

    private static double[][] getData () {
        List<Double> xData = prevData.get(0);
        xData.add(xData.get(xData.size() - 1) + 1);

        List<Double> yData = prevData.get(1);
        yData.add((yData.get(yData.size() - 1) + 1) * 1.1);

        return new double[][] {toArray(xData), toArray(yData)};
    }

    private static double[] toArray (List<Double> list) {
        double[] toReturn = new double[list.size()];
        for (int i = 0; i < list.size(); i++)
            toReturn[i] = list.get(i);
        return toReturn;

    }
}
