package util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import window.AppWindow;

/**
 * 
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class SSHUtil
{

    /**
     * Forms the initial SSH connection with the Pi.
     */
    public static void connectSSH() throws Exception
    {
        AppWindow window = AppWindow.getInstance();
        try
        {
            JSch jsch = new JSch();
            
            window.setSession(jsch.getSession(Constants.PI_USER, Constants.PI_IP, Constants.PI_PORT));
            window.getSession().setPassword(Constants.PI_PASSWORD);
            window.getSession().setConfig("StrictHostKeyChecking", "no");
            window.getLblSshConnected().setText("Connecting...");
            window.getLblSshConnected().setForeground(Color.BLUE);
            window.getSession().connect();
            
            if (window.getSession().isConnected())
            {
                window.getLblSshConnected().setText("Pi Connected");
                window.getLblSshConnected().setForeground(new Color(105, 196, 80));
                Font f = window.getLblSshConnected().getFont();
                window.getLblSshConnected().setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            }
        }
        catch (Exception e)
        {
            window.getLblSshConnected().setText("Pi Not Connected");
            window.getLblSshConnected().setForeground(Color.RED);
            window.getBtnDeploy().setEnabled(false);
            window.getBtnRun().setEnabled(false);
        }
    }
    
    /**
     * Transfers the actively selected file to the Pi via SFTP.
     */
    public static void transferFile (Session s, File f) 
    {
        AppWindow window = AppWindow.getInstance();
        if (s.isConnected() && f != null)
        {
            try
            {
                ChannelSftp sftpChannel = (ChannelSftp) s.openChannel("sftp");
                sftpChannel.connect();
                sftpChannel.put(f.getAbsolutePath(), f.getName());
                sftpChannel.disconnect();
            }
            catch (JSchException | SftpException e)
            {
                window.getErrorLabel().setText("ERROR: File could not be transferred.");
            }
        }
    }

    /**
     * Runs the deployed code on the Pi.
     */
    public static void runCode (Session s, File f)
    {
        if (s.isConnected() && f != null)
        {
            try
            {
                AppWindow.setClExec(s.openChannel("exec"));
                AppWindow.getClExec().setOutputStream(System.out);
    
                ((ChannelExec)AppWindow.getClExec()).setCommand("sudo java -jar " + f.getName());
                AppWindow.getClExec().connect();
            }
            catch (JSchException e)
            {
                AppWindow.getErrorLabel().setText("ERROR: Code could not be run.");
            }
            
        }
    }
    
}
