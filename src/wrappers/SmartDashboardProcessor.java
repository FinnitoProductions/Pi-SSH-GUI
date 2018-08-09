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
    
    /**
     * Adds an entry to be graphed.
     * @param entry the entry to be added
     */
    public static void addEntry (SmartDashboardEntry entry) {
        AppWindow window = AppWindow.getInstance();
        if (!window.containsKey(entry.getKey())) {
            window.addGraph(entry.getKey());
        }
        Grapher graph = window.getGraph(entry.getKey());
        graph.addPoint(entry.getEntryTimeMs() - graph.getStartTimeMs(), entry.getValue());
    }
}
