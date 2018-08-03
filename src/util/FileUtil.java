package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import window.AppWindow;

/**
 * 
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class FileUtil
{

    public static String getStringFromExternalFile (String fileName) throws Exception
    {
        String fileContents = "";
    
        BufferedReader br = new BufferedReader(new FileReader(fileName));
    
        while (br.ready())
            fileContents += br.readLine() + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }

    public static String getStringFromLocalFile (String fileName) throws Exception
    {
        String fileContents = "";
    
        BufferedReader br = new BufferedReader(new InputStreamReader(AppWindow.getInstance().getClass().getResourceAsStream(Constants.INT_K_BIND_PATH)));
    
        while (br.ready())
            fileContents += br.readLine() + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }

    public static void writeStringToFile (String fileName, String newContents) throws Exception
    {
        BufferedWriter br = new BufferedWriter (new FileWriter(fileName));
        br.write(newContents);
        br.close();
    }

}
