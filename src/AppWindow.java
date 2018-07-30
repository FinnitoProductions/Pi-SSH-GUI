import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;


import com.jcraft.jsch.*;

/**
 * 
 * @author Finn Frankis
 * @version Jul 30, 2018
 */
public class AppWindow implements KeyListener
{

    private JFrame frame;
    private JTextField textField;

    private String user = "pi";
    private String password = "team1072";
    private String ip = "192.168.178.63";
    private int port = 22;
    
    private Channel cl;
    private CharInputStream inputStream;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    AppWindow window = new AppWindow();
                    window.frame.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * @throws JSchException 
     */
    public AppWindow() throws Exception
    {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     * @throws JSchException 
     */
    private void initialize() throws Exception
    {
        frame = new JFrame();
        frame.setBounds(100, 100, 766, 617);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        JButton btnDeployCode = new JButton("Select File");
        btnDeployCode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                final JFileChooser fc = new JFileChooser();
                fc.showOpenDialog(null);
                
                textField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        btnDeployCode.setBounds(532, 50, 187, 32);
        frame.getContentPane().add(btnDeployCode);
        
        textField = new JTextField();
        textField.setBounds(37, 50, 462, 38);
        frame.getContentPane().add(textField);
        textField.setColumns(10);
        textField.addKeyListener(this);
        
        //Process process = Runtime.getRuntime().exec("cd Documents; pscp servotest1.jar pi@192.168.178.63:Desktop");
        System.out.println("OPENING SSH");
        JSch jsch = new JSch();
        System.out.println("setting up session");
        Session session = jsch.getSession(user, ip, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel cl = session.openChannel("exec");
        cl.setOutputStream(System.out);
        inputStream = new CharInputStream();

        cl.setInputStream(inputStream);
        ((ChannelExec)cl).setCommand("cd Documents; sudo java -jar servotest2.jar");
        cl.connect();

        /*System.out.println("OPENING CHANNEL " + System.currentTimeMillis());
        cl.disconnect();
        ((ChannelExec)cl).setCommand("");
        cl.connect();
        System.out.println("RECONNECTING " + System.currentTimeMillis());*/
        /*
        cl.setInputStream(System.in);*/


        System.out.println("CONNECTED");
    }

    /**
    * @param arg0
    */
    @Override
    public void keyPressed(KeyEvent e) 
    {
        if (e.getKeyCode() == KeyEvent.VK_UP)
        {
            System.out.println("KEY UP");
            inputStream.addChar("i");
        }
        
    }

    /**
    * @param arg0
    */
    @Override
    public void keyReleased(KeyEvent arg0)
    {
        // TODO Auto-generated method stub
        
    }

    /**
    * @param arg0
    */
    @Override
    public void keyTyped(KeyEvent arg0)
    {
        // TODO Auto-generated method stub
        
    }
}
