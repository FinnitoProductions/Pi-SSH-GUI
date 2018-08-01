import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;


import com.jcraft.jsch.*;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

/**
 * Represents the primary window of the app.
 * @author Finn Frankis
 * @version Jul 30, 2018
 */
public class AppWindow 
{

    private JFrame frame;
    private JTextField textField;
    private static JLabel lblSshConnected;
    private static JButton btnDeploy;
    private static JButton btnRun;

    private static String user = "pi";
    private static String password = "team1072";
    private static String ip = "192.168.178.63";
    private static int port = 22;
    
    private static Session session;
    
    private static File fileTransfer;
    
    private static Socket socket;
    
    private static Channel clExec;
    /**
     * Launches the application.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        AppWindow window = new AppWindow();
        window.frame.setVisible(true);
        while (true)
        {
            if (session == null || !session.isConnected())
            {
                try
                {
                    connectSSH();
                }
                catch (Exception e)
                {
                    lblSshConnected.setText("Pi Not Connected");
                    lblSshConnected.setForeground(Color.RED);
                }
            }
            Thread.sleep(1000l);
        }
    }

    /**
     * Creates the application.
     * @throws Exception if there is a problem in initialization
     */
    public AppWindow() throws Exception
    {
        initialize();
    }

    /**
     * Initializes the contents of the frame.
     */
    private void initialize() throws Exception
    {
        frame = new JFrame();
        frame.setBounds(100, 100, 766, 617);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        JButton btnSelectFile = new JButton("Select File");
        btnSelectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose a file to be transferred." );
                fc.setPreferredSize(new Dimension(900, 900));
                fc.showOpenDialog(null);
                fileTransfer = fc.getSelectedFile();
                if (fileTransfer == null)
                    return;
                String filePath = fileTransfer.getAbsolutePath();
                textField.setText("");
                textField.setText(filePath);
                if (filePath != null && filePath.length() > 0 && session.isConnected())
                {
                    String fileName = fc.getSelectedFile().getName();
                    if (fileName.substring(fileName.indexOf(".") + 1).equals("jar"))
                    {
                        btnDeploy.setEnabled(true);
                        btnRun.setEnabled(true);
                    }
                    else
                    {
                        btnDeploy.setEnabled(false);
                        btnRun.setEnabled(false);
                    }
                }
                        
            }
        });
        btnSelectFile.setBounds(532, 50, 187, 32);
        frame.getContentPane().add(btnSelectFile);
        
        textField = new JTextField();
        textField.setBounds(37, 50, 462, 38);
        frame.getContentPane().add(textField);
        textField.setColumns(10);
        
        lblSshConnected = new JLabel("Pi Not Connected");
        lblSshConnected.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblSshConnected.setForeground(new Color(128, 0, 0));
        lblSshConnected.setBounds(21, 468, 166, 64);

        frame.getContentPane().add(lblSshConnected);
        
        btnDeploy = new JButton("Deploy");
        btnDeploy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                try
                {
                    transferFile();
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        btnDeploy.setBounds(386, 482, 141, 35);
        btnDeploy.setEnabled(false);
        frame.getContentPane().add(btnDeploy);
        
        btnRun = new JButton("Run");
        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                try
                {
                    runCode();
                }
                catch (JSchException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        btnRun.setBounds(578, 482, 141, 35);
        btnRun.setEnabled(false);
        frame.getContentPane().add(btnRun);

        
        KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                try
                {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        if (e.getKeyCode() == KeyEvent.VK_UP)
                        {
                            System.out.println("KEY UP");
                            new BufferedWriter(new OutputStreamWriter(clExec.getOutputStream())).write("i");
                        }
                    }
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
              // Pass the KeyEvent to the next KeyEventDispatcher in the chain
              return false;
            }
          };
          KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    /**
     * Forms the initial SSH connection with the Pi.
     */
    private static void connectSSH() throws Exception
    {
        try
        {
            JSch jsch = new JSch();
            
            session = jsch.getSession(user, ip, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            lblSshConnected.setText("Connecting...");
            lblSshConnected.setForeground(Color.BLUE);
            session.connect();
            
            if (session.isConnected())
            {
                lblSshConnected.setText("Pi Connected");
                lblSshConnected.setForeground(new Color(105, 196, 80));
                Font f = lblSshConnected.getFont();
                lblSshConnected.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            }
            else
            {
                lblSshConnected.setText("Pi Not Connected");
                lblSshConnected.setForeground(Color.RED);
                btnDeploy.setEnabled(false);
                btnRun.setEnabled(false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Transfers the actively selected file to the Pi via SFTP.
     */
    private static void transferFile () 
    {
        if (session.isConnected() && fileTransfer != null)
        {
            try
            {
                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();
                sftpChannel.put(fileTransfer.getAbsolutePath(), fileTransfer.getName());
                sftpChannel.disconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Runs the deployed code on the Pi.
     * @throws JSchException if the channel link cannot be created
     */
    private static void runCode () throws JSchException
    {
        if (session.isConnected() && fileTransfer != null)
        {
            clExec = session.openChannel("exec");
            clExec.setOutputStream(System.out);

            ((ChannelExec)clExec).setCommand("sudo java -jar " + fileTransfer.getName());
            clExec.connect();
            
        }
    }
}
