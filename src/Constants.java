import java.io.File;

/**
 * 
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class Constants 
{
    public static final String user = "pi";
    public static final String password = "team1072";
    public static final String ip = "192.168.178.63";
    public static final int port = 22;
    public static final String srcBindingsName = "/bindings.json";
    public static final String dirName = "pi-ssh-app";
    public static final int FRAME_LOCATION_X = 100;
    public static final int FRAME_LOCATION_Y = 100;
    public static final int FRAME_SIZE_X = 766;
    public static final int FRAME_SIZE_Y = 600;
    public static final String userHome = System.getProperty("user.home") + File.separator;
    public static final String absoluteDirName = userHome + dirName;
    public static final String absoluteBindingsName = absoluteDirName + File.separator + "bindings.json";
    public static final String upKey = "up";
    public static final String downKey = "down";
    public static final String leftKey = "left";
    public static final String rightKey = "right";
}
