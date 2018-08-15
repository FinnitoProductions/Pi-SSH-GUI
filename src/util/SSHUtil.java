package util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import window.AppWindow;
import wrappers.ConnectionThread;
import wrappers.PipedWrapper;

/**
 * Contains multiple methods for interfacing with an SSH device.
 * 
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class SSHUtil {
    private static Thread connectionThread;
    private static AppWindow connectingWindow;
    private static boolean canReconnect;

    /**
     * Forms the initial SSH connection with the Pi.
     * 
     * @param window the window class which will be used to connect
     */
    public static void connectSSH (AppWindow window) {
        if (window.canReconnect()) {
            System.out.println("CONNECTING TO " + window.getSelectedIP());
            connectingWindow = window;
            window.setCanReconnect(false);
            (connectionThread = new ConnectionThread(window)).start();
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
                File file = new File(Constants.EXT_DIR_PATH + File.separator + "output.txt");
                f.createNewFile();
                window.getClExec().setOutputStream(new FileOutputStream(file));
                ((ChannelExec) window.getClExec()).setCommand("sudo java -jar " + f.getName());
                window.getClExec().connect();
                window.closePiSocket();
                
                Thread.sleep(1000);
                boolean isReady = false;
                while (!isReady)
                {
                    try {
                        System.out.println("trying to connect again");
                        window.setPiSocket(new Socket(window.getSelectedIP(), Constants.SOCKET_PORT));
                        window.setSocketWriter(new PrintWriter(window.getPiSocket().getOutputStream()));
                        System.out.println("socket connected");
                        isReady = true;
                    } catch (ConnectException e)
                    {
                        Thread.sleep(50l);
                    }
                } 
            } catch (Exception e) {
                window.getErrorLabel().setText("ERROR: Code could not be run.");
                e.printStackTrace();
            }

        }
    }

    public static Thread getConnectionThread () {
        return connectionThread;
    }

    public static void stopConnecting () {
        getConnectionThread().stop();
        connectingWindow.setCanReconnect(true);
        connectingWindow.getLblSshConnected().setText("Changing Connection...");
    }

}
