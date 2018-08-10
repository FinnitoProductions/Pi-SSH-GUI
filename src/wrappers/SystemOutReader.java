package wrappers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import util.Constants;
import util.StringUtil;
import window.AppWindow;

/**
 * Keeps a running track of lines read from an OutputStream.
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class SystemOutReader {

    private static ArrayList<String> lines;
    private static int unreadIndex;
    private PipedWrapper pipedWrapper;

    /**
     * Constructs a new SystemOutReader.
     */
    public SystemOutReader () {
        lines = new ArrayList<String>();
        unreadIndex = 0;
        new Thread() {
            public void run () {
                String prevVal = "";

                while (true) {
                    if (AppWindow.hasInstance()) {
                        if (pipedWrapper == null)
                            pipedWrapper = AppWindow.getInstance().getSystemOut();
                        if (pipedWrapper != null)
                        {
                            String s = pipedWrapper.readVal();
                            //System.out.println(s);
                            if (!prevVal.equals(s) && !s.equals("")) {
                                prevVal = s;
                                if (StringUtil.startsWithValue(s, Constants.SMART_DASH_PREFIX))
                                    SmartDashboardProcessor.addEntry(smartDashboardParser(s));
                                else
                                    lines.add(s);
                                System.out.println("NEW VAL: " + prevVal);
                            }
                        }
                    }
                    try {
                        Thread.sleep(10l);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }

    /**
     * Gets the lines which have not yet been extracted.
     * @return the ArrayList containing unextracted lines
     */
    public List<String> getUnreadLines () {
        return lines.subList(unreadIndex, lines.size());
    }

    /**
     * Gets the first unread line and removes it from the ArrayList of unread lines.
     * @return the first unread line, removing it
     */
    public String getFirstUnread () {
        if (lines.size() == 0 || unreadIndex >= lines.size())
            return "";
        return lines.get(unreadIndex++);
    }

    /**
     * Parses a String into a SmartDashboard entry, provided that it has already been validated.
     * @param s the String containing the SmartDashboard information
     * @return the entry, after having been parsed
     */
    private static SmartDashboardEntry smartDashboardParser (String s) {
        long currentTimeMillis = System.currentTimeMillis();
        int endKeyIndex = StringUtil.indexOfNumber(s);
        Double value = new Double(0);
        if (endKeyIndex == -1) {
            endKeyIndex = s.length();
            value = null;
        }
        String key = s.substring(Constants.SMART_DASH_PREFIX.length(), endKeyIndex - 1);
        value = (value == null) ? 0 : Double.parseDouble(s.substring(endKeyIndex));

        System.out.println("KEY " + key + "VALUE " + value);
        return new SmartDashboardEntry(currentTimeMillis, key, value);
    }

}
