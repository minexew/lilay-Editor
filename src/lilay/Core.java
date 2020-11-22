
package lilay;

import java.awt.Dimension;
import java.io.*;
import java.util.Vector;
import org.minexew.mu.KeyFile;

/**
 *
 * @author Minexewl
 */
public class Core
{
    public static Core instance;
    public static final String tempDir = "projectTemp",
            layersDir = tempDir + "/lilay.Map.layers",
            resourceDir = tempDir + "/resources";

    EditorSettings config;
    Dimension mapSize;
    String mapID, mapName, mapAuthor, resRoot;
    public int backgroundColor;
    public Vector<Layer> layers;
    public Vector<Resource> resources;

    public Core()
    {
        instance = this;
        layers = new Vector<Layer>();
        resources = new Vector<Resource>();

        checkDir( tempDir, false ); // todo: set to true once packing works
        checkDir( resourceDir, false );

        clear();
    }

    public void clear()
    {
        mapID = "NewMap";
        mapName = "Untitled-1";
        mapAuthor = "Unknown";
        mapSize = new Dimension( 320, 320 );
        resRoot = ".";
        backgroundColor = 0;
        layers.clear();
    }

    public void checkDir( String name, boolean mustEmpty )
    {
        File dir = new File( name );

        if ( mustEmpty && dir.isDirectory() )
            deleteDir( dir );

        if ( mustEmpty || !dir.isDirectory() )
            dir.mkdirs();
    }

    public static boolean deleteDir( File dir )
    {
        if ( dir.isDirectory() )
        {
            String[] children = dir.list();
            for ( int i = 0; i < children.length; i++ )
                if ( !deleteDir( new File( dir, children[i] ) ) )
                    return false;
        }

        return dir.delete();
    }

    public String getResourceRoot()
    {
        return resRoot + "/";
    }

    public void setResourceRoot( String dir )
    {
        dir = dir.replace( '\\' , '/' );
        if ( dir.endsWith( "/" ) )
            resRoot = dir.substring( 0, dir.length() - 1 );
        else
            resRoot = dir;
    }
    
    public boolean load( File mapPackageFile )
    {
        try
        {
            // Smash the layers dir to prevent loading zombies
            checkDir( layersDir, true );

            String[] command = { EditorSettings.instance.encoder, "open", tempDir, mapPackageFile.getAbsolutePath() };
            //Runtime.getRuntime().exec( command );

            KeyFile mapInfoFile = new KeyFile( new File( tempDir + "/lilay.Map" ), ':' );
            int version = Integer.parseInt( mapInfoFile.getValue( "ptolemy-version" ) );

            if ( version == 100 )
            {
                String[] requiredKeys = { "lilay-id", "lilay-map-title",
                        "lilay-map-author", "lilay-map-w", "lilay-map-h" };
                if ( !mapInfoFile.hasKeys( requiredKeys ) )
                    System.err.println( "Loading Error: one or more of the required keys is missing" );

                mapID = mapInfoFile.getValue( "lilay-id" );
                mapName = mapInfoFile.getValue( "lilay-map-title" );
                mapAuthor = mapInfoFile.getValue( "lilay-map-author" );
                mapSize.setSize( Integer.parseInt( mapInfoFile.getValue( "lilay-map-w" ) ),
                        Integer.parseInt( mapInfoFile.getValue( "lilay-map-h" ) ) );
            }
            else
                System.err.println( "Loading Error: undefined or unknown format version" );

            KeyFile resourcesFile = new KeyFile( new File( tempDir + "/lilay.Map.resources" ), ':' );
            java.util.Iterator<java.util.Map.Entry<String,String>> iter = resourcesFile.getIterator();
            while ( iter.hasNext() )
            {
                java.util.Map.Entry<String, String> entry = iter.next();
                Resource res = new Resource();
                res.name = entry.getKey();
                res.sourceName = entry.getValue();
                resources.add( res );
            }

            for ( int layerId = 0; ; layerId++ )
            {
                KeyFile layerFile = new KeyFile( new File( layersDir + "/" + layerId ), ':' );
                Layer newLayer = Layer.create( layerFile );
                if ( newLayer != null )
                    layers.add( newLayer );
                else
                    break;
            }

            return true;
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean save( File mapPackageFile, boolean editable )
    {
        try
        {
            KeyFile mapInfoFile = new KeyFile( new File( tempDir + "/lilay.Map" ), ':' );
            mapInfoFile.setValue( "lilay-id", mapID );
            mapInfoFile.setValue( "lilay-map-title", mapName );
            mapInfoFile.setValue( "lilay-map-author", mapAuthor );
            mapInfoFile.setValue( "lilay-map-w", Integer.toString( mapSize.width ) );
            mapInfoFile.setValue( "lilay-map-h", Integer.toString( mapSize.height ) );
            mapInfoFile.flush();

            if ( editable )
            {
                KeyFile resourcesFile = new KeyFile( new File( tempDir + "/lilay.Map.resources" ), ':' );
                for ( Resource res : resources )
                    resourcesFile.setValue( res.name, res.sourceName );
                resourcesFile.flush();
            }

            int layerId = 0;
            checkDir( layersDir, true );
            for ( Layer layer : layers )
            {
                KeyFile layerFile = new KeyFile( new File( layersDir + "/" + layerId ), ':' );
                layer.write( layerFile );
                layerFile.flush();
                layerId++;
            }

            String[] command = { EditorSettings.instance.encoder, "make", tempDir, mapPackageFile.getAbsolutePath() };
            //Runtime.getRuntime().exec( command );
            return true;
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            return false;
        }
    }
}
