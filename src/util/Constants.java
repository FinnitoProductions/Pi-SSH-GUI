package util;

import java.io.File;

/**
 * 
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class Constants {
    public static String PI_USER = "pi";
    public static String PI_PASSWORD = "team1072";
    public static String PI_IP = "192.168.0.63";
    public static int PI_PORT = 22;

    public static final int FRAME_LOC_X = 100;
    public static final int FRAME_LOC_Y = 100;
    public static final int FRAME_SIZE_X = 766;
    public static final int FRAME_SIZE_Y = 600;

    public static final String INT_K_BIND_PATH = "/bindings.json";
    public static final String INT_IP_BIND_PATH = "/ipbindings.json";
    public static final String USER_HOME = System.getProperty("user.home") + File.separator;
    public static final String EXT_DIR_NAME = "pi-ssh-app";
    public static final String EXT_DIR_PATH = USER_HOME + EXT_DIR_NAME;
    public static final String EXT_K_BIND_PATH = EXT_DIR_PATH + File.separator + "bindings.json";
    public static final String EXT_IP_BIND_PATH = EXT_DIR_PATH + File.separator + "ipbindings.json";
    
    public static final String HOME_ICON_PATH = "/images/homeicon.png";
    public static final String GRAPH_ICON_PATH = "/images/graphicon.jpg";
    public static final String USB_ICON_PATH = "/images/usbicon.png";

    public static final String K_BIND_UP_KEY = "up";
    public static final String K_BIND_DOWN_KEY = "down";
    public static final String K_BIND_LEFT_KEY = "left";
    public static final String K_BIND_RIGHT_KEY = "right";
    public static final String K_BIND_W_KEY = "W";
    public static final String K_BIND_A_KEY = "A";
    public static final String K_BIND_S_KEY = "S";
    public static final String K_BIND_D_KEY = "D";
    public static final String K_BIND_Q_KEY = "Q";
    public static final String K_BIND_O_KEY = "O";
    public static final String K_BIND_STOP_CHAR = "^C";
    
    public static final int GRAPH_X_INDEX = 0;
    public static final int GRAPH_Y_INDEX = 1;
    
    public static final String SMART_DASH_PREFIX = "SD: ";
    public static final int GRAPH_SIZE_X = 425;
    public static final int GRAPH_SIZE_Y = 400;
    public static final int GRAPH_LOC_X = 275;
    public static final int GRAPH_LOC_Y = 50;
    
    public static final String IP_BIND_1_KEY = "1";
    public static final String IP_BIND_2_KEY = "2";
    public static final String IP_BIND_3_KEY = "3";
    
    public static final String BINDINGS_INIT_CONTENT = "{\"A\":\"a\",\"S\":\"s\",\"D\":\"d\",\"left\":\"j\",\"W\":\"w\", \"Q\": \"q\", \"O\": \"o\", \"up\":\"i\",\"right\":\"l\",\"down\":\"k\"}";
    public static final String IP_INIT_CONTENT = "{\"1\": \"10.12.47.63\", \"2\": \"10.12.47.64\", \"3\": \"10.12.47.65\"}";
}
