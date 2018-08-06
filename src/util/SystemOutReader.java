package util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import window.AppWindow;

/**
 * Keeps a running track of lines read from an OutputStream.
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class SystemOutReader {

    private static ArrayList<String> unreadLines;
    private PipedWrapper pipedWrapper;

    /**
     * Constructs a new SystemOutReader.
     */
    public SystemOutReader () {
        unreadLines = new ArrayList<String>();
        new Thread() {
            public void run () {
                String prevVal = "";

                while (true) {
                    if (pipedWrapper != null) {
                        String s = pipedWrapper.readVal();
                        if (!prevVal.equals(s) && !s.equals("")) {
                            prevVal = s;
                            unreadLines.add(s);
                            System.out.println("NEW VAL: " + prevVal);
                        }
                    } else {
                        System.out.println("NULL");
                        pipedWrapper = AppWindow.getInstance().getSystemOut();
                    }
                    try {
                        Thread.sleep(50l);
                    } catch (InterruptedException e) {
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
    public ArrayList<String> getUnreadLines () {
        return unreadLines;
    }

    /**
     * Gets the first unread line and removes it from the ArrayList of unread lines.
     * @return the first unread line, removing it   
     */
    public String getFirstUnread () {
        if (unreadLines.size() == 0)
            return "";
        return unreadLines.remove(0);
    }
}
