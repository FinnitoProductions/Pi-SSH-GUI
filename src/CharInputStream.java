import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author Finn Frankis
 * @version Jul 30, 2018
 */
public class CharInputStream extends InputStream
{
    private byte[] currentChar;
    private int currentPointer;
    private Queue<byte[]> characters;
    
    public CharInputStream()
    {
        currentPointer = -1;
        characters = new LinkedList<byte[]>();
    }
    public void addChar (String character)
    {
        characters.add(character.getBytes());
        if (currentChar == null || currentPointer > currentChar.length || currentPointer == -1)
        {
            currentPointer = 0;
            currentChar = characters.remove();
        }
            
    }
    /**
    * @return
    * @throws IOException
    */
    @Override
    public int read() throws IOException
    {
        if (currentPointer > currentChar.length || currentChar == null) 
        {
            if (!characters.isEmpty())
            {
                currentChar = characters.remove();
                currentPointer = 0;
            }
            return -1;
        }
        return currentChar[currentPointer++];
    }

}
