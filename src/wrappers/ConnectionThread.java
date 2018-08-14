package wrappers;

import java.awt.Color;
import java.awt.Font;

import com.jcraft.jsch.JSch;

import util.Constants;
import window.AppWindow;

/**
 * 
 * @author Finn Frankis
 * @version Aug 13, 2018
 */
public class ConnectionThread extends Thread {
    private AppWindow window;

    public ConnectionThread (AppWindow window) {
        this.window = window;
    }

    public void run () {
        try {
            JSch jsch = new JSch();

            window.setSession(
                    jsch.getSession(Constants.PI_USER, window.getSelectedIP(), Constants.PI_PORT));
            window.getSession().setPassword(Constants.PI_PASSWORD);
            window.getSession().setConfig("StrictHostKeyChecking", "no");
            window.getLblSshConnected().setText("Connecting...");

            window.getLblSshConnected().setForeground(Color.BLUE);
            window.getSession().connect();

            if (window.getSession().isConnected()) {
                window.getLblSshConnected().setText("Pi Connected");
                window.getLblSshConnected().setForeground(new Color(105, 196, 80));
                Font f = window.getLblSshConnected().getFont();
                window.getLblSshConnected().setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            }
        } catch (Exception e) {
            e.printStackTrace();
            window.getLblSshConnected().setText("Pi Not Connected");
            window.getLblSshConnected().setForeground(Color.RED);
            window.getBtnDeploy().setEnabled(false);
            window.getBtnRun().setEnabled(false);
        }
        window.setCanReconnect(true);
    }
}
