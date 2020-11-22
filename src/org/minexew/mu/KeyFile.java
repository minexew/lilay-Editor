
package org.minexew.mu;

import java.util.*;

/**
 *
 * @author minexew
 */
public class KeyFile
{
    Hashtable<String, String> data = new Hashtable<String, String>();
    char delimiter;
    java.io.File file;

    public KeyFile( java.io.File file, char delimiter )
    {
        this.delimiter = delimiter;

        try
        {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader( this.file = file ) );

            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                int index = line.indexOf( delimiter );
                if ( index == -1 )
                    continue;

                data.put( line.substring( 0, index ), line.substring( index + 1 ) );
            }
        }
        catch ( java.io.IOException ex )
        {
        }
    }

    public boolean flush()
    {
        try
        {
            java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.FileWriter( file ) );

            Iterator<Map.Entry<String, String>> iter = data.entrySet().iterator();
            while ( iter.hasNext() )
            {
                Map.Entry<String, String> entry = iter.next();
                writer.write( entry.getKey() + delimiter + entry.getValue() + "\n" );
            }

            writer.close();
            return true;
        }
        catch ( java.io.IOException ex )
        {
            ex.printStackTrace();
            return false;
        }
    }

    public Iterator<Map.Entry<String, String>> getIterator()
    {
        return data.entrySet().iterator();
    }

    public String getValue( String key )
    {
        return data.get( key );
    }

    public boolean hasKeys( String keys[] )
    {
        for ( String key : keys )
            if ( data.get( key ) == null )
                return false;

        return true;
    }

    public String setValue( String key, String value )
    {
        return data.put( key, value );
    }
}
