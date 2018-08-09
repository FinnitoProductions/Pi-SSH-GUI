package util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import window.AppWindow;
import wrappers.PipedWrapper;

/**
 * Contains multiple methods for interfacing with an SSH device.
 * 
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class SSHUtil {

    /**
     * Forms the initial SSH connection with the Pi.
     * 
     * @param window the window class which will be used to connect
     */
    public static void connectSSH (AppWindow window) {
        try {
            JSch jsch = new JSch();

            window.setSession(jsch.getSession(Constants.PI_USER, Constants.PI_IP, Constants.PI_PORT));
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
    }

    /**
     * Transfers the actively selected file to the Pi via SFTP.
     */
    public static void transferFile (AppWindow window, File f) {
        if (window.getSession().isConnected() && f != null) {
            try {
                ChannelSftp sftpChannel = (ChannelSftp) window.getSession().openChannel("sftp");
                sftpChannel.connect();
                sftpChannel.put(f.getAbsolutePath(), f.getName());
                System.out.println(f.getName());
                sftpChannel.disconnect();
            } catch (JSchException | SftpException e) {
                window.getErrorLabel().setText("ERROR: File could not be transferred.");
            }
        }
    }

    /**
     * Runs the deployed code on the Pi.
     */
    public static void runCode (AppWindow window, File f) {
        if (window.getSession().isConnected() && f != null) {
            try {
                window.setClExec(window.getSession().openChannel("exec"));
                //window.clExecReadOutput = window.getSession().openChannel("exec");
                
                if (window.getSystemOut() != null)
                    window.getSystemOut().stop();
                window.setSystemOut(new PipedWrapper());
                window.getClExec().setOutputStream(window.getSystemOut().getOutputStream());
                
                if (window.getSSHCommandValue() != null)
                    window.getSSHCommandValue().stop();
                window.setSSHCommandValue(new PipedWrapper());
                window.getClExec().setInputStream(window.getSSHCommandValue().getInputStream());
                ((ChannelExec) window.getClExec()).setCommand("sudo java -jar " + f.getName());
                window.getClExec().connect();
                
                //window.clExecReadOutput.connect();
            } catch (JSchException e) {
                window.getErrorLabel().setText("ERROR: Code could not be run.");
            }

        }
    }

}
