package window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

import util.Constants;
import util.FileUtil;
import util.PipedInputOutputWrapper;
import util.SSHUtil;
import util.SystemOutReader;

/**
 * Represents the primary window of the app.
 * @author Finn Frankis
 * @version Jul 30, 2018
 */
public class AppWindow {
    private JFrame mainFrame;
    private JFrame setupFrame;

    private JTextField fileTextField;
    private JLabel lblSshConnected;
    private JButton btnDeploy;
    private JButton btnRun;
    private JButton btnStop;

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

    private PipedInputOutputWrapper sshCommandValue;
    private PipedInputOutputWrapper systemOut;
    
    private SystemOutReader outReader;
    
    private Grapher positionGraph;

    /**
     * Launches the application.
     * @throws Exception
     */
    public static void main (String[] args) throws Exception {
        window = new AppWindow();

        window.getMainFrame().setTitle("Raspberry Pi SSH Deploy");
        window.getMainFrame().getContentPane().setBackground(Color.BLACK);
        
        window.getMainFrame().setVisible(true);

        while (true) {
            if (window.getSession() == null || !window.getSession().isConnected()) {
                try {
                    SSHUtil.connectSSH(AppWindow.getInstance());
                } catch (Exception e) {
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
    private AppWindow () {
        initialize();
    }

    /**
     * 
     */
    private void setupExternalFiles () {
        new File(Constants.EXT_DIR_PATH).mkdir();
        try {
            File json = new File(Constants.EXT_K_BIND_PATH);
            boolean newFile = json.createNewFile();

            if (newFile)
                FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH,
                        FileUtil.getStringFromLocalFile(Constants.INT_K_BIND_PATH));
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
    }

    /**
     * Initializes the contents of the frame.
     */
    private void initialize () {
        setupExternalFiles();

        displayMainFrame();

        setupFileSelector();

        displayDeployRunBtns();

        setupWarningLabels();

        setupKeyBindings();

        setupKeyChecking();
        
        outReader = new SystemOutReader();
        
        initializeGraph();
    }

    /**
     * 
     */
    private void initializeGraph () {
        positionGraph = new Grapher ("Position vs Time", "Position", "Time", "Pi Bot", 400, 400, 25, 25);
        mainFrame.getContentPane().add(positionGraph.getChartPanel());
    }

    /**
     * 
     */
    private void setupKeyChecking () {
        KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent (final KeyEvent e) {
                try {
                    if (getSession().isConnected() && getClExec().isConnected()) {
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            if (e.getKeyCode() == KeyEvent.VK_UP) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_UP_KEY));

                            }
                            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_DOWN_KEY));

                            }
                            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_LEFT_KEY));

                            }
                            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_RIGHT_KEY));

                            }
                        }
                    }
                } catch (Exception ex) {
                    getErrorLabel().setText("ERROR: Key Press Failed.");
                    ex.printStackTrace();
                }
                // Pass the KeyEvent to the next KeyEventDispatcher in the chain
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    /**
     * 
     */
    private void setupKeyBindings () {
        try {
            String fileContents = FileUtil.getStringFromExternalFile(Constants.EXT_K_BIND_PATH);

            jo = new JSONObject(fileContents);

        } catch (Exception e) {
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
        try {
            upArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_UP_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        JButton btnSaveUpArrow = new JButton("Save");
        btnSaveUpArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveUpArrow.setBounds(236, 150, 61, 19);
        mainFrame.getContentPane().add(btnSaveUpArrow);
        btnSaveUpArrow.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_UP_KEY, upArrowField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
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
        try {
            downArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_DOWN_KEY));
        } catch (JSONException e1) {
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
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_DOWN_KEY, downArrowField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
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
        try {
            leftArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_LEFT_KEY));
        } catch (JSONException e1) {
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
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_LEFT_KEY, leftArrowField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
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
        try {
            rightArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_RIGHT_KEY));
        } catch (JSONException e1) {
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
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_RIGHT_KEY, rightArrowField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 
     */
    private void setupWarningLabels () {
        setLblSshConnected(new JLabel("Pi Not Connected"));
        getLblSshConnected().setFont(new Font("Tahoma", Font.PLAIN, 17));
        getLblSshConnected().setForeground(Color.RED);
        getLblSshConnected().setBounds(21, 468, 166, 64);

        mainFrame.getContentPane().add(getLblSshConnected());

        setErrorLabel(new JLabel(""));
        getErrorLabel().setBounds(21, 444, 698, 26);
        getErrorLabel().setForeground(new Color(128, 0, 0));
        mainFrame.getContentPane().add(getErrorLabel());

        lblKeyBindings = new JLabel("Key Bindings:");
        lblKeyBindings.setForeground(Color.ORANGE);
        lblKeyBindings.setFont(new Font("Tahoma", Font.PLAIN, 24));
        lblKeyBindings.setBounds(153, 109, 157, 32);
        mainFrame.getContentPane().add(lblKeyBindings);
    }

    /**
     * 
     */
    private void displayDeployRunBtns () {
        setBtnDeploy(new JButton("Deploy"));
        getBtnDeploy().addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent arg0) {
                SSHUtil.transferFile(AppWindow.getInstance(), getFileTransfer());
            }
        });
        getBtnDeploy().setBounds(394, 482, 103, 35);
        getBtnDeploy().setEnabled(false);
        mainFrame.getContentPane().add(getBtnDeploy());

        setBtnRun(new JButton("Run"));
        getBtnRun().addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent arg0) {
                SSHUtil.runCode(AppWindow.getInstance(), getFileTransfer());
                btnRun.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });
        getBtnRun().setBounds(544, 482, 78, 35);
        getBtnRun().setEnabled(false);
        mainFrame.getContentPane().add(getBtnRun());
        
        btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent arg0) {
                getSSHCommandValue().writeVal(Constants.K_BIND_STOP_CHAR);
                btnRun.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
        btnStop.setEnabled(false);
        btnStop.setBounds(643, 482, 78, 35);
        mainFrame.getContentPane().add(btnStop);
       
        
    }

    /**
     * 
     */
    private void setupFileSelector () {
        JButton btnSelectFile = new JButton("Select File");
        btnSelectFile.setBackground(Color.RED);
        btnSelectFile.setForeground(Color.ORANGE);
        Font f = btnSelectFile.getFont();
        btnSelectFile.setFont(new Font("Tahoma", Font.PLAIN, 19));
        btnSelectFile.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent arg0) {
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose a file to be transferred.");
                fc.setCurrentDirectory(new File(System.getProperty("user.home")));
                fc.setPreferredSize(new Dimension(900, 900));
                fc.showOpenDialog(null);
                setFileTransfer(fc.getSelectedFile());
                if (getFileTransfer() == null)
                    return;
                String filePath = getFileTransfer().getAbsolutePath();
                fileTextField.setText("");
                fileTextField.setText(filePath);
                if (filePath != null && filePath.length() > 0 && getSession().isConnected()) {
                    String fileName = fc.getSelectedFile().getName();
                    if (fileName.substring(fileName.indexOf(".") + 1).equals("jar")) {
                        getBtnDeploy().setEnabled(true);
                        getBtnRun().setEnabled(true);
                    } else {
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
    }

    /**
     * 
     */
    private void displayMainFrame () {
        mainFrame = new JFrame();
        mainFrame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 23));
        mainFrame.setBounds(Constants.FRAME_LOCATION_X, Constants.FRAME_LOCATION_Y, Constants.FRAME_SIZE_X,
                Constants.FRAME_SIZE_Y); // 550 for exporting
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(null);
    }

    public JFrame getMainFrame () {
        return this.mainFrame;
    }

    /**
     * Gets the lblSshConnected.
     * @return the lblSshConnected
     */
    public JLabel getLblSshConnected () {
        return this.lblSshConnected;
    }

    /**
     * Sets lblSshConnected to a given value.
     * @param lblSshConnected the lblSshConnected to set
     *
     * @postcondition the lblSshConnected has been changed to lblSshConnected
     */
    public void setLblSshConnected (JLabel lblSshConnected) {
        this.lblSshConnected = lblSshConnected;
    }

    /**
     * Gets the session.
     * @return the session
     */
    public Session getSession () {
        return this.session;
    }

    /**
     * Sets session to a given value.
     * @param session the session to set
     *
     * @postcondition the session has been changed to session
     */
    public void setSession (Session session) {
        this.session = session;
    }

    /**
     * Gets the btnDeploy.
     * @return the btnDeploy
     */
    public JButton getBtnDeploy () {
        return this.btnDeploy;
    }

    /**
     * Sets btnDeploy to a given value.
     * @param btnDeploy the btnDeploy to set
     *
     * @postcondition the btnDeploy has been changed to btnDeploy
     */
    private void setBtnDeploy (JButton btnDeploy) {
        this.btnDeploy = btnDeploy;
    }

    /**
     * Gets the btnRun.
     * @return the btnRun
     */
    public JButton getBtnRun () {
        return btnRun;
    }

    /**
     * Sets btnRun to a given value.
     * @param btnRun the btnRun to set
     *
     * @postcondition the btnRun has been changed to btnRun
     */
    private void setBtnRun (JButton btnRun) {
        this.btnRun = btnRun;
    }

    /**
     * Gets the errorLabel.
     * @return the errorLabel
     */
    public JLabel getErrorLabel () {
        return this.errorLabel;
    }

    /**
     * Sets errorLabel to a given value.
     * @param errorLabel the errorLabel to set
     *
     * @postcondition the errorLabel has been changed to errorLabel
     */
    private void setErrorLabel (JLabel errorLabel) {
        this.errorLabel = errorLabel;
    }

    /**
     * Gets the clExec.
     * @return the clExec
     */
    public Channel getClExec () {
        return AppWindow.getInstance().clExec;
    }

    /**
     * Sets clExec to a given value.
     * @param clExec the clExec to set
     *
     * @postcondition the clExec has been changed to clExec
     */
    public void setClExec (Channel clExec) {
        AppWindow.getInstance().clExec = clExec;
    }

    public static AppWindow getInstance () {
        if (window == null)
            window = new AppWindow();
        return window;
    }

    /**
     * Gets the fileTransfer.
     * @return the fileTransfer
     */
    public File getFileTransfer () {
        return fileTransfer;
    }

    /**
     * Sets fileTransfer to a given value.
     * @param fileTransfer the fileTransfer to set
     *
     * @postcondition the fileTransfer has been changed to fileTransfer
     */
    public void setFileTransfer (File fileTransfer) {
        this.fileTransfer = fileTransfer;
    }

    /**
     * Gets the inputOutput.
     * @return the inputOutput
     */
    public PipedInputOutputWrapper getSSHCommandValue () {
        return sshCommandValue;
    }

    /**
     * Sets inputOutput to a given value.
     * @param inputOutput the inputOutput to set
     *
     * @postcondition the inputOutput has been changed to inputOutput
     */
    public void setSSHCommandValue (PipedInputOutputWrapper inputOutput) {
        this.sshCommandValue = inputOutput;
    }

    /**
     * Gets the outReader.
     * @return the outReader
     */
    public SystemOutReader getOutReader () {
        return outReader;
    }

    /**
     * Sets outReader to a given value.
     * @param outReader the outReader to set
     *
     * @postcondition the outReader has been changed to outReader
     */
    public void setOutReader (SystemOutReader outReader) {
        this.outReader = outReader;
    }

    /**
     * Gets the systemOut.
     * @return the systemOut
     */
    public PipedInputOutputWrapper getSystemOut () {
        return systemOut;
    }

    /**
     * Sets systemOut to a given value.
     * @param systemOut the systemOut to set
     *
     * @postcondition the systemOut has been changed to systemOut
     */
    public void setSystemOut (PipedInputOutputWrapper systemOut) {
        this.systemOut = systemOut;
    }
}
