package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import window.AppWindow;

/**
 * Contains multiple methods for handling both internal and external files.
 * @author Finn Frankis
 * @version Aug 3, 2018
 */
public class FileUtil {
    /**
     * Converts the contents of a given external file (stored in a directory not
     * linked to the project) into a String format.
     * @param fileName the name of the file to be converted
     * @return the String format of the given file
     * @throws Exception if the file cannot be found or read from
     */
    public static String getStringFromExternalFile (String fileName)
            throws Exception {
        String fileContents = "";

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        while (br.ready())
            fileContents += br.readLine()
                    + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }

    /**
     * Converts the contents of a given local file (stored in a project directory
     * past src). into a String format.
     * @param fileName the name of the file to be converted
     * @return the String format of the given file
     * @throws Exception if the file cannot be found or read from
     */
    public static String getStringFromLocalFile (String fileName)
            throws Exception {
        String fileContents = "";

        BufferedReader br = new BufferedReader(
                new InputStreamReader(AppWindow.getInstance().getClass()
                        .getResourceAsStream(Constants.INT_K_BIND_PATH)));

        while (br.ready())
            fileContents += br.readLine()
                    + System.getProperty("line.separator");
        System.out.println(fileContents);
        return fileContents;
    }

    /**
     * Replaces the contents of a file with a String value.
     * @param fileName the name of the file for which the contents will be replaced
     * @param newContents the String containing the new file contents
     * @throws Exception if the file cannot be find or written to
     */
    public static void writeStringToFile (String fileName, String newContents)
            throws Exception {
        BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
        br.write(newContents);
        br.close();
    }

}
