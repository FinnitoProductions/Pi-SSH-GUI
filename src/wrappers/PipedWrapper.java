package wrappers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Wraps the process of reading from and writing to connected PipedInput and PipedOutput streams. Note that a
 * PipedInputStream is periodically funneled data from a PipedOutputStream, which can be written to like a standard
 * OutputStream.
 * @author Finn Frankis
 * @version Aug 4, 2018
 */
public class PipedWrapper {
    private PipedOutputStream pipedOut;
    private PipedInputStream pipedIn;

    private PipedOutputThread pot;
    private PipedInputThread pit;

    private ExecutorService service;

    /**
     * Constructs a new PipedInputOutputWrapper.
     */
    public PipedWrapper () {
        pipedIn = new PipedInputStream();
        pipedOut = new PipedOutputStream();
        try {
            pipedIn.connect(pipedOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        service = Executors.newFixedThreadPool(2);
        service.execute(pot = new PipedOutputThread());
        service.execute(pit = new PipedInputThread());
    }

    /**
     * Represents the thread to write to the output stream.
     * @author Finn Frankis
     * @version Aug 4, 2018
     */
    class PipedOutputThread implements Runnable {

        private String currentVal;

        @Override
        public void run () {
            if (currentVal != null) {
                try {
                    System.out.println("WRITING " + currentVal);
                    pipedOut.write((currentVal + System.getProperty("line.separator")).getBytes());
                    pipedOut.flush();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        /**
         * Writes a value to the output stream on a new line.
         * @param s the value to write to the output stream
         */
        public void writeVal (String s) {
            currentVal = s;
            run();
        }
    }

    /**
     * 
     * 
     * @author Finn Frankis
     * @version Aug 6, 2018
     */
    class PipedInputThread implements Runnable {
        private String currentVal;

        @Override
        public void run () {
            try {
                Scanner br = new Scanner(new InputStreamReader(pipedIn));
                if (br.hasNextLine()) {
                    currentVal = br.nextLine();
                }
            } catch (Exception e) {
                if (!(e instanceof IOException))
                    e.printStackTrace();
            }

        }

        /**
         * Reads the most recent value from the input stream.
         * @return the read value
         */
        public String readVal () {
            run();
            return currentVal;
        }
    }

    /**
     * Gets the current piped input stream.
     * @return the PipedInputStream in current use
     */
    public PipedInputStream getInputStream () {
        return pipedIn;
    }

    /**
     * Gets the current piped output stream.
     * @return the PipedOutputStream in current use
     */
    public PipedOutputStream getOutputStream () {
        return pipedOut;
    }

    /**
     * Writes a given value to the output thread for reading by the input stream.
     * @param s the String to be written
     */
    public void writeVal (String s) {
        pot.writeVal(s);
    }

    /**
     * Reads the most recent value from the input stream.
     * @return the read value
     */
    public String readVal () {
        return pit.readVal();
    }

    public void stop () {
        service.shutdownNow();
    }
}