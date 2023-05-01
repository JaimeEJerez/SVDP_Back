package fast_track;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.PreparedStatmentStruct.PreparedStatementItm;


public class PreparedStatmentExec
{
	static  Hashtable<String,Integer> sqlDataTypes2indx = new Hashtable<String,Integer>();
    static
    {
    	sqlDataTypes2indx.put("INT", 1);
    	sqlDataTypes2indx.put("BIT", 1);
    	sqlDataTypes2indx.put("TINYINT", 1);
    	sqlDataTypes2indx.put("SMALLINT", 1);
    	sqlDataTypes2indx.put("INTEGER", 1);
    	sqlDataTypes2indx.put("BIGINT", 1);
    				
    	sqlDataTypes2indx.put("FLOAT", 2);
    	sqlDataTypes2indx.put("REAL", 2);
    	sqlDataTypes2indx.put("DOUBLE", 2);
    	sqlDataTypes2indx.put("NUMERIC", 2);
    	sqlDataTypes2indx.put("DECIMAL", 2);
    	
    	sqlDataTypes2indx.put("CHAR", 3);
    	sqlDataTypes2indx.put("VARCHAR", 3);
    	sqlDataTypes2indx.put("LONGVARCHAR", 3);
    	sqlDataTypes2indx.put("ENUM", 3);
    	
    	sqlDataTypes2indx.put("BOOLEAN", 4);
    	
		sqlDataTypes2indx.put("DATE", 5);
		
		sqlDataTypes2indx.put("TIME", 6);
    }
	    
	private int									stsmCount			= 1;
	public 	PreparedStatement 					preparedStatement 	= null;
	
	
    public void setPrepBoolean(  LinkedTreeMap<String,Object> paramMap, String paramName ) throws SQLException
    {
    	Boolean value = null;
    	
		Object object = paramMap.get( paramName );
		
		if ( object == null )
		{
			object = paramMap.get( paramName.toUpperCase() );
		}
		
		if ( object instanceof String )
		{
			String param = (String)object;
				
			try
			{
				value = param == null ? null : Boolean.valueOf( param );
			}
	    	catch ( NumberFormatException e )
	    	{
	    		throw new SQLException( "Number format exception: " + paramName + " is not a boolean: " + "'" + param + "'" );
	    	}
		}
		
		setPrepBoolean( value );
    }

    public void setPrepBoolean( Boolean value ) throws SQLException
    {
		if ( value == null )
		{
			preparedStatement.setNull( stsmCount, Types.INTEGER);
		}
		else	
		{    		
			preparedStatement.setBoolean( stsmCount , value );
		}

		stsmCount++;
    }

    public void setPrepFloat( Float value ) throws SQLException
    {
		if ( value == null )
		{
			preparedStatement.setNull( stsmCount, Types.INTEGER);
		}
		else	
		{    		
			preparedStatement.setFloat( stsmCount , value );
		}

		stsmCount++;
    }

    public void setPrepFloat(  LinkedTreeMap<String,Object> paramMap, String paramName ) throws SQLException
    {
    	Object objPr = paramMap.get(paramName);
    	
		if ( objPr == null )
		{
			objPr = paramMap.get( paramName.toUpperCase() );
		}

    	String param = null;
    	
    	if ( objPr instanceof Double )
    	{
    		param = String.valueOf( (double)objPr );
    	}
    	else
    	{
    		param = (String)objPr;
    	}

    	try
    	{
			Double value = ( param  == null || param.isEmpty() )  ? null : Double.valueOf( param );
				
			setPrepDouble( value ) ;
    	}
    	catch ( NumberFormatException e )
    	{
    		throw new SQLException( "Number format exception: " + paramName + " is not a Double: " + "'" + param + "'" );
    	}
    
    	/*
		String param = paramMap.get( paramName );
		
		if ( param == null )
		{
			 param = paramMap.get( paramName.toUpperCase() );
		}

		try
		{
			Float value = param == null ? null : Float.valueOf( param );
			
	    	setPrepFloat( value ) ;
		}
    	catch ( NumberFormatException e )
    	{
    		throw new SQLException( "Number format exception: " + paramName + " is not a float: " + "'" + param + "'" );
    	}*/

    }

    public void setPrepDouble( Double value ) throws SQLException
    {
		if ( value == null )
		{
			preparedStatement.setNull( stsmCount, Types.INTEGER);
		}
		else	
		{    		
			preparedStatement.setDouble( stsmCount , value );
		}

    	stsmCount++;
    }
	
    public void setPrepInt( Integer value ) throws SQLException
    {
		if ( value == null )
		{
			preparedStatement.setNull( stsmCount, Types.INTEGER);
		}
		else	
		{    		
			preparedStatement.setInt( stsmCount , value );
		}

		stsmCount++;
    }

    public void setPrepInt( LinkedTreeMap<String,Object> paramMap, String paramName ) throws SQLException
    {
    	Object objPr = paramMap.get(paramName);
    	
		if ( objPr == null )
		{
			objPr = paramMap.get( paramName.toUpperCase() );
		}

    	String param = null;
    	
    	if ( objPr instanceof Double )
    	{
    		param = String.valueOf( (int)((double)objPr) );
    	}
    	else
    	{
    		param = (String)objPr;
    	}

    	try
    	{
			Integer value = ( param == null || param.isEmpty() ) ? null : Integer.valueOf( param );
				
	    	setPrepInt( value ) ;
    	}
    	catch ( NumberFormatException e )
    	{
    		throw new SQLException( "Number format exception: " + paramName + " is not a integer: " + "'" + param + "'" );
    	}
    }

    
    public void setPrepString( String value ) throws SQLException
    {
		if ( value == null )
		{
			preparedStatement.setNull( stsmCount, Types.VARCHAR );
		}
		else	
		{
			preparedStatement.setString( stsmCount , value);
		}
    	
    	stsmCount++;
    }
    
    public void setPrepTime(  LinkedTreeMap<String,Object> paramMap, String paramName ) throws SQLException
    {
    	String value = null;
    	
		Object object = paramMap.get( paramName );
		
		if ( object == null )
		{
			object = paramMap.get( paramName.toUpperCase() );
		}
		
		if ( object instanceof String )
		{
			String param = (String)object;

			value = param == null ? null : param.trim();
		}
		
		setPrepString( value );
    }
    
    
    public void setPrepDate(  LinkedTreeMap<String,Object> paramMap, String paramName ) throws SQLException
    {
    	String value = null;
    	
		Object object = paramMap.get( paramName );
		
		if ( object == null )
		{
			object = paramMap.get( paramName.toUpperCase() );
		}
		
		if ( object instanceof String )
		{
			String param = (String)object;
			
			value = param == null ? null : param.trim();
		}
		
		setPrepString( value );
    }

    public void setPrepString(  LinkedTreeMap<String,Object> paramMap, String paramName ) throws SQLException
    {
    	String value = null;
    	
		Object object = paramMap.get( paramName );
		
		if ( object == null )
		{
			object = paramMap.get( paramName.toUpperCase() );
		}
		
		if ( object instanceof String )
		{
			String param = (String)object;
						
			value = param == null ? null : param.trim();
		}
		
		setPrepString( value );
    }
        
    
    public int execute( Connection conn, 
    					LinkedTreeMap<String,Object> paramMap, 
    					PreparedStatmentStruct preparedStatmentStruct, String updateID ) throws SQLException
    {
    	String 								query 			= new String( preparedStatmentStruct.getQuery( 1 ) );
    	ArrayList<PreparedStatementItm> 	preparesItems 	= preparedStatmentStruct.getPreparedStatementItms();
    	
		this.preparedStatement = conn.prepareStatement( query );    		
 		
		stsmCount = 1;
		
		for ( PreparedStatementItm psi : preparesItems )
		{
			String type = psi.type.trim().toUpperCase();
			
			int p = type.indexOf('(');
			
			if ( p > 0 )
			{
				type = type.substring( 0, p );
			}
			
			Integer typeIndx =  sqlDataTypes2indx.get( type );
			
			
			if ( typeIndx != null )
			{
				switch ( typeIndx)
				{
				case 1:
					setPrepInt( paramMap, psi.name );
					break;
				case 2:
					setPrepFloat( paramMap, psi.name );
					break;
				case 3:
					setPrepString( paramMap, psi.name );
					break;
				case 4:
					setPrepBoolean( paramMap, psi.name );
					break;
				case 5:
					setPrepDate( paramMap, psi.name );
					break;
				case 6:
					setPrepTime( paramMap, psi.name );
				}
			}
			else
			{
				throw new SQLException( type + " not suported yet..." );
		/*    public static final int TIMESTAMP = 93;
			  public static final int BINARY = -2;
			  public static final int VARBINARY = -3;
			  public static final int LONGVARBINARY = -4;
			  public static final int NULL = 0;
			  public static final int OTHER = 1111;
			  public static final int JAVA_OBJECT = 2000;
			  public static final int DISTINCT = 2001;
			  public static final int STRUCT = 2002;
			  public static final int ARRAY = 2003;
			  public static final int BLOB = 2004;
			  public static final int CLOB = 2005;
			  public static final int REF = 2006;
			  public static final int DATALINK = 70;
			  public static final int ROWID = -8;
			  public static final int NCHAR = -15;
			  public static final int NVARCHAR = -9;
			  public static final int LONGNVARCHAR = -16;
			  public static final int NCLOB = 2011;
			  public static final int SQLXML = 2009;
			  public static final int REF_CURSOR = 2012;
			  public static final int TIME_WITH_TIMEZONE = 2013;
			  public static final int TIMESTAMP_WITH_TIMEZONE = 2014;*/
			}
		}
    		
		if ( updateID != null )
		{
			setPrepString( updateID );
		}
		
		int result = preparedStatement.executeUpdate();
		
    	return result;
    }	
    
    private String prepareSelectQuery(  PreparedStatmentStruct preparedStatmentStruct, Map<String, String[]> paramMap, Vector<Map<String, String>> resultVect, int neestDeep )
    {
    	String  	query 		= preparedStatmentStruct.getQuery( neestDeep );
		APIEntry 	apyEntry 	= preparedStatmentStruct.getAPIEntry();
		
		String[] selected_keys = apyEntry.select.select_keys;
		
		if ( selected_keys[0].equalsIgnoreCase( "$AND" ) )
		{
			for ( int i=0;i<selected_keys.length; i++ )
			{
				String 		name 	= selected_keys[i];
				String[] 	value 	= paramMap.get( name );
				
				if ( value != null )
				{
					query = query.replaceFirst( "\\?", name ).replaceFirst( "\\?",  "\"" + value[0] + "\"" );
				}
			}
		}
		else
		{
			for ( int i=0;i<selected_keys.length; i++ )
			{
				String 		name 	= selected_keys[i];
				String[] 	value 	= paramMap.get( name );
				
				for ( Map<String, String> map : resultVect )
				{
					if ( value == null  )
					{								
						String rslt  = map.get( name );
												
						if ( rslt != null )
						{
							value = new String[1];
							value[0] = rslt;
							break;
						}
					}
				}
				
				if ( value != null )
				{
					if ( value[0].startsWith("like(") )
					{
						int i1 = value[0].indexOf( "(" );
						int i2 = value[0].indexOf( ")" );
								
						String likeTxt = value[0].substring(i1+1,i2);
						
						likeTxt = " LIKE \"" + likeTxt.replace( '*', '%' ) + "\"";
						
						query = query.replaceFirst( "\\?", name ).replaceFirst( "=\\?",  likeTxt );
					}
					else
					{
						if ( value[0].contains("(") && value[0].contains(")") )
						{
							query = query.replaceFirst( "\\?", name ).replaceFirst( "\\?", value[0] );
						}
						else
						{
							query = query.replaceFirst( "\\?", name ).replaceFirst( "\\?",  "\"" + value[0] + "\"" );
						}
					}
					
					break;
				}
			}
		}

		return query;
    }
    
    
    
	public int executeQuery( MySQL mySQL, Map<String, String[]> paramMap, PreparedStatmentStruct preparedStatmentStruct, Vector<Map<String, String>> resultVect, int neestDeep ) throws SQLException
    {
		String query = prepareSelectQuery( preparedStatmentStruct, paramMap, resultVect, neestDeep );
    		
		mySQL.simpleHMapQueryAdd( query, resultVect );
				
		if ( mySQL.getLastError() != null )
		{
			throw new SQLException( mySQL.getLastError() );
		}
				
    	return resultVect.size();
    }	

        
    

}
