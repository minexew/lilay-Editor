/*
 * LilayEditorApp.java
 */

package lilay;

import java.io.*;
import java.util.EventObject;
import org.jdesktop.application.Application;
import org.jdesktop.application.Application.ExitListener;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class LilayEditorApp extends SingleFrameApplication
        implements ExitListener
{
    LilayEditorView view;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup()
    {
        new Core();
        addExitListener( this );

        try
        {
            FileInputStream fis = new FileInputStream( new File( "Ptolemy.GUI" ) );
            ObjectInputStream ois = new ObjectInputStream( fis );
            EditorSettings.instance = ( EditorSettings )ois.readObject();
        }
        catch ( Exception ex )
        {
            System.out.println( "Loading settings failed. (" + ex.getLocalizedMessage() + ")" );
            EditorSettings.instance = new EditorSettings();
        }

        try
        {
            show( view = new LilayEditorView( this ) );
        } catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of LilayEditorApp
     */
    public static LilayEditorApp getApplication()
    {
        return Application.getInstance( LilayEditorApp.class );
    }

    public static void main( String[] args )
    {
        launch( LilayEditorApp.class, args );
    }

    public boolean canExit( EventObject event )
    {
        return true;
    }

    public void willExit( EventObject event )
    {
        try
        {
            FileOutputStream fos = new FileOutputStream( new File( "Ptolemy.GUI" ) );
            ObjectOutputStream oos = new ObjectOutputStream( fos );
            oos.writeObject( EditorSettings.instance );
        }
        catch ( Exception ex )
        {
            System.out.println( "Saving settings failed. (" + ex.getLocalizedMessage() + ")" );
        }
    }

    public static ResourceMap getResourceMap( Class cl )
    {
        return getInstance().getContext().getResourceMap( cl );
    }
}
