
package lilay;

import java.io.*;

/**
 *
 * @author Minexewl
 */
public class EditorSettings implements Serializable
{
    public static EditorSettings instance;

    public String encoder = "";
    public boolean autoImportResources = true;

    /*private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
    }*/
}
