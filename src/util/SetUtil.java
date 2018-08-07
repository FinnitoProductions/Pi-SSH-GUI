package util;

import java.util.Set;

/**
 * 
 * @author Finn Frankis
 * @version Aug 7, 2018
 */
public class SetUtil {
    public static void addMultiple (Set set, Object... objects)
    {
        for (Object o : objects)
            set.add(o);
    }
}
