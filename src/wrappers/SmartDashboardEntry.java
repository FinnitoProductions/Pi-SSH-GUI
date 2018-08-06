package wrappers;

/**
 * Represents a single entry to SmartDashboard.
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class SmartDashboardEntry {
    private double entryTimeMs;
    private String key;
    private double value;
    
    /**
     * Constructs a new SmartDashboardEntry.
     * @param entryTimeMs the time at which the entry was made
     * @param key the title of the graph to which value will be entered
     * @param value the value which will be plotted
     */
    public SmartDashboardEntry (double entryTimeMs, String key, double value) {
        super();
        this.entryTimeMs = entryTimeMs;
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the entryTimeMs.
     * @return the entryTimeMs
     */
    public double getEntryTimeMs () {
        return entryTimeMs;
    }

    /**
     * Sets entryTimeMs to a given value.
     * @param entryTimeMs the entryTimeMs to set
     *
     * @postcondition the entryTimeMs has been changed to the newly passed in entryTimeMs
     */
    public void setEntryTimeMs (double entryTimeMs) {
        this.entryTimeMs = entryTimeMs;
    }

    /**
     * Gets the key.
     * @return the key
     */
    public String getKey () {
        return key;
    }

    /**
     * Sets key to a given value.
     * @param key the key to set
     *
     * @postcondition the key has been changed to the newly passed in key
     */
    public void setKey (String key) {
        this.key = key;
    }

    /**
     * Gets the value.
     * @return the value
     */
    public double getValue () {
        return value;
    }

    /**
     * Sets value to a given value.
     * @param value the value to set
     *
     * @postcondition the value has been changed to the newly passed in value
     */
    public void setValue (double value) {
        this.value = value;
    }
}
