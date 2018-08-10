package window;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;

import util.PrecisePoint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent.EventType;

import org.json.JSONException;
import org.json.JSONObject;
import org.knowm.xchart.XChartPanel;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

import util.Constants;
import util.FileUtil;
import util.SSHUtil;
import util.SetUtil;
import wrappers.PipedWrapper;
import wrappers.SmartDashboardEntry;
import wrappers.SmartDashboardProcessor;
import wrappers.SystemOutReader;
import javax.swing.JScrollBar;
import javax.swing.JRadioButton;

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
    private JButton homeButton;
    private JButton graphButton;
    private JButton usbButton;

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
    private JLabel lblW;
    private JLabel lblA;
    private JLabel lblS;
    private JLabel lblD;
    private JLabel lblO;
    private JLabel lblQ;

    private JTextField downArrowField;
    private JTextField leftArrowField;
    private JTextField rightArrowField;
    private JTextField wField;
    private JTextField aField;
    private JTextField sField;
    private JTextField dField;
    private JTextField oField;
    private JTextField qField;

    private JSONObject jo;

    private static AppWindow window;

    private PipedWrapper sshCommandValue;
    private PipedWrapper systemOut;

    private SystemOutReader outReader;

    private Grapher currentGraph;
    private Set<Grapher> graphs;

    private Map<PageType, Set<Container>> pageContents;

    private Socket s;
    
    private PageType currentPage;

    /**
     * 
     * @author Finn Frankis
     * @version Aug 7, 2018
     */
    public enum PageType {
        HOME, GRAPHS, BINDINGS
    }

    /**
     * Launches the application.
     * @param args the array of arguments passed in by the user when executing
     */
    public static void main (String[] args) {
        window = new AppWindow();

        window.getMainFrame().setTitle("Raspberry Pi SSH Deploy");
        window.getMainFrame().getContentPane().setBackground(Color.BLACK);

        window.getMainFrame().setVisible(true);

        /*
         * for (double i = 0; i < 100000; i += .1) { window.positionGraph.addPoint(i, Math.pow(i, 2)); //
         * mainFrame.getContentPane().add(positionGraph.getChartPanel()); window.mainFrame.repaint(); try {
         * Thread.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); } }
         */

        while (true) {
            System.out.println(window.graphs);
            if (window.getSession() == null || !window.getSession().isConnected()) {
                try {
                    SSHUtil.connectSSH(AppWindow.getInstance());
                   
                } catch (Exception e) {
                    window.getLblSshConnected().setText("Pi Not Connected");
                    window.getLblSshConnected().setForeground(Color.RED);
                }
            }
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /*SmartDashboardProcessor.addEntry (new SmartDashboardEntry(System.currentTimeMillis(), "Left Sensor Position", 10));
            try {
                Thread.sleep(50l);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
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
        pageContents = new HashMap<PageType, Set<Container>>();
        for (PageType p : PageType.values())
            pageContents.put(p, new HashSet<Container>());

        setupExternalFiles();

        displayMainFrame();

        setupFileSelector();

        displayDeployRunBtns();

        setupWarningLabels();

        setupKeyBindings();

        setupKeyChecking();

        outReader = new SystemOutReader();

        initializeGraph();

        setupPages();

    }

    /**
     * 
     */
    private void setupPages () {
        homeButton = setupImageButton(Constants.HOME_ICON_PATH, new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent e) {
                hideAllPageDependentContainers();
                showPage(PageType.HOME);
            }
        });
        homeButton.setBounds(0, 54, 61, 75);

        graphButton = setupImageButton(Constants.GRAPH_ICON_PATH, new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent e) {
                hideAllPageDependentContainers();
                showPage(PageType.GRAPHS);
            }
        });
        graphButton.setBounds(0, 130, 61, 75);

        usbButton = setupImageButton(Constants.USB_ICON_PATH, new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent e) {
                hideAllPageDependentContainers();
                showPage(PageType.BINDINGS);
            }
        });
        usbButton.setBounds(0, 206, 61, 75);

        hideAllPageDependentContainers();
        showPage(PageType.HOME);
    }

    private void showPage (PageType pt) {
        currentPage = pt;;
        for (Container c : pageContents.get(pt))
            c.setVisible(true);
    }

    private void hideAllPageDependentContainers () {
        for (PageType p : pageContents.keySet()) {
            for (Container c : pageContents.get(p)) {
                c.setVisible(false);
            }
        }
    }

    private JButton setupImageButton (String fileLocation, ActionListener al) {
        try {
            JButton b = new JButton(new ImageIcon(
                    ImageIO.read(getClass().getResource(fileLocation)).getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
            b.setVisible(true);;
            // to remote the spacing between the image and button's borders
            b.setMargin(new Insets(0, 0, 0, 0));
            // to remove the border
            b.setBorder(null);
            b.setContentAreaFilled(false);
            b.addActionListener(al);
            mainFrame.getContentPane().add(b);
            return b;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 
     */
    private void initializeGraph () {
        graphs = new HashSet<Grapher>();
        /*currentGraph = new Grapher("", "Time", "", "Pi Bot", Constants.GRAPH_SIZE_X, Constants.GRAPH_SIZE_Y,
                Constants.GRAPH_LOC_X, Constants.GRAPH_LOC_Y);
        graphs.add(currentGraph);
        Container c = currentGraph.getChartPanel();
        mainFrame.getContentPane().add(c);
        pageContents.get(PageType.GRAPHS).add(c);*/
    }

    /**
     * 
     */
    private void setupKeyChecking () {
        KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent (final KeyEvent e) {
                try {
                    if (getSession() != null && getSession().isConnected() && getClExec() != null
                            && getClExec().isConnected()) {
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
                            if (e.getKeyCode() == KeyEvent.VK_W) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_W_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_A) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_A_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_S) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_S_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_D) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_D_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_Q) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_Q_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_O) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_O_KEY));
                            }
                        }
                        if (e.getID() == KeyEvent.KEY_RELEASED) {
                            if (e.getKeyCode() == KeyEvent.VK_W) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_Q_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_A) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_Q_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_S) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_Q_KEY));
                            }
                            if (e.getKeyCode() == KeyEvent.VK_D) {
                                getSSHCommandValue().writeVal(jo.getString(Constants.K_BIND_Q_KEY));
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

        lblKeyBindings = new JLabel("Key Bindings:");
        lblKeyBindings.setForeground(Color.ORANGE);
        lblKeyBindings.setFont(new Font("Tahoma", Font.PLAIN, 24));
        lblKeyBindings.setBounds(77, 51, 157, 32);
        mainFrame.getContentPane().add(lblKeyBindings);
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
        lblUpArrow.setBounds(77, 88, 26, 19);
        mainFrame.getContentPane().add(lblUpArrow);

        upArrowField = new JTextField();
        upArrowField.setBounds(144, 88, 43, 19);
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
        btnSaveUpArrow.setBounds(211, 90, 61, 19);
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
        lblDownArrow.setBounds(77, 128, 56, 19);
        mainFrame.getContentPane().add(lblDownArrow);

        downArrowField = new JTextField();
        try {
            downArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_DOWN_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        downArrowField.setColumns(2);
        downArrowField.setBounds(144, 128, 43, 19);
        mainFrame.getContentPane().add(downArrowField);

        JButton btnSaveDownArrow = new JButton("Save");
        btnSaveDownArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveDownArrow.setBounds(211, 130, 61, 19);
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
        lblLeftArrow.setBounds(77, 167, 56, 19);
        mainFrame.getContentPane().add(lblLeftArrow);

        leftArrowField = new JTextField();
        try {
            leftArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_LEFT_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        leftArrowField.setColumns(2);
        leftArrowField.setBounds(144, 164, 43, 19);
        mainFrame.getContentPane().add(leftArrowField);

        JButton btnSaveLeftArrow = new JButton("Save");
        btnSaveLeftArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveLeftArrow.setBounds(211, 164, 61, 19);
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
        lblRightArrow.setBounds(77, 207, 56, 19);
        mainFrame.getContentPane().add(lblRightArrow);

        rightArrowField = new JTextField();
        try {
            rightArrowField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_RIGHT_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        rightArrowField.setColumns(2);
        rightArrowField.setBounds(144, 204, 43, 19);
        mainFrame.getContentPane().add(rightArrowField);

        JButton btnSaveRightArrow = new JButton("Save");
        btnSaveRightArrow.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveRightArrow.setBounds(211, 204, 61, 19);
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

        lblW = new JLabel("W:");
        lblW.setForeground(Color.ORANGE);
        lblW.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblW.setBounds(77, 247, 56, 19);
        mainFrame.getContentPane().add(lblW);

        wField = new JTextField();
        try {
            wField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_W_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        wField.setColumns(2);
        wField.setBounds(144, 247, 43, 19);
        mainFrame.getContentPane().add(wField);

        JButton btnSaveW = new JButton("Save");
        btnSaveW.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveW.setBounds(211, 247, 61, 19);
        mainFrame.getContentPane().add(btnSaveW);

        btnSaveW.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_W_KEY, wField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        lblA = new JLabel("A:");
        lblA.setForeground(Color.ORANGE);
        lblA.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblA.setBounds(77, 290, 56, 19);
        mainFrame.getContentPane().add(lblA);

        aField = new JTextField();
        try {
            aField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_A_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        aField.setColumns(2);
        aField.setBounds(144, 290, 43, 19);
        mainFrame.getContentPane().add(aField);

        JButton btnSaveA = new JButton("Save");
        btnSaveA.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveA.setBounds(211, 290, 61, 19);
        mainFrame.getContentPane().add(btnSaveA);

        btnSaveA.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_A_KEY, aField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        lblS = new JLabel("S:");
        lblS.setForeground(Color.ORANGE);
        lblS.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblS.setBounds(77, 333, 56, 19);
        mainFrame.getContentPane().add(lblS);

        sField = new JTextField();
        try {
            sField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_S_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        sField.setColumns(2);
        sField.setBounds(144, 333, 43, 19);
        mainFrame.getContentPane().add(sField);

        JButton btnSaveS = new JButton("Save");
        btnSaveS.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveS.setBounds(211, 333, 61, 19);
        mainFrame.getContentPane().add(btnSaveS);

        btnSaveS.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_S_KEY, sField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        lblD = new JLabel("D:");
        lblD.setForeground(Color.ORANGE);
        lblD.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblD.setBounds(77, 376, 56, 19);
        mainFrame.getContentPane().add(lblD);

        dField = new JTextField();
        try {
            dField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_D_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        dField.setColumns(2);
        dField.setBounds(144, 376, 43, 19);
        mainFrame.getContentPane().add(dField);

        JButton btnSaveD = new JButton("Save");
        btnSaveD.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveD.setBounds(211, 376, 61, 19);
        mainFrame.getContentPane().add(btnSaveD);

        btnSaveD.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_D_KEY, dField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        lblO = new JLabel("O:");
        lblO.setForeground(Color.ORANGE);
        lblO.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblO.setBounds(77, 376 + 43, 56, 19);
        mainFrame.getContentPane().add(lblO);

        oField = new JTextField();
        try {
            oField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_O_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        oField.setColumns(2);
        oField.setBounds(144, 376 + 43, 43, 19);
        mainFrame.getContentPane().add(oField);

        JButton btnSaveO = new JButton("Save");
        btnSaveO.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveO.setBounds(211, 376 + 43, 61, 19);
        mainFrame.getContentPane().add(btnSaveO);

        btnSaveO.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_O_KEY, oField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        lblQ = new JLabel("Q:");
        lblQ.setForeground(Color.ORANGE);
        lblQ.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblQ.setBounds(77, 376 + 43 * 2, 56, 19);
        mainFrame.getContentPane().add(lblQ);

        qField = new JTextField();
        try {
            qField.setText(jo == null ? "" : jo.getString(Constants.K_BIND_Q_KEY));
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        qField.setColumns(2);
        qField.setBounds(144, 376 + 43 * 2, 43, 19);
        mainFrame.getContentPane().add(qField);

        JButton btnSaveQ = new JButton("Save");
        btnSaveQ.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSaveQ.setBounds(211, 376 + 43 * 2, 61, 19);
        mainFrame.getContentPane().add(btnSaveQ);

        btnSaveQ.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0) {
                try {
                    jo.put(Constants.K_BIND_Q_KEY, qField.getText());
                    FileUtil.writeStringToFile(Constants.EXT_K_BIND_PATH, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        SetUtil.addMultiple(pageContents.get(PageType.BINDINGS), lblKeyBindings,
                lblUpArrow, upArrowField, btnSaveUpArrow,
                lblDownArrow, downArrowField, btnSaveDownArrow,
                lblLeftArrow, leftArrowField, btnSaveLeftArrow,
                lblRightArrow, rightArrowField, btnSaveRightArrow,
                lblW, wField, btnSaveW,
                lblA, aField, btnSaveA,
                lblS, sField, btnSaveS,
                lblD, dField, btnSaveD,
                lblO, oField, btnSaveO,
                lblQ, qField, btnSaveQ);
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
                AppWindow.getInstance().getSSHCommandValue().writeVal(Constants.K_BIND_STOP_CHAR);
                btnRun.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
        btnStop.setEnabled(false);
        btnStop.setBounds(643, 482, 78, 35);
        mainFrame.getContentPane().add(btnStop);

        SetUtil.addMultiple(pageContents.get(PageType.HOME), btnStop, btnRun, btnDeploy);
    }

    private void updateGraphList () {
        int startPositionY = 105;
        int deltaPositionY = 30;
        int currentPos = 0;
        for (Grapher g : graphs) {
            JRadioButton rdbtnGraph = new JRadioButton(g.getTitle());
            rdbtnGraph.setBounds(71, startPositionY + deltaPositionY * currentPos++, 201, 35);
            rdbtnGraph.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed (ActionEvent arg0) {

                }

            });
            window.getMainFrame().getContentPane().add(rdbtnGraph);
            pageContents.get(PageType.GRAPHS).add(rdbtnGraph);
        }
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
                if (filePath != null && filePath.length() > 0 && AppWindow.getInstance().getSession().isConnected()) {
                    String fileName = fc.getSelectedFile().getName();
                    if (fileName.substring(fileName.indexOf(".") + 1).equals("jar")) {
                        getBtnDeploy().setEnabled(true);
                        getBtnRun().setEnabled(true);
                    } else {
                        getBtnDeploy().setEnabled(false);
                        getBtnRun().setEnabled(false);
                    }
                } else {
                    getBtnDeploy().setEnabled(false);
                    getBtnRun().setEnabled(false);
                }

            }
        });
        btnSelectFile.setBounds(532, 50, 187, 38);
        mainFrame.getContentPane().add(btnSelectFile);

        fileTextField = new JTextField();
        fileTextField.setBounds(100, 50, 397, 38);
        mainFrame.getContentPane().add(fileTextField);
        fileTextField.setColumns(10);

        SetUtil.addMultiple(pageContents.get(PageType.HOME), btnSelectFile, fileTextField);
    }

    /**
     * 
     */
    private void displayMainFrame () {
        mainFrame = new JFrame();
        mainFrame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 23));
        mainFrame.setBounds(Constants.FRAME_LOC_X, Constants.FRAME_LOC_Y, Constants.FRAME_SIZE_X,
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

    /**
     * Gets this instance of AppWindow.
     * @return the instance of the singleton AppWindow
     */
    public static AppWindow getInstance () {
        if (window == null)
            window = new AppWindow();
        return window;
    }

    public static boolean hasInstance () {
        return window != null;
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
    public PipedWrapper getSSHCommandValue () {
        return sshCommandValue;
    }

    /**
     * Sets inputOutput to a given value.
     * @param inputOutput the inputOutput to set
     *
     * @postcondition the inputOutput has been changed to inputOutput
     */
    public void setSSHCommandValue (PipedWrapper inputOutput) {
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
    public PipedWrapper getSystemOut () {
        return systemOut;
    }

    /**
     * Sets systemOut to a given value.
     * @param systemOut the systemOut to set
     *
     * @postcondition the systemOut has been changed to systemOut
     */
    public void setSystemOut (PipedWrapper systemOut) {
        this.systemOut = systemOut;
    }

    /**
     * Adds a given graph to the set of available graphs.
     * @param grapher the graph to be added
     * 
     * @return true if a graph with the given title did not already exist; false otherwise
     */
    public boolean addGraph (Grapher grapher) {
        if (containsKey(grapher.getTitle()))
            return false;
        graphs.add(grapher);
        Container panel = grapher.getChartPanel();
        mainFrame.getContentPane().add(panel);
        for (Container c : pageContents.get(PageType.GRAPHS))
            if ((c instanceof XChartPanel) && c.isVisible())
                c.setVisible(false);
        addToPage (PageType.GRAPHS, panel);
        return true;
    }

    /**
     * Adds a given graph to the set of available graphs.
     * @param title the title of the graph to be added
     * 
     * @return true if a graph with the given title did not already exist; false otherwise
     */
    public boolean addGraph (String title) {
        return addGraph(new Grapher(title, "Time", "Value", "Pi Bot",
                Constants.GRAPH_SIZE_X, Constants.GRAPH_SIZE_Y, Constants.GRAPH_LOC_X, Constants.GRAPH_LOC_Y));
    }

    /**
     * Gets the set of all used graphs.
     * @return the set of used graphs
     */
    public Set<Grapher> getGraphs () {
        return graphs;
    }

    /**
     * Determines whether the window contains a graph with the given title.
     * @param title the title of the graph to be checked for
     * @return true if the window contains a graph with the title s; false otherwise
     */
    public boolean containsKey (String title) {
        return getGraph(title) != null;
    }

    /**
     * Gets the graph with the given title.
     * @param title the title to check for
     * @return the graph with the given title; null if nonexistent
     */
    public Grapher getGraph (String title) {
        for (Grapher g : graphs) {
            if (g != null && g.getTitle().equals(title))
                return g;
        }
        return null;
    }

    /**
     * Adds a point to the graph with the given key, creating it if non-existent.
     * @param title the title of the graph which the point will be added to
     * @param p the point which will be added to the graph
     */
    public void addPoint (String title, Point p) {
        if (!containsKey(title))
            addGraph(title);
        getGraph(title).addPoint(p);
        getMainFrame().repaint();
        getMainFrame().getContentPane().repaint();
    }
    
    public void addToPage (PageType page, Container c)
    {
        mainFrame.getContentPane().add(c);
        if (currentPage == page)
            c.setVisible(true);
        else
            c.setVisible(false);
    }

}
