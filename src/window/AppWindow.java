package window;
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

import util.Constants;
import util.SSHUtil;

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

    private JFrame mainFrame;
    private JFrame backgroundFrame;
    private JFrame setupFrame; 
    
    private JTextField fileTextField;
    private JLabel lblSshConnected;
    private JButton btnDeploy;
    private JButton btnRun;

    private Session session;
    
    private File fileTransfer;
    
    private Socket socket;
    
    private Channel clExec;
    
    private JLabel errorLabel;
    private JLabel lblKeyBindings;
    private JTextField upArrowField;
    private JLabel lblDownArrow;
    private JLabel lblLeftArrow;
    private JLabel lblRightArrow;
    
    private JTextField downArrowField;
    private JTextField leftArrowField;
    private JTextField rightArrowField;
    
    private JSONObject jo;
    
    private static AppWindow window;
    
    /**
     * Launches the application.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        window = new AppWindow();
        
        window.getBackgroundFrame().setVisible(true);
        
        window.getMainFrame().setTitle("Raspberry Pi SSH Deploy");
        window.getMainFrame().getContentPane().setBackground(Color.BLACK);
        window.getMainFrame().setVisible(true);
        



        while (true)
        {
            if (window.getSession() == null || !window.getSession().isConnected())
            {
                try
                {
                    SSHUtil.connectSSH(AppWindow.getInstance());
                }
                catch (Exception e)
                {
                    window.getLblSshConnected().setText("Pi Not Connected");
                    window.getLblSshConnected().setForeground(Color.RED);
                }
            }
            Thread.sleep(1000l);
        }
    }

    /**
     * Creates the application.
     */
    private AppWindow()
    {
        initialize();
    }

    /**
     * Initializes the contents of the frame.
     */
    private void initialize()
    {
        new File(Constants.EXT_DIR_PATH).mkdir();
        try
        {
            File json = new File(Constants.EXT_K_BIND_PATH);
            boolean newFile = json.createNewFile();
 
            if (newFile)
                writeStringToFile(Constants.EXT_K_BIND_PATH, getStringFromLocalFile(Constants.INT_K_BIND_PATH));
        }
        catch (Exception e2)
        {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        backgroundFrame = new JFrame();
        backgroundFrame.setBounds(Constants.FRAME_LOCATION_X, Constants.FRAME_LOCATION_Y, Constants.FRAME_SIZE_X, Constants.FRAME_SIZE_Y);
        backgroundFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        backgroundFrame.getContentPane().setLayout(null);
        
        mainFrame = new JFrame();
        mainFrame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 23));
        mainFrame.setBounds(Constants.FRAME_LOCATION_X, Constants.FRAME_LOCATION_Y, Constants.FRAME_SIZE_X, Constants.FRAME_SIZE_Y); // 550 for exporting
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(null);
        

        
        JButton btnSelectFile = new JButton("Select File");
        btnSelectFile.setBackground(Color.RED);
        btnSelectFile.setForeground(Color.ORANGE);
        Font f = btnSelectFile.getFont();
        btnSelectFile.setFont(new Font("Tahoma", Font.PLAIN, 19));
        btnSelectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose a file to be transferred." );
                fc.setCurrentDirectory(new File(System.getProperty("user.home")));
                fc.setPreferredSize(new Dimension(900, 900));
                fc.showOpenDialog(null);
                setFileTransfer(fc.getSelectedFile());
                if (getFileTransfer() == null)
                    return;
                String filePath = getFileTransfer().getAbsolutePath();
                fileTextField.setText("");
                fileTextField.setText(filePath);
                if (filePath != null && filePath.length() > 0 && getSession().isConnected())
                {
                    String fileName = fc.getSelectedFile().getName();
                    if (fileName.substring(fileName.indexOf(".") + 1).equals("jar"))
                    {
                        getBtnDeploy().setEnabled(true);
                        getBtnRun().setEnabled(true);
                    }
                    else
                    {
                        getBtnDeploy().setEnabled(false);
                        getBtnRun().setEnabled(false);
                    }
                }
                        
            }
        });
        btnSelectFile.setBounds(532, 50, 187, 38);
        mainFrame.getContentPane().add(btnSelectFile);
        
        fileTextField = new JTextField();
        fileTextField.setBounds(102, 50, 397, 38);
        mainFrame.getContentPane().add(fileTextField);
        fileTextField.setColumns(10);
        
        setLblSshConnected(new JLabel("Pi Not Connected"));
        getLblSshConnected().setFont(new Font("Tahoma", Font.PLAIN, 17));
        getLblSshConnected().setForeground(Color.RED);
        getLblSshConnected().setBounds(21, 468, 166, 64);

        mainFrame.getContentPane().add(getLblSshConnected());
        
        setBtnDeploy(new JButton("Deploy"));
        getBtnDeploy().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                SSHUtil.transferFile(AppWindow.getInstance(), getFileTransfer());
            }
        });
        getBtnDeploy().setBounds(386, 482, 141, 35);
        getBtnDeploy().setEnabled(false);
        mainFrame.getContentPane().add(getBtnDeploy());
        
        setBtnRun(new JButton("Run"));
        getBtnRun().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                SSHUtil.runCode(AppWindow.getInstance(), getFileTransfer());
            }
        });
        getBtnRun().setBounds(578, 482, 141, 35);
        getBtnRun().setEnabled(false);
        mainFrame.getContentPane().add(getBtnRun());
        
        setErrorLabel(new JLabel(""));
        getErrorLabel().setBounds(21, 444, 92, 26);
        getErrorLabel().setForeground(new Color(128, 0, 0));
        mainFrame.getContentPane().add(getErrorLabel());
        
        lblKeyBindings = new JLabel("Key Bindings:");
        lblKeyBindings.setForeground(Color.ORANGE);
        lblKeyBindings.setFont(new Font("Tahoma", Font.PLAIN, 24));
        lblKeyBindings.setBounds(153, 109, 157, 32);
        mainFrame.getContentPane().add(lblKeyBindings);
       

        try
        {
            String fileContents = getStringFromExternalFile(Constants.EXT_K_BIND_PATH);
            
            jo = new JSONObject(fileContents);
        
        }
        catch (Exception e)
        {
            getErrorLabel().setText(e.getMessage());
            e.printStackTrace();
        }
            JLabel lblUpArrow = new JLabel("Up:");
            lblUpArrow.setForeground(Color.ORANGE);
            lblUpArrow.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblUpArrow.setBounds(143, 151, 26, 19);
            mainFrame.getContentPane().add(lblUpArrow);
            
            upArrowField = new JTextField();
            upArrowField.setBounds(182, 148, 43, 19);
            mainFrame.getContentPane().add(upArrowField);
            upArrowField.setColumns(2);
            try
            {
                upArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_UP_KEY));
            }
            catch (JSONException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            JButton btnSaveUpArrow = new JButton("Save");
            btnSaveUpArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnSaveUpArrow.setBounds(236, 150, 61, 19);
            mainFrame.getContentPane().add(btnSaveUpArrow);
            btnSaveUpArrow.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        jo.put(Constants.K_BIND_UP_KEY, upArrowField.getText());
                        writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
            });
            
            lblDownArrow = new JLabel("Down:");
            lblDownArrow.setForeground(Color.ORANGE);
            lblDownArrow.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblDownArrow.setBounds(37, 185, 56, 19);
            mainFrame.getContentPane().add(lblDownArrow);
            
            
            downArrowField = new JTextField();
            try
            {
                downArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_DOWN_KEY));
            }
            catch (JSONException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            downArrowField.setColumns(2);
            downArrowField.setBounds(102, 185, 43, 19);
            mainFrame.getContentPane().add(downArrowField);
            
            JButton btnSaveDownArrow = new JButton("Save");
            btnSaveDownArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnSaveDownArrow.setBounds(187, 185, 61, 19);
            mainFrame.getContentPane().add(btnSaveDownArrow);

            btnSaveDownArrow.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        jo.put(Constants.K_BIND_DOWN_KEY, downArrowField.getText());
                        writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
            });
            
            lblLeftArrow = new JLabel("Left:");
            lblLeftArrow.setForeground(Color.ORANGE);
            lblLeftArrow.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblLeftArrow.setBounds(37, 222, 56, 19);
            mainFrame.getContentPane().add(lblLeftArrow);

            
            leftArrowField = new JTextField();
            try
            {
                leftArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_LEFT_KEY));
            }
            catch (JSONException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            leftArrowField.setColumns(2);
            leftArrowField.setBounds(102, 222, 43, 19);
            mainFrame.getContentPane().add(leftArrowField);
            
            JButton btnSaveLeftArrow = new JButton("Save");
            btnSaveLeftArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnSaveLeftArrow.setBounds(187, 222, 61, 19);
            mainFrame.getContentPane().add(btnSaveLeftArrow);

            btnSaveLeftArrow.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        jo.put(Constants.K_BIND_LEFT_KEY, leftArrowField.getText());
                        writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
            });
            

            lblRightArrow = new JLabel("Right:");
            lblRightArrow.setForeground(Color.ORANGE);
            lblRightArrow.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblRightArrow.setBounds(37, 259, 56, 19);
            mainFrame.getContentPane().add(lblRightArrow);

            
            rightArrowField = new JTextField();
            try
            {
                rightArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_RIGHT_KEY));
            }
            catch (JSONException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            rightArrowField.setColumns(2);
            rightArrowField.setBounds(102, 259, 43, 19);
            mainFrame.getContentPane().add(rightArrowField);
            
            JButton btnSaveRightArrow = new JButton("Save");
            btnSaveRightArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnSaveRightArrow.setBounds(187, 259, 61, 19);
            mainFrame.getContentPane().add(btnSaveRightArrow);

            btnSaveRightArrow.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        jo.put(Constants.K_BIND_RIGHT_KEY, rightArrowField.getText());
                        writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
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
                    if (getSession().isConnected() && getClExec().isConnected())
                    {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(getClExec().getOutputStream()));
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
                    getErrorLabel().setText("ERROR: Key Press Failed.");
                }
              // Pass the KeyEvent to the next KeyEventDispatcher in the chain
              return false;
            }
          };
          KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }


    
    public JFrame getMainFrame()
    {
        return mainFrame;
    }
    
    public JFrame getBackgroundFrame()
    {
        return backgroundFrame;
    }
    
    private static String getStringFromExternalFile (String fileName) throws Exception
    {
        String fileContents = "";

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        while (br.ready())
            fileContents += br.readLine() + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }

    private static String getStringFromLocalFile (String fileName) throws Exception
    {
        String fileContents = "";
 
        BufferedReader br = new BufferedReader(new InputStreamReader(AppWindow.getInstance().getClass().getResourceAsStream(Constants.INT_K_BIND_PATH)));

        while (br.ready())
            fileContents += br.readLine() + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }
    
    private static String setJSONValue (String fileContents, String var, String newVal)
    {
        if (!fileContents.contains(var))
            return "";
        String oldVal = fileContents.substring(fileContents.indexOf("var") + ("\":".length()));
        
        return oldVal;
    }
    private static void writeStringToFile (String fileName, String newContents) throws Exception
    {
        BufferedWriter br = new BufferedWriter (new FileWriter(fileName));
        br.write(newContents);
        br.close();
    }

    /**
     * Gets the lblSshConnected.
     * @return the lblSshConnected
     */
    public JLabel getLblSshConnected()
    {
        return lblSshConnected;
    }

    /**
     * Sets lblSshConnected to a given value.
     * @param lblSshConnected the lblSshConnected to set
     *
     * @postcondition the lblSshConnected has been changed to lblSshConnected
     */
    public void setLblSshConnected(JLabel lblSshConnected)
    {
        AppWindow.getInstance().lblSshConnected = lblSshConnected;
    }

    /**
     * Gets the session.
     * @return the session
     */
    public Session getSession()
    {
        return session;
    }

    /**
     * Sets session to a given value.
     * @param session the session to set
     *
     * @postcondition the session has been changed to session
     */
    public void setSession(Session session)
    {
        AppWindow.getInstance().session = session;
    }

    /**
     * Gets the btnDeploy.
     * @return the btnDeploy
     */
    public JButton getBtnDeploy()
    {
        return btnDeploy;
    }

    /**
     * Sets btnDeploy to a given value.
     * @param btnDeploy the btnDeploy to set
     *
     * @postcondition the btnDeploy has been changed to btnDeploy
     */
    private void setBtnDeploy(JButton btnDeploy)
    {
        AppWindow.getInstance().btnDeploy = btnDeploy;
    }

    /**
     * Gets the btnRun.
     * @return the btnRun
     */
    public JButton getBtnRun()
    {
        return btnRun;
    }

    /**
     * Sets btnRun to a given value.
     * @param btnRun the btnRun to set
     *
     * @postcondition the btnRun has been changed to btnRun
     */
    private void setBtnRun(JButton btnRun)
    {
        AppWindow.getInstance().btnRun = btnRun;
    }

    /**
     * Gets the errorLabel.
     * @return the errorLabel
     */
    public JLabel getErrorLabel()
    {
        return AppWindow.getInstance().errorLabel;
    }

    /**
     * Sets errorLabel to a given value.
     * @param errorLabel the errorLabel to set
     *
     * @postcondition the errorLabel has been changed to errorLabel
     */
    private void setErrorLabel(JLabel errorLabel)
    {
        AppWindow.getInstance().errorLabel = errorLabel;
    }

    /**
     * Gets the clExec.
     * @return the clExec
     */
    public Channel getClExec()
    {
        return AppWindow.getInstance().clExec;
    }

    /**
     * Sets clExec to a given value.
     * @param clExec the clExec to set
     *
     * @postcondition the clExec has been changed to clExec
     */
    public void setClExec(Channel clExec)
    {
        AppWindow.getInstance().clExec = clExec;
    }
    
    public static AppWindow getInstance()
    {
        if (window == null)
            window = new AppWindow();
        return window;
    }

    /**
     * Gets the fileTransfer.
     * @return the fileTransfer
     */
    public File getFileTransfer()
    {
        return fileTransfer;
    }

    /**
     * Sets fileTransfer to a given value.
     * @param fileTransfer the fileTransfer to set
     *
     * @postcondition the fileTransfer has been changed to fileTransfer
     */
    public void setFileTransfer(File fileTransfer)
    {
        this.fileTransfer = fileTransfer;
    }
}