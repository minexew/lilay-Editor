
package lilay;

import java.io.*;

/**
 *
 * @author Minexewl
 */
public class Resource
{
    String name, sourceName;

    private boolean transfer( File f1, File f2 )
    {
        try
        {
            InputStream in = new FileInputStream( f1 );
            OutputStream out = new FileOutputStream( f2 );

            byte[] buf = new byte[1024];
            int len;

            while ( ( len = in.read( buf ) ) > 0 )
                out.write( buf, 0, len );

            in.close();
            out.close();
            return true;
        }
        catch ( FileNotFoundException ex )
        {
            System.out.println( "transfer( '" + f1.getPath() + "' -> '" + f2.getPath() + "' ): "
                    + ex.getLocalizedMessage() );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean update()
    {
        return transfer( new File( sourceName ), new File( Core.resourceDir + name ) );
    }

    public boolean extract()
    {
        return transfer( new File( Core.resourceDir + name ), new File( sourceName ) );
    }

    public boolean delete()
    {
        return ( new File( Core.resourceDir + name ) ).delete();
    }

    private long getFileSize( File f )
    {
        return ( f.exists() && f.isFile() ) ? f.length() : -1;
    }

    public long getSourceSize()
    {
        return getFileSize( new File( sourceName ) );
    }

    public long getSize()
    {
        return getFileSize( new File( Core.resourceDir + name ) );
    }

    @Override public String toString()
    {
        return name + "\n" + sourceName;
    }
}
