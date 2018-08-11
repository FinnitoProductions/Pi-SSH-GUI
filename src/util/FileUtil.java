package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
     * Converts the contents of a given external file (stored in a directory not linked to the project) into a String
     * format.
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
        return fileContents;
    }

    /**
     * Converts the contents of a given local file (stored in a project directory past src). into a String format.
     * @param fileName the name of the file to be converted
     * @return the String format of the given file
     * @throws Exception if the file cannot be found or read from
     */
    public static String getStringFromLocalFile (String fileName)
            throws Exception {
        String fileContents = "";

        BufferedReader br = new BufferedReader(
                new InputStreamReader(AppWindow.getInstance().getClass()
                        .getResourceAsStream(fileName)));

        while (br.ready())
            fileContents += br.readLine()
                    + System.getProperty("line.separator");
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

    /**
     * Sets up an external file, copying into it the contents of internal file if non-existent.
     * @param externalPath the external path
     * @param internalPath the path of the internal file
     */
    public static void setupExternalFile (String externalPath, String internalPath) {
        try {
            File json = new File(externalPath);

            if (json.createNewFile())
                writeStringToFile(externalPath,
                        getStringFromLocalFile(internalPath));
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
    }

    /**
     * Deletes the file at the given path.
     * @param path the path of the file to be deleted
     */
    public static void deleteFile (String path) { 
        new File(path).delete();
    }

}
