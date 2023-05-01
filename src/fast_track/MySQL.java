package fast_track;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import svdp.general.Globals;

public class MySQL 
{
	public static interface QueryCallBack
	{
		
		public boolean execute( ResultSet rs, int rowCount, int columnNumber, Object linkObj ) throws SQLException, IOException;

	}
	
	private String lastError = null;

	private Connection 	conn;
	private String 		db;

	private static final String host 	= Globals.mysqlHost;
	private static final String port 	= "3306";
	private static final String user 	= Globals.mysqlUser;
	private static final String pass 	= Globals.mysqlPass;
		
	public MySQL()
	{
		this.db 	= Globals.dataBase;
	}
	
	public MySQL( String dataBase )
	{
		this.db 	= dataBase;
	}


	/*
	public Connection connect()
	{
		if ( conn != null )
		{
			return conn;
		}
				
		String url = String.format("jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false", host, port, db);		
		
		try 
		{
			Class.forName("com.mysql.jdbc.Driver"); 
			
			conn = DriverManager.getConnection(url, user, pass);
		} 
		catch (SQLException e) 
		{
			lastError = ( "SQLException: " + e );
		} 
		catch (ClassNotFoundException e) 
		{
			lastError = ( "SQLException: " + e );
		} 
		
		return conn;
	}*/
	
	public Connection connect()
	{
		if ( conn != null )
		{
			return conn;
		}

		String url = String.format("jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=yes&characterEncoding=UTF-8", host, port, db);		
		
		try 
		{
			//Class.forName("com.mysql.jdbc.Driver"); 
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			conn = DriverManager.getConnection(url, user, pass);
		} 
		catch (SQLException e) 
		{
			lastError = ( "SQLException: " + e );
		} 
		catch (ClassNotFoundException e) 
		{
			lastError = ( "SQLException: " + e );
		} 
		
		return conn;
	}

		
	public void close()
	{
		if ( conn != null )
		{
			try
			{
				conn.close();
			} 
			catch (SQLException e)
			{
			}
			
			finally
			{
				conn = null;
			}
		}
	}

	public String getLastError()
	{
		return lastError;
	}

	public Map<String, Integer> executeCommandMAP( String command )
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
	
		int result = executeCommand( command );
		
		if ( result > 0 )
		{
			map.put( "NR_COLUMNAS_AFECTDAS", result );
		}
		
		return map;
	}

	public int executeCommand( String command )
	{
		if ( connect() == null )
			return -2;
		
		lastError = null;
		
		Statement 	stmt 			= null;
		int			returnValue 	= 0;
	
		try 
		{
		    stmt = conn.createStatement();
		    
		    returnValue = stmt.executeUpdate( command );
		} 
		catch (SQLException e) 
		{
			returnValue = -1;
			lastError = e.getMessage();
		    close();
		} 
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } 
		        catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		}
		    
		return returnValue;
	}
	

	public long get_mysql_insert_id()
	{
		long lastID = -1;
		
		try
		{
			lastID = Long.parseLong( simpleQuery( "SELECT LAST_INSERT_ID();" ) );
		} 
		catch (NumberFormatException e)
		{
			lastError = e.getMessage();
		}
		
		return lastID;
	}
	
	@SuppressWarnings("resource")
	public String simpleQuery( String query )
	{
		if ( connect() == null )
			return null;
		
		Statement 	stmt 		= null;
		ResultSet 	rs 			= null;
		String		returnValue = null;
	
		try 
		{
		    stmt = conn.createStatement();
		    
		    rs = stmt.executeQuery( query );

		    if ( rs != null ) 
		    {
		        rs = stmt.getResultSet();
		        	        		        
		        while (rs.next()) 
		        {
		        	if ( returnValue == null )
		        	{
		        		returnValue = rs.getString(1);
		        	}
		        }
		    }
		} 
		catch (SQLException e) 
		{
			lastError = e.getMessage();
		} 
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) 
		    {
		        try 
		        {
		            rs.close();
		        } 
		        catch (SQLException sqlEx) {  }

		        rs = null;
		    }

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } 
		        catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		}
		    
		return returnValue;
	}	
	
	public Vector<Map<String, String>> simpleHMapQuery( String query )
	{
		final  	Vector<Map<String, String>> resultVect 	= new  Vector<Map<String, String>>();
		
		this.callBackQuery(query, null, new QueryCallBack()
		{

			@Override
			public boolean execute(ResultSet rs, int rowCount, int columnNumber, Object linkObj) throws SQLException, IOException
			{
				ResultSetMetaData 			rsmd 		= rs.getMetaData();
				int 						numColumns 	= rsmd.getColumnCount();
				Map<String, String> 		resultMap 	= new HashMap<String, String>();
				
				for (int i=1; i<numColumns+1; i++) 
				{
				    String 	column_name = rsmd.getColumnName(i);
				    int 	column_type = rsmd.getColumnType(i);
				    	
				    switch ( column_type )
				    {
					    case java.sql.Types.CHAR:
					    {
					    	resultMap.put(column_name, rs.getString(column_name));
					    	
					    	break;
					    }
				        case java.sql.Types.ARRAY:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.BIGINT:
				        {
				        	resultMap.put(column_name, String.valueOf( rs.getInt(column_name) ));
				        	
				        	break;
				        }
				        case java.sql.Types.BOOLEAN:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.BLOB:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.DOUBLE:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.FLOAT:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.INTEGER:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.NVARCHAR:
				        {
				        	resultMap.put(column_name,  rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.VARCHAR:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.TINYINT:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.SMALLINT:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.DATE:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.TIMESTAMP:
				        {				        	
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        default:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        }
				    }
				}
				
				resultVect.add(resultMap);

				return true;
			}
		});
		
		
		return resultVect;
	}
	
	public int simpleHMapQueryAdd( final String query, final Vector<Map<String, String>> resultVect )
	{		
		int startSize = resultVect.size();
				
		this.callBackQuery(query, null, new QueryCallBack()
		{
			@Override
			public boolean execute(ResultSet rs, int rowCount, int columnNumber, Object linkObj) throws SQLException, IOException
			{
				ResultSetMetaData 			rsmd 		= rs.getMetaData();
				int 						numColumns 	= rsmd.getColumnCount();
				TreeMap<String, String> 	resultMap 	= new TreeMap<String, String>();
				
				resultVect.add(resultMap);

				for (int i=1; i<numColumns+1; i++) 
				{
				    String 	column_name = rsmd.getColumnLabel(i);
				    int 	column_type = rsmd.getColumnType(i);
				    String 	column_pref = column_name;
				    
				    System.out.println( column_name );
				    
				    if ( rs.findColumn(column_name) > 0 )
				    {
					    switch ( column_type )
					    {
						    case java.sql.Types.CHAR:
						    {
						    	resultMap.put(column_pref, rs.getString(column_name));
						    	
						    	break;
						    }
					        case java.sql.Types.ARRAY:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.BIGINT:
					        {
					        	resultMap.put(column_pref, String.valueOf( rs.getInt(column_name) ));
					        	
					        	break;
					        }
					        case java.sql.Types.BOOLEAN:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.BLOB:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.DOUBLE:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.FLOAT:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.INTEGER:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.NVARCHAR:
					        {
					        	resultMap.put(column_pref,  rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.VARCHAR:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.TINYINT:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.SMALLINT:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.DATE:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        case java.sql.Types.TIMESTAMP:
					        {				        	
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        	
					        	break;
					        }
					        default:
					        {
					        	resultMap.put(column_pref, rs.getString(column_name) );
					        }
					    }
				    }
				}
				
				return true;
			}
		});
		
		
		return resultVect.size() - startSize;
	}

	
	@SuppressWarnings("resource")
	public void callBackQuery( String query, Object linkObj, QueryCallBack queryCallBack )
	{
		if ( connect() == null )
			return;
		
		Statement 	stmt 		= null;
		ResultSet 	rs 			= null;
				
		try 
		{
		    stmt = conn.createStatement();
		    
		    rs = stmt.executeQuery( query );

		    stmt.setFetchSize(1000);
		    
		    if ( rs != null ) 
		    {
		        rs = stmt.getResultSet();
		        
		        java.sql.ResultSetMetaData md = rs.getMetaData();
	        
		        int columnNumber = md.getColumnCount();
		        int rowCounter	= 0;
		        
		        while (rs.next()) 
		        {
		        	try 
		        	{
						if ( queryCallBack.execute( rs, rowCounter++, columnNumber, linkObj ) == false )
						{
							break;
						}
					} 
		        	catch (IOException e) 
					{
		        		lastError = e.getMessage();
						break;
					}
		        }
		    }
		} 
		catch (SQLException e) 
		{
			lastError = e.getMessage();
			
			close();
		}
		
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) 
		    {
		        try 
		        {
		            rs.close();
		        } catch (SQLException sqlEx) {  }

		        rs = null;
		    }

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		}
		    
	}

	public Vector<String> simpleVQuery( String query )
	{
		Vector<String> resultVect = new Vector<String>();
		
		simpleVQueryAdd( query, resultVect );
		
		return resultVect;
	}
	
	public String[] simpleAQuery( String query )
	{
		Vector<String> resultVect = new Vector<String>();
		
		simpleVQueryAdd( query, resultVect );
		
		String[] retArr = new String[resultVect.size()];
				
		return resultVect.toArray( retArr );
	}
	
	@SuppressWarnings("resource")
	public int simpleVQueryAdd( String query, Vector<String> resultVect )
	{
		int columnCount = 0;
		
		if ( connect() == null )
			return -1;
		
		Statement 	stmt 		= null;
		ResultSet 	rs 			= null;
	
		lastError = null;
		
		try 
		{
		    stmt = conn.createStatement();
		    
		    rs = stmt.executeQuery( query );

		    if ( rs != null ) 
		    {
		        rs = stmt.getResultSet();
		        
		        java.sql.ResultSetMetaData md = rs.getMetaData();
	        
		        columnCount = md.getColumnCount();
		        
		        if ( resultVect != null )
		        {
			        while (rs.next()) 
			        {
		        		for ( int i = 1; i<=columnCount; i++)
			        	{
		        			String s = rs.getString(i);
		        			
		        			resultVect.add( s == null ? "" : s.trim() );
			        	}
			        }
		        }
		    }

		    // Now do something with the ResultSet ....
		} 
		catch (SQLException e) 
		{
			lastError = e.getMessage();
		} 
		
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) 
		    {
		        try 
		        {
		            rs.close();
		        } catch (SQLException sqlEx) {  }

		        rs = null;
		    }

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		}
		    
		return columnCount;
	}
 	 
	public static String getPassword()
	{
		return pass;
	}

	public static String geomFromCoordinates( double longitud, double latitud )
	{
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat format = new DecimalFormat("###.00000000",symbols);
		String sLongitud 	= format.format( longitud );
		String sLatitud 	= format.format( latitud );
		String geolocation	= "ST_GeomFromText('POINT(" + sLongitud + " " + sLatitud + ")')";
		return geolocation;
	}

	public Connection getConnection()
	{
		return this.conn;
	}

}
