package util;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PipedInputOutputWrapper {
   private PipedOutputStream pipedOut;
   private PipedInputStream pipedIn;
   
   private PipedOutputThread pot;
   
   public PipedInputOutputWrapper()
   {
       pipedIn = new PipedInputStream();
       pipedOut = new PipedOutputStream();
       try { pipedIn.connect(pipedOut); }
    catch (IOException e) { e.printStackTrace(); }
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(pot = new PipedOutputThread());


   }
   class PipedOutputThread implements Runnable{
       
       private String currentVal;
        @Override
        public void run() {
            if (currentVal != null)
            {
                try
                {
                    System.out.println("WRITING " + currentVal);
                    pipedOut.write((currentVal + System.getProperty("line.separator")).getBytes());
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        
        }
        public void writeVal (String s)
        {
            currentVal = s;
            run(); 
        }
   }
    
    public PipedInputStream getInputStream()
    {
        return pipedIn;
    }
    
    public PipedOutputStream getOutputStream()
    {
        return pipedOut;
    }
    
    public void writeVal (String s)
    {
        pot.writeVal(s);
    }
    
   
} 