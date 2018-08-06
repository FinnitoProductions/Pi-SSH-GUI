package wrappers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Constants;
import window.AppWindow;
import window.Grapher;

/**
 * Graphically processes data given by SmartDashboard entries.
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class SmartDashboardProcessor {
    public static Map<String, Grapher> existingKeys = new HashMap<String, Grapher>();

    /**
     * Adds an entry to be graphed.
     * @param entry the entry to be added
     */
    public static void addEntry (SmartDashboardEntry entry) {
        if (!existingKeys.containsKey(entry.getKey()))
        {
            existingKeys.put(entry.getKey(), new Grapher(entry.getKey(), "Time", "Value", "Pi Bot",
                    Constants.GRAPH_SIZE_X, Constants.GRAPH_SIZE_Y, Constants.GRAPH_LOC_X, Constants.GRAPH_LOC_Y));
            AppWindow.getInstance().addGraph(existingKeys.get(entry.getKey()));
        }   
        Grapher graph = existingKeys.get(entry.getKey());
        graph.addPoint(entry.getEntryTimeMs() - graph.getStartTimeMs(), entry.getValue());
    }
}
