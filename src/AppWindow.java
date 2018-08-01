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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;

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
    private JTextField fileTextField;
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
    
    private static JLabel errorLabel;
    private JLabel lblKeyBindings;
    private JTextField upArrowField;
    private JLabel lblDownArrow;
    
    private static final String srcBindingsName = "/bindings.json";
    private static final String userHome = System.getProperty("user.home") + File.separator;
    private static final String dirName = "pi-ssh-app";
    private static final String absoluteDirName = userHome + dirName;
    private static final String absoluteBindingsName = absoluteDirName + File.separator + "bindings.json";
    private JTextField downArrowField;
    
    private JSONObject jo;
    /**
     * Launches the application.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        AppWindow window = new AppWindow();
        window.getFrame().setTitle("Raspberry Pi SSH Deploy");
        window.getFrame().setVisible(true);
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
     */
    public AppWindow()
    {
        initialize();
    }

    /**
     * Initializes the contents of the frame.
     */
    private void initialize()
    {
        new File(absoluteDirName).mkdir();
        try
        {
            File json = new File(absoluteBindingsName);
            boolean newFile = json.createNewFile();
 
            if (newFile)
                writeStringToFile(absoluteBindingsName, getStringFromLocalFile(srcBindingsName));
        }
        catch (Exception e2)
        {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
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
                fileTextField.setText("");
                fileTextField.setText(filePath);
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
        
        fileTextField = new JTextField();
        fileTextField.setBounds(37, 50, 462, 38);
        frame.getContentPane().add(fileTextField);
        fileTextField.setColumns(10);
        
        lblSshConnected = new JLabel("Pi Not Connected");
        lblSshConnected.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblSshConnected.setForeground(new Color(128, 0, 0));
        lblSshConnected.setBounds(21, 468, 166, 64);

        frame.getContentPane().add(lblSshConnected);
        
        btnDeploy = new JButton("Deploy");
        btnDeploy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                transferFile();
            }
        });
        btnDeploy.setBounds(386, 482, 141, 35);
        btnDeploy.setEnabled(false);
        frame.getContentPane().add(btnDeploy);
        
        btnRun = new JButton("Run");
        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                runCode();
            }
        });
        btnRun.setBounds(578, 482, 141, 35);
        btnRun.setEnabled(false);
        frame.getContentPane().add(btnRun);
        
        errorLabel = new JLabel("");
        errorLabel.setBounds(21, 444, 92, 26);
        errorLabel.setForeground(new Color(128, 0, 0));
        frame.getContentPane().add(errorLabel);
        
        lblKeyBindings = new JLabel("Key Bindings:");
        lblKeyBindings.setFont(new Font("Tahoma", Font.PLAIN, 22));
        lblKeyBindings.setBounds(37, 115, 141, 26);
        frame.getContentPane().add(lblKeyBindings);
       

        try
        {
            String fileContents = getStringFromExternalFile(absoluteBindingsName);
            
            jo = new JSONObject(fileContents);
        
        }
        catch (Exception e)
        {
            errorLabel.setText(e.getMessage());
            e.printStackTrace();
        }
            JLabel lblUpArrow = new JLabel("Up:");
            lblUpArrow.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblUpArrow.setBounds(37, 145, 26, 19);
            frame.getContentPane().add(lblUpArrow);
            
            upArrowField = new JTextField();
            upArrowField.setBounds(70, 145, 43, 19);
            frame.getContentPane().add(upArrowField);
            upArrowField.setColumns(2);
            try
            {
                upArrowField.setText(jo == null ? "" : jo.getString("up"));
            }
            catch (JSONException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            JButton btnSaveUpArrow = new JButton("Save");
            btnSaveUpArrow.setFont(new Font("Tahoma", Font.PLAIN, 8));
            btnSaveUpArrow.setBounds(120, 145, 49, 19);
            frame.getContentPane().add(btnSaveUpArrow);
            
            lblDownArrow = new JLabel("Down:");
            lblDownArrow.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblDownArrow.setBounds(37, 185, 56, 19);
            frame.getContentPane().add(lblDownArrow);
            
;
            
            downArrowField = new JTextField();
            downArrowField.setText("");
            downArrowField.setColumns(2);
            downArrowField.setBounds(178, 182, 43, 19);
            frame.getContentPane().add(downArrowField);
            btnSaveUpArrow.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        jo.put("up", upArrowField.getText());
                        writeStringToFile(absoluteBindingsName, jo.toString());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
            });
    

        
        KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                try
                {
                    if (session.isConnected() && clExec.isConnected())
                    {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clExec.getOutputStream()));
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            if (e.getKeyCode() == KeyEvent.VK_UP)
                            {
                                System.out.println("KEY UP");
                                bw.write("i");
                            }
                        }
                    }
                }
                catch (IOException ex)
                {
                    errorLabel.setText("ERROR: Key Press Failed.");
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
        }
        catch (Exception e)
        {
            lblSshConnected.setText("Pi Not Connected");
            lblSshConnected.setForeground(Color.RED);
            btnDeploy.setEnabled(false);
            btnRun.setEnabled(false);
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
            catch (JSchException | SftpException e)
            {
                errorLabel.setText("ERROR: File could not be transferred.");
            }
        }
    }
    
    /**
     * Runs the deployed code on the Pi.
     */
    private static void runCode ()
    {
        if (session.isConnected() && fileTransfer != null)
        {
            try
            {
                clExec = session.openChannel("exec");
                clExec.setOutputStream(System.out);
    
                ((ChannelExec)clExec).setCommand("sudo java -jar " + fileTransfer.getName());
                clExec.connect();
            }
            catch (JSchException e)
            {
                errorLabel.setText("ERROR: Code could not be run.");
            }
            
        }
    }
    
    public JFrame getFrame()
    {
        return frame;
    }
    
    private String getStringFromExternalFile (String fileName) throws Exception
    {
        String fileContents = "";

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        while (br.ready())
            fileContents += br.readLine() + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }

    private String getStringFromLocalFile (String fileName) throws Exception
    {
        String fileContents = "";

        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(srcBindingsName)));

        while (br.ready())
            fileContents += br.readLine() + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }
    
    private String setJSONValue (String fileContents, String var, String newVal)
    {
        if (!fileContents.contains(var))
            return "";
        String oldVal = fileContents.substring(fileContents.indexOf("var") + ("\":".length()));
        
        return oldVal;
    }
    private void writeStringToFile (String fileName, String newContents) throws Exception
    {
        BufferedWriter br = new BufferedWriter (new FileWriter(fileName));
        br.write(newContents);
        br.close();
    }
}
