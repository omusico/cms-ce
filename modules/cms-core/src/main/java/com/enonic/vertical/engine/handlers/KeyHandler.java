/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.vertical.engine.handlers;

import java.sql.Connection;
import java.sql.SQLException;

import com.enonic.esl.sql.model.Column;
import com.enonic.esl.sql.model.Table;
import com.enonic.vertical.engine.VerticalEngineLogger;
import com.enonic.vertical.engine.VerticalKeyException;
import com.enonic.vertical.engine.XDG;

public class KeyHandler
    extends BaseHandler
{
    private final static String KEY_UPDATE = "UPDATE tKey SET key_lLastKey = key_lLastKey + ? WHERE key_sTableName = ?";

    public int generateNextKeyRange( String tableName, int count )
        throws VerticalKeyException
    {
        if ( count <= 0 )
        {
            String message = "Count must be at least 1.";
            VerticalEngineLogger.errorKey( message, null );
        }
        CommonHandler commonHandler = getCommonHandler();
        Connection con = null;
        int key;
        try
        {
            con = getConnection();
            Object[] values = {count, tableName.toLowerCase()};
            int rowCount = commonHandler.update( KEY_UPDATE, values );
            if ( rowCount == 0 )
            {
                key = count - 1;
                StringBuffer sql = XDG.generateInsertSQL( db.tKey );
                values = new Object[]{tableName.toLowerCase(), key};
                try
                {
                    commonHandler.update( sql.toString(), values );
                    String message = "Inserted new key(s) for table \"{0}\".";
                    VerticalEngineLogger.info(message, tableName, null );
                    key = 0;
                }
                catch ( SQLException sqle )
                {
                    String message = "Failed to insert new key(s) for table \"{0}\": %t";
                    VerticalEngineLogger.warn(message, tableName, sqle );
                    sql = XDG.generateSelectSQL( db.tKey, db.tKey.key_lLastKey, false, db.tKey.key_sTableName );
                    key = commonHandler.getInt( sql.toString(), tableName.toLowerCase() );
                }
            }
            else
            {
                StringBuffer sql = XDG.generateSelectSQL( db.tKey, db.tKey.key_lLastKey, false, db.tKey.key_sTableName );
                key = commonHandler.getInt( sql.toString(), tableName.toLowerCase() ) - ( count - 1 );
            }
        }
        catch ( SQLException sqle )
        {
            String message = "Failed to generate next key(s) for table \"{0}\"";
            VerticalEngineLogger.errorKey(message, tableName, sqle );
            key = -1;
        }
        finally
        {
            close( con );
        }

        return key;
    }

    public int generateNextKeySafe( String tableName )
        throws VerticalKeyException
    {
        Table table = db.getTable( tableName );
        Connection con = null;
        int key;
        try
        {
            con = getConnection();
            CommonHandler commonHandler = getCommonHandler();
            Column keyColumn = table.getPrimaryKeys()[0];
            StringBuffer sql = XDG.generateSelectSQL( table, keyColumn, false, keyColumn );
            do
            {
                key = generateNextKeyRange( tableName, 1 );
            }
            while ( commonHandler.getInt( sql.toString(), key ) >= 0 );
        }
        catch ( SQLException sqle )
        {
            String message = "Failed to generate next key for table \"{0}\"";
            VerticalEngineLogger.errorKey(message, tableName, sqle );
            key = -1;
        }
        finally
        {
            close( con );
        }
        return key;
    }

    public boolean keyExists( String tableName, int key )
    {
        Table table = db.getTable( tableName );
        StringBuffer sql = XDG.generateSelectSQL( table, table.getPrimaryKeys(), false, table.getPrimaryKeys() );
        return getCommonHandler().getInt( sql.toString(), key ) >= 0;
    }
}
