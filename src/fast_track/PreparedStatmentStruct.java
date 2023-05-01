package fast_track;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import fast_track.MySQL.QueryCallBack;
import init.InitTables.ExternalReferences;


public class PreparedStatmentStruct
{
	public enum MODE
	{
		INSERT,
		UPDATE,
		SELECT,
		DELETE,
		OPTIONS
	}
	
	public static class PreparedStatementItm
	{
		public String 	name;
		public String 	type;
		public boolean 	nullable;
		
		public PreparedStatementItm( String name, String type, boolean 	nullable )
		{
			this.name 		= name;
			this.type 		= type;
			this.nullable 	= nullable;
		}
	}

	private static final Map<String, PreparedStatmentStruct> preparedStatmentStructMap 	= new HashMap<String, PreparedStatmentStruct>();
	
	private  	APIEntry 							apiEntry			= null;
	private 	String 								query				= null;
	private 	ArrayList<PreparedStatementItm> 	preparesItems 		= new ArrayList<PreparedStatementItm>();
	
	public static PreparedStatmentStruct create( ServletContext servletContext, MySQL mysql, String tableName, APIEntry apiEntry, MODE mode, ExternalReferences externalReferences  ) throws FileNotFoundException, KeyException, SQLException, IOException
	{		
		switch ( mode )
		{
			case DELETE:
				if ( apiEntry.delete.delete_key == null )
					return null;
				break;
			case INSERT:
				if ( apiEntry.insert.insert_fields == null )
					return null;
				break;
			case SELECT:
				if ( apiEntry.select.select_keys == null )
					return null;
				break;
			case UPDATE:
				if ( apiEntry.update.update_key == null )
					return null;
				break;
			case OPTIONS:
				if ( apiEntry.options.options_fields == null )
					return null;
				break;

			default:
				break;
		}
		
		PreparedStatmentStruct preparedStatmentStruct = new PreparedStatmentStruct( mysql, tableName, apiEntry, mode );
					
		String key = tableName + "_" + mode.toString();
		
		preparedStatmentStructMap.put( key.toUpperCase(), preparedStatmentStruct );
		
		return preparedStatmentStruct;
	}
	
	private PreparedStatementItm 					updateKey 	= null;
	private PreparedStatementItm 					deleteKey 	= null;
	private MODE 									mode		= null;
	private String 									tableName	= null;
	
	public String getTableName()
	{
		return tableName;
	}
	
	public APIEntry getAPIEntry()
	{
		return apiEntry;
	}
	
	public String getSelectFields( String prefix, String as )
	{		
		String names	= null;
		
		for ( PreparedStatementItm psi : this.preparesItems )
		{
			String fieldName  = as == null ? psi.name : (psi.name + " as \"" + as + "." + psi.name + "\"") ;
					
			if ( prefix != null )
			{
				fieldName = prefix + "." + fieldName;
			}
			
			names 	= names == null ? ( "\n " + fieldName ) : ( names + ",\n " + fieldName );
		}
		
		return names;
	}
	
	public String getSelectWhere()
	{
		String[] selctKeys 	= apiEntry.select.select_keys;
		String   comparator = apiEntry.select.comparator == null ? "=" : apiEntry.select.comparator;
		
		String where = null;
				
		if ( selctKeys[0].equalsIgnoreCase("$AND") ) 
		{
	    	for ( int i=1; i<selctKeys.length; i++)
	    	{
	    		where  	= where == null ? " WHERE ?" + comparator + "?" : ( where + " AND ?=?" );
	    	}
		}
		else
		{
	    	where = " WHERE ?" + comparator + "?";
		}
		
		return where;
	}
	
	public PreparedStatmentStruct( MySQL mysql, String tableName,  APIEntry apiEntry, MODE mode ) throws SQLException, FileNotFoundException, IOException, KeyException
	{		
		this.apiEntry 			= apiEntry;
		this.mode 				= mode;
		this.tableName			= tableName; 
		
		mysql.callBackQuery( "SHOW COLUMNS FROM " + tableName, null, new QueryCallBack()
		{
			@Override
			public boolean execute(ResultSet rs, int rowCount, int columnNumber, Object linkObj) throws SQLException, IOException
			{
				String 	columnName 	= rs.getString( 1 );
				String 	columnType 	= rs.getString( 2 ).toUpperCase();
				boolean nullable 	= rs.getString( 3 ).equalsIgnoreCase("YES");
				
				switch( mode )
				{
					case INSERT:

						if ( apiEntry.contains( mode, columnName, true ) )
						{
				            preparesItems.add( new PreparedStatementItm( columnName, columnType, nullable ) );
						}

						break;
					case SELECT:

						if ( apiEntry.contains( mode, columnName, true ) )
						{
				            preparesItems.add( new PreparedStatementItm( columnName, columnType, nullable ) );
						}

						break;
					case OPTIONS:

						if ( apiEntry.contains( mode, columnName, true ) )
						{
				            preparesItems.add( new PreparedStatementItm( columnName, columnType, nullable ) );
						}

						break;

					case UPDATE:
						
						if ( columnName.equalsIgnoreCase( apiEntry.update.update_key ))
						{
							updateKey = new PreparedStatementItm( columnName, columnType, nullable );
						}

						if ( apiEntry.contains( mode, columnName, false ) )
						{
				            preparesItems.add( new PreparedStatementItm( columnName, columnType, nullable ) );
						}
						
						break;
					case DELETE:
						if ( columnName.equalsIgnoreCase( apiEntry.delete.delete_key ))
						{
							deleteKey = new PreparedStatementItm( columnName, columnType, nullable );
							
							preparesItems.add( deleteKey );
						}

						break;
				default:
					break;
				}

	            		
				return true;
				
			}
		});
		
		switch ( mode )
		{
			case INSERT:
			{
				if ( apiEntry.insert.insert_fields != null )
				{
					String names	= null;
					String values	= null;		
					
					/*
					if ( apiEntry != null && apiEntry.insert.generete_uuid != null )
					{
						names 	= apiEntry.insert.generete_uuid;
						values 	= "?";
					}*/					
					
					for ( PreparedStatementItm psi : this.preparesItems )
					{
						names 	= names  == null ? psi.name : (names + ", " + psi.name );
						values	= values == null ? "?" : (values + ",?" );
					}
					
			        query  	= "INSERT INTO " + tableName + "( " + names + " ) VALUES( " + values + " )";
				}
				break;
			}
			case SELECT:
			{
				if ( apiEntry.foreingTables == null || apiEntry.foreingTables.length == 0 )
				{					
					String names = getSelectFields( null, null );
					String where = getSelectWhere();					
			    	
					query  	= "SELECT " + names + " FROM  " + tableName + " " + where;
				}
		    	break;
			}
			case UPDATE:
			{
				if ( apiEntry.update.update_fields != null )
				{
					String nameValues	= null;
					
					for ( PreparedStatementItm psi : this.preparesItems )
					{
						nameValues 	= nameValues == null ? ( psi.name + "=IFNULL( ?, " + psi.name + ")" ) : ( nameValues + ", " + psi.name + "=IFNULL( ?, " + psi.name + ")" );
					}
					
					if ( updateKey == null )
					{
						throw new SQLException( "null updateKey in " + tableName + " PreparedStatmentStruct UPDATE mode" );
					}
					
					{
				    	query  	= "UPDATE " + tableName + "\r\nSET ";
				            
						query += nameValues + "\r\nWHERE " + updateKey.name + "=?";
					}
					
				}
				break;
			}
			case DELETE:
			{	
				if ( apiEntry.delete.delete_key != null )
				{
					if ( deleteKey == null )
					{
						throw new KeyException( "null deleteKey in " + tableName + " PreparedStatmentStruct DELETE mode" );
					}
					
					query  	= "DELETE FROM " + tableName + " WHERE " + deleteKey.name + "=?"; 
					
					//preparesItems.add( deleteKey );
				}
				
				break;
			}
		default:
			break;
		}
	}

	public String getQuery( int forceDeep )
	{
		if ( this.mode == MODE.SELECT )
		{
			if ( apiEntry.select.select_keys != null )
			{					
				String names 	= getSelectFields( "t1", null );
				String inners 	= "";
				String where 	= getSelectWhere();					
		    	
				if ( (apiEntry.neestDeep > 0 || forceDeep > 0)  && apiEntry.foreingTables != null && apiEntry.foreingTables.length > 0 )
				{
					for ( int i = 0; i<apiEntry.foreingTables.length; i++ )
					{
						if ( apiEntry.foreingTables[i].visible )
						{
							String key = apiEntry.foreingTables[i].tablenName + "_" + MODE.SELECT.toString();
							
							PreparedStatmentStruct pss = get( key );
							
							names += "," + pss.getSelectFields( apiEntry.foreingTables[i].tablenName, apiEntry.foreingTables[i].tablenName );
							
							if ( !inners.isEmpty() )
							{
								inners += "\n";
							}
							
							inners += " INNER JOIN " + apiEntry.foreingTables[i].tablenName + " USING(" + apiEntry.foreingTables[i].columnName + ")";
						}
					}
					
					inners += "\n";
				}
				query = "SELECT " + names + "\nFROM " + tableName + " t1\n" + inners + where;
				
				if ( apiEntry.select.order != null )
				{
					query  = query + " " + apiEntry.select.order;
				}
			}			
		}
		
		return query;
	}

	public static PreparedStatmentStruct get(String key)
	{
		return preparedStatmentStructMap.get(key.toUpperCase());
	}
	
	public static  Map<String, PreparedStatmentStruct> getPreparedStatmentStructMap()
	{
		return preparedStatmentStructMap;
	}

	public ArrayList<PreparedStatementItm> getPreparedStatementItms()
	{
		return preparesItems;
	}	
    
}
