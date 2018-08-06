package util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import window.AppWindow;

/**
 * 
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class SystemOutReader {

    private static ArrayList<String> unreadLines;
    private PipedInputOutputWrapper pipedWrapper;

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
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public ArrayList<String> getUnreadLines () {
        return unreadLines;
    }
}
