package util;

/**
 * 
 * @author Finn Frankis
 * @version Aug 6, 2018
 */
public class StringUtil {
    /**
     * Determines whether a given String starts with a given value.
     * @param toCheck the String to be checked
     * @param value the value to check for at the beginning
     * @return true if the given String starts with the given value; false otherwise
     */
    public static boolean startsWithValue (String toCheck, String value) {
        if (toCheck.length() < value.length())
            return false;
        return toCheck.substring(0, value.length()).equals(value);
    }

    /**
     * Determines the index of the first numeric character in this String.
     * @param s the String to be checked
     * @return the index of the first numeric character; -1 if none are found
     */
    public static int indexOfNumber (String s) {
        for (int i = 0; i < s.length(); i++) {
            int compareTo = s.substring(i, i + 1).compareTo("0");
            if (compareTo <= 9 && compareTo >= 0)
                return i;
        }
        return -1;
    }
}
