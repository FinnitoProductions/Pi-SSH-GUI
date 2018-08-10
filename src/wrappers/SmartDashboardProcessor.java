package wrappers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Constants;
import util.PrecisePoint;
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
        System.out.println("ENTRY " + entry);
        if (!window.containsKey(entry.getKey())) {
            window.addGraph(entry.getKey());
        }
        Grapher graph = window.getGraph(entry.getKey());
        window.addPoint(entry.getKey(),
                new PrecisePoint((entry.getEntryTimeMs() - graph.getStartTimeMs()) / 1000.0, entry.getValue()));
    }
}
