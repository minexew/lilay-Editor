
package lilay;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import org.minexew.mu.KeyFile;

/**
 *
 * @author Minexewl
 */
public class Layer
{
    public static final byte Layer_image = 0, Layer_tileset = 1, Layer_collision = 2,
            Layer_object = 3, Layer_control = 4;

    public static final String layerTypeNames[] = { "image", "tileset", "collision",
            "object", "control" };

    byte type;
    String name;

    ImageResource imageSource = new ImageResource();
    Point pos, selectedTile, tiles[][], moveStart;
    Dimension tileSize, numTiles;
    float opacity = 1.f;
    boolean isBeingDragged, collisionMap[][];
    static boolean gridVisible = true, rightButton;

    public Layer( int type, String name )
    {
        this.type = ( byte )type;
        this.name = name;

        switch ( type )
        {
            case Layer_image:
                pos = new Point();
                break;

            case Layer_tileset:
                tileSize = new Dimension( 20, 20 );
                selectedTile = new Point();
                resize( Core.instance.mapSize );
                break;

            case Layer_collision:
                tileSize = new Dimension( 20, 20 );
                resize( Core.instance.mapSize );
                break;
        }
    }

    private Layer()
    {
    }

    static Layer create( KeyFile layerFile )
    {
        Layer newLayer = new Layer();
        return newLayer.read( layerFile ) ? newLayer : null;
    }

    public void dragged( int x, int y )
    {
        switch ( type )
        {
            case Layer_image:
                if ( isBeingDragged )
                    pos.setLocation( x - moveStart.x, y - moveStart.y );
                break;

            case Layer_tileset:
                if ( imageSource.image != null && x > 0 && x < Core.instance.mapSize.width
                        && y > 0 && y < Core.instance.mapSize.height )
                {
                    if ( !rightButton )
                        tiles[x / tileSize.width][y / tileSize.height].setLocation( selectedTile );
                    else
                        tiles[x / tileSize.width][y / tileSize.height].setLocation( 0, 0 );
                }
                break;

            case Layer_collision:
                if ( x > 0 && x < Core.instance.mapSize.width && y > 0 && y < Core.instance.mapSize.height )
                    collisionMap[x / tileSize.width][y / tileSize.height] = !rightButton;
                break;
        }
    }

    static byte getTypeByName( String typeString )
    {
        for ( byte i = 0; i < layerTypeNames.length; i++ )
            if ( layerTypeNames[i].equals( typeString ) )
                return i;

        return -1;
    }

    private void resizeTiles( Dimension size )
    {
        Dimension oldSize = ( numTiles != null ) ? ( Dimension )numTiles.clone() : null;
        numTiles = new Dimension( size.width / tileSize.width + 1, size.height / tileSize.height + 1 );

        Point[][] newTiles = new Point[numTiles.width][numTiles.height];
        for ( int x = 0; x < numTiles.width; x++ )
            for ( int y = 0; y < numTiles.height; y++ )
                newTiles[x][y] = new Point();

        if ( tiles != null )
        {
            int copyHeight = Math.min( numTiles.height, oldSize.height );
            for ( int col = 0; col < numTiles.width && col < oldSize.width; col++ )
                System.arraycopy( tiles[col], 0, newTiles[col], 0, copyHeight );
        }

        tiles = newTiles;
    }
    
    private void resizeCollisionMap( Dimension size )
    {
        Dimension oldSize = ( numTiles != null ) ? ( Dimension )numTiles.clone() : null;
        numTiles = new Dimension( size.width / tileSize.width + 1, size.height / tileSize.height + 1 );

        boolean[][] newMap = new boolean[numTiles.width][numTiles.height];

        if ( collisionMap != null )
        {
            int copyHeight = Math.min( numTiles.height, oldSize.height );
            for ( int col = 0; col < numTiles.width && col < oldSize.width; col++ )
                System.arraycopy( collisionMap[col], 0, newMap[col], 0, copyHeight );
        }

        collisionMap = newMap;
    }

    public void resize( Dimension size )
    {
        if ( type == Layer_tileset )
            resizeTiles( size );
        else if ( type == Layer_collision )
            resizeCollisionMap( size );
    }

    public void setTileSize( int w, int h )
    {
        tileSize.setSize( w, h );
        resize( Core.instance.mapSize );
    }

    public String getTypeDescription()
    {
        switch ( type )
        {
            case Layer_image:
                return "Image: " + imageSource.toHtml();

            case Layer_tileset:
                return "Tileset: " + imageSource.toHtml();

            case Layer_collision:
                return "Collision map (" + tileSize.width + "x" + tileSize.height + ")";

            case Layer_object:
                return "Objects";

            case Layer_control:
                return "Control"; // TODO: desc

            default:
                return "Unknown";
        }
    }

    void paintGrid( Graphics2D g2d, Dimension size )
    {
        float[] dash = { 1, 1 };
        g2d.setColor( Color.gray );
        g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 1.f ) );
        g2d.setStroke( new java.awt.BasicStroke( 1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, dash, 0 ) );

        for ( int x = 0; x < size.width; x += tileSize.width )
            g2d.drawLine( x, 0, x, size.height );

        for ( int y = 0; y < size.height; y += tileSize.height )
            g2d.drawLine( 0, y, size.width, y );
    }
    
    public void paint( Graphics2D g2d, ImageObserver obs, boolean isSelected )
    {
        switch ( type )
        {
            case Layer_image:
                if ( imageSource.image != null )
                {
                    g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity ) );
                    g2d.drawImage( imageSource.image, pos.x, pos.y, obs );

                    if ( isBeingDragged )
                    {
                        g2d.setColor( Color.red );
                        g2d.setStroke( new BasicStroke( 2 ) );
                        g2d.drawRect( pos.x, pos.y, imageSource.width, imageSource.height );
                    }
                }
                break;
                
            case Layer_tileset:
                g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity ) );
                for ( int x = 0; x < numTiles.width; x++ )
                    for ( int y = 0; y < numTiles.height; y++ )
                    {
                        int dx = x * tileSize.width, dy = y * tileSize.height;
                        int sx = tiles[x][y].x * tileSize.width,
                                sy = tiles[x][y].y * tileSize.height;
                        g2d.drawImage( imageSource.image,
                                dx, dy, dx + tileSize.width, dy + tileSize.height,
                                sx, sy, sx + tileSize.width, sy + tileSize.height,
                                obs );
                    }

                if ( isSelected && gridVisible )
                    paintGrid( g2d, Core.instance.mapSize );
                break;

            case Layer_collision:
                g2d.setColor( Color.red );
                g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f ) );

                for ( int x = 0; x < numTiles.width; x++ )
                    for ( int y = 0; y < numTiles.height; y++ )
                        if ( collisionMap[x][y] )
                        {
                            int dx = x * tileSize.width, dy = y * tileSize.height;
                            g2d.fillOval( dx + 1, dy + 1, tileSize.width - 2, tileSize.height - 2 );
                        }

                if ( isSelected && gridVisible )
                    paintGrid( g2d, Core.instance.mapSize );
                break;
        }
    }

    public void pressed( int x, int y, boolean right )
    {
        rightButton = right;

        switch ( type )
        {
            case Layer_image:
                if ( !right && imageSource.image != null )
                {
                    isBeingDragged = true;
                    moveStart = new Point( x - pos.x, y - pos.y );
                }
                break;

            case Layer_tileset:
                if ( imageSource.image != null && x > 0 && x < Core.instance.mapSize.width
                        && y > 0 && y < Core.instance.mapSize.height )
                {
                    if ( !rightButton )
                        tiles[x / tileSize.width][y / tileSize.height].setLocation( selectedTile );
                    else
                        tiles[x / tileSize.width][y / tileSize.height].setLocation( 0, 0 );
                }
                break;

            case Layer_collision:
                if ( x > 0 && x < Core.instance.mapSize.width && y > 0 && y < Core.instance.mapSize.height )
                    collisionMap[x / tileSize.width][y / tileSize.height] = !rightButton;
                break;
        }
    }

    public void released( int x, int y )
    {
        switch ( type )
        {
            case Layer_image:
                isBeingDragged = false;
                break;
        }
    }

    public void paintTileset( Graphics2D g2d, ImageObserver obs )
    {
        g2d.drawImage( imageSource.image, 0, 0, obs );
        paintGrid( g2d, new Dimension( imageSource.width, imageSource.height ) );

        g2d.setColor( Color.red );
        g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f ) );
        g2d.fillRect( selectedTile.x * tileSize.width, selectedTile.y * tileSize.height,
                tileSize.width, tileSize.height );
    }

    private boolean read( KeyFile layerFile )
    {
        String typeString = layerFile.getValue( "lilay-layer-type" );

        if ( typeString == null || ( type = getTypeByName( typeString ) ) < 0 )
            return false;

        name = layerFile.getValue( "lilay-layer-name" );

        if ( type == Layer_image || type == Layer_tileset )
        {
            String image = layerFile.getValue( "lilay-layer-image" );
            if ( image == null )
                System.err.println( "Loading Error: required property 'lilay-layer-image' not set." );
            else
                imageSource.set( image );
        }

        if ( type == Layer_tileset || type == Layer_collision )
        {
            String tileW = layerFile.getValue( "lilay-layer-tile-w" ),
                    tileH = layerFile.getValue( "lilay-layer-tile-h" );
            if ( tileW == null || tileH == null )
                System.err.println( "Loading Error: one or both of required properties 'lilay-layer-tile-w', 'lilay-layer-tile-h' not set." );
            else
                tileSize.setSize( Integer.parseInt( tileW ), Integer.parseInt( tileH ) );
            // TODO: read lilay-layer-tile-data
        }

        if ( type == Layer_image || type == Layer_object )
        {
            String x = layerFile.getValue( "lilay-layer-x" ),
                    y = layerFile.getValue( "lilay-layer-y" );
            if ( x == null || y == null )
                System.err.println( "Loading Error: one or both of required properties 'lilay-layer-x', 'lilay-layer-y' not set." );
            else
                pos.setLocation( Integer.parseInt( x ), Integer.parseInt( y ) );
        }

        if ( type == Layer_image )
        {
            String image = layerFile.getValue( "lilay-layer-image" ),
                    x = layerFile.getValue( "lilay-layer-x" ),
                    y = layerFile.getValue( "lilay-layer-y" ),
                    opacityString = layerFile.getValue( "lilay-layer-opacity" );

            imageSource.set( image );
            pos.setLocation( Integer.parseInt( x ), Integer.parseInt( y ) );
            opacity = Float.parseFloat( opacityString );
        }

        if ( type == Layer_image || type == Layer_tileset || type == Layer_object )
        {
            String opacityString = layerFile.getValue( "lilay-layer-opacity" );
            if ( opacityString == null )
                System.err.println( "Loading Error: required property 'lilay-layer-opacity' not set." );
            else
                opacity = Float.parseFloat( opacityString );
        }

        return true;
    }

    public void selectTile( int x, int y )
    {
        if ( x > 0 && x < imageSource.width && y > 0 && y < imageSource.height )
            selectedTile.setLocation( x / tileSize.width, y / tileSize.height );
    }

    void write( KeyFile layerFile )
    {
        layerFile.setValue( "lilay-layer-name", name );
        layerFile.setValue( "lilay-layer-type", layerTypeNames[type] );

        if ( type == Layer_image || type == Layer_tileset )
            layerFile.setValue( "lilay-layer-image", imageSource.originalName );

        if ( type == Layer_tileset || type == Layer_collision )
        {
            layerFile.setValue( "lilay-layer-tile-w", Integer.toString( tileSize.width ) );
            layerFile.setValue( "lilay-layer-tile-h", Integer.toString( tileSize.height ) );

            String layerData = "";
            if ( type == Layer_tileset )
            {
                for ( int y = 0; y < numTiles.height; y++ )
                    for ( int x = 0; x < numTiles.width; x++ )
                        layerData += String.format( "%04X%04X", tiles[x][y].x, tiles[x][y].y );
            }
            else if ( type == Layer_collision )
            {
                for ( int y = 0; y < numTiles.height; y++ )
                    for ( int x = 0; x < numTiles.width; x++ )
                        layerData += collisionMap[x][y] ? '1' : '0';
            }

            layerFile.setValue( "lilay-layer-tile-data", layerData );
        }

        if ( type == Layer_image || type == Layer_object )
        {
            layerFile.setValue( "lilay-layer-x", Integer.toString( pos.x ) );
            layerFile.setValue( "lilay-layer-y", Integer.toString( pos.y ) );
        }

        if ( type == Layer_image || type == Layer_tileset || type == Layer_object )
            layerFile.setValue( "lilay-layer-opacity", Float.toString( opacity ) );
    }

    public class ImageResource
    {
        public String originalName = null;
        public BufferedImage image = null;
        public int width = 0, height = 0;

        public String getResource( String name )
        {
            if ( name.length() == 0 )
                return name;

            else if ( name.charAt( 0 ) == '/' )
                return Core.resourceDir + name;
            else
                return Core.instance.getResourceRoot() + "/" + name;
        }

        public boolean reload()
        {
            if ( originalName == null )
            {
                image = null;
                return false;
            }

            try
            {
                image = ImageIO.read( new File( getResource( originalName ) ) );
                width = image.getWidth();
                height = image.getHeight();
                return true;
            }
            catch ( IOException ex )
            {
                System.out.println( "failed opening '" + originalName + "'" );
                return false;
            }
        }

        public boolean set( String name )
        {
            originalName = ( name.equals( "<none>" ) || name.isEmpty() ) ? null : name;
            return reload();
        }

        @Override public String toString()
        {
            return originalName != null ? originalName : "<none>";
        }

        public String toHtml()
        {
            return originalName != null ? originalName : "&lt;none&gt;";
        }
    }
}