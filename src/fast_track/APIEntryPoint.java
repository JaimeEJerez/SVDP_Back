package fast_track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.PreparedStatmentStruct.MODE;
import svdp.general.Globals;
import svdp.servlets_auto.SecurityTokens;
import svdp.tcp.DebugServer;

/**
 * Servlet implementation class APIEntryPoint00
 */
//@WebServlet("/FastTrack")
public abstract class APIEntryPoint extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       	
	protected boolean verifyAuthorizationKeyPost( MySQL mySQL, String token ) throws SQLException
	{
		if ( !useAuthorizationKeyPost() )
		{
			return true;
		}
		
		String result = SecurityTokens.validateToken(mySQL, token);
		
		return result != null && !result.equalsIgnoreCase( SecurityTokens.expiredTime );
	}
	
	protected boolean useAuthorizationKeyPost()
	{
		return true;
	}
	
	protected boolean verifyAuthorizationKeyPut( MySQL mySQL, String token ) throws SQLException
	{
		if ( !useAuthorizationKeyPut() )
		{
			return true;
		}
		
		String result = SecurityTokens.validateToken(mySQL, token);
		
		return result != null && !result.equalsIgnoreCase( SecurityTokens.expiredTime );
	}

	protected boolean useAuthorizationKeyPut()
	{
		return true;
	}

	
	protected boolean verifyAuthorizationKeyOptions( MySQL mySQL, String token ) throws SQLException
	{
		if ( !useAuthorizationKeyOptions() )
		{
			return true;
		}

		String result = SecurityTokens.validateToken(mySQL, token);
		
		return result != null && !result.equalsIgnoreCase( SecurityTokens.expiredTime );
	}

	protected boolean useAuthorizationKeyOptions()
	{
		return true;
	}

	
	protected boolean verifyAuthorizationKeyDelete( MySQL mySQL, String token ) throws SQLException
	{
		if ( !useAuthorizationKeyDelete() )
		{
			return true;
		}

		String result = SecurityTokens.validateToken(mySQL, token);
		
		return result != null && !result.equalsIgnoreCase( SecurityTokens.expiredTime );
	}
	
	protected boolean useAuthorizationKeyDelete()
	{
		return true;
	}

	protected boolean verifyAuthorizationKeyGet( MySQL mySQL, String token ) throws SQLException
	{
		if ( !useAuthorizationKeyGet() )
		{
			return true;
		}

		String result = SecurityTokens.validateToken(mySQL, token);
		
		return result != null && !result.equalsIgnoreCase( SecurityTokens.expiredTime );
	}

	protected boolean useAuthorizationKeyGet()
	{
		return true;
	}



    /**
     * @see HttpServlet#HttpServlet()
     */
    public APIEntryPoint() 
    {
        super();
    }

    /*
		SELECT 
			HauseholdheadID,
		    Email,
		    Name, 
		    LastName, 
		    Age,
		    ChapterID,
		    ChapterName as "ChapterID.ChapterName",
		    HelpGroupID,
		    HelpGroupName as "HelpGroupID.HelpGroupName"
		FROM
		    hauseholdheads
		INNER JOIN chapters USING (ChapterID)
		INNER JOIN helpgroups USING (HelpGroupID)
     */
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		DebugServer.println( this.getServletName() + ".doGet"  );
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		String neestDeepStr = request.getParameter( "neestDeep" );
		
		int neestDeep = neestDeepStr == null ? 0 : Integer.valueOf( neestDeepStr  );
		
		String key = getTableName() + "_" + MODE.SELECT.toString();
		
		PreparedStatmentStruct preparedStatmentStruct = PreparedStatmentStruct.get( key );		
						
		MySQL mySQL = new MySQL();
				
		try
		{
			APIEntry ae = preparedStatmentStruct.getAPIEntry();
			
			boolean useToken = ae.select.useToken();

			if ( useToken && !verifyAuthorizationKeyGet( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or expired Token" ), osw );
			}	
			else
			{
				PreparedStatmentExec pse = new PreparedStatmentExec();
				
				Vector<Map<String, String>> resultVect = new Vector<Map<String, String>>();
				
				get_PreProcess(  mySQL, request, response, resultVect );

				pse.executeQuery( mySQL, request.getParameterMap() , preparedStatmentStruct, resultVect, neestDeep );			
				
				JSONResponse posP = null;
				
				if ( resultVect.size() == 0 )
				{
					 posP = get_PosProcess( mySQL, request, response, JSONResponse.not_success( 777, "Empy result" ) );
				}
				else
				{
					 posP = get_PosProcess( mySQL, request, response, JSONResponse.success( resultVect ) );
				}
	
				response.setStatus( HttpServletResponse.SC_OK );
				
			    gson.toJson( posP, osw );
			}
		} 
		catch (SQLException e)
		{
			DebugServer.printException( "SQLException", e  );
			
			gson.toJson( JSONResponse.not_success( 1452, e.getMessage()), osw );
			
			//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());// setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		}
		finally
		{
			mySQL.close();
			
			osw.flush();
		}
	}
	
	private static class RollBackInfo
	{
		public String 	tableName;
		public String 	colmnName;
		public long		insertion;
		
		public RollBackInfo( String tableName, String colmnName, long insertion )
		{
			this.tableName = tableName;
			this.colmnName = colmnName;
			this.insertion = insertion;
		}
	}
	
	public static class Index
	{
		public long i = 0;
	}
	
	protected JSONResponse postAction( 	MySQL 							mySQL,
										String 							tableName,
										HttpServletRequest 				request, 
										HttpServletResponse 			response, 
										PreparedStatmentStruct 			preparedStatmentStruct, 
										LinkedTreeMap<String,Object> 	paramMap)
	{	
		DebugServer.println( "postAction()" );

		JSONResponse postResponse = null;
				
		Vector<RollBackInfo>	roolBabkVect		= new Vector<RollBackInfo>();
		int 					nAffectedRows 		= 0;
		Exception 				roolBackException	= null;
		
		APIEntry apiEntry = preparedStatmentStruct.getAPIEntry();
	
		int size = apiEntry.foreingTables == null ? 0 : apiEntry.foreingTables.length;
			
		for ( int i=0; i<size; i++ )
		{
			String foreingTable = apiEntry.foreingTables[i].tablenName;
			String foreingColmn = apiEntry.foreingTables[i].columnName;
			boolean isVisible 	= apiEntry.foreingTables[i].visible;
			
			if ( isVisible )
			{
				LinkedTreeMap<String,Object> 	sub_paramMap 				= new LinkedTreeMap<String,Object>();
				Set<String> 					paramMap_keySet 			= paramMap.keySet();
				Iterator<String> 				paramMap_keySet_iterator 	= paramMap_keySet.iterator();
				String 							key2 						= null;
				PreparedStatmentStruct 			pss 						= null;
				PreparedStatmentExec 			pse 						= null;

				try
				{
					while ( paramMap_keySet_iterator.hasNext() )
					{
						String paramName = paramMap_keySet_iterator.next();
						
						if ( paramName.toUpperCase().startsWith( foreingTable.toUpperCase() + "." ) )
						{							
							String sub_paramName = paramName.substring( foreingColmn.length() + 1 );
							
							Object object = paramMap.get( paramName );
							
							if ( object instanceof String )
							{
								String subValue = (String)object;
								
								sub_paramMap.put( sub_paramName, subValue );
							}
						}
					}
				
					if ( sub_paramMap.isEmpty() )
					{
						//paramMap.put( foreingColmn, "1" );
					}
					else
					{
						key2 						= foreingTable + "_" + MODE.INSERT.toString();
						pss 						= PreparedStatmentStruct.get(key2);
						pse 						= new PreparedStatmentExec();
						
						nAffectedRows += pse.execute( mySQL.connect(), sub_paramMap, pss, null );	
						
						long subInsertID = mySQL.get_mysql_insert_id();
						
						if ( subInsertID > -1 )
						{
							paramMap.put( foreingColmn, String.valueOf(subInsertID) );
							
							roolBabkVect.add( new RollBackInfo( foreingTable, foreingColmn, subInsertID) );
						}
					}
				}
				catch ( SQLException e )
				{
					DebugServer.println( pse.preparedStatement.toString() + "\r\n" );
					DebugServer.printException( "Exception", e  );
					roolBackException = e;
				}
				catch ( Exception e )
				{
					DebugServer.printException( "Exception", e  );
					roolBackException = e;
					break;
				}
			}
		}
		
		if ( roolBackException == null )
		{
			try
			{
				PreparedStatmentExec pse = new PreparedStatmentExec();
				
				if ( apiEntry != null && apiEntry.insert.generete_uuid != null )
				{
					UUID 			uuid 		= UUID.randomUUID();
					String			confUUID 	= uuid.toString();
		
					paramMap.put( apiEntry.insert.generete_uuid, confUUID );
				}
				
				nAffectedRows += pse.execute( mySQL.connect(), paramMap, preparedStatmentStruct, null );			
											
				if ( nAffectedRows > 0 )
				{
					long insertedID = mySQL.get_mysql_insert_id();
					
					Map<String, Object> resultMap = new HashMap<String, Object>();
		
					resultMap.put( "insertedID", String.valueOf( insertedID ) );
					
					if ( apiEntry.insert.returns != null )
					{
						for ( String k : apiEntry.insert.returns )
						{
							Object o = paramMap.get( k );
							
							if ( o instanceof String )
							{
								String v = (String)paramMap.get( k );
								
								if ( v != null && v != null )
								{
									resultMap.put( k, v );
								}
							}
						}
					}
					
					postResponse = post_PosProcess( mySQL, request, response, resultMap, paramMap );
				}
				else
				{
					postResponse = post_PosProcess( mySQL, request, response, null, paramMap );
				}
				
				//gson.toJson( postResponse, osw );
			}
			catch ( Exception e )
			{
				DebugServer.printException( "Exception", e  );
				
				roolBackException = e;
			}
		}
		
		if ( roolBackException != null )
		{
			for ( RollBackInfo rbi : roolBabkVect )
			{
				String command = "DELETE FROM " + rbi.tableName + " WHERE " + rbi.colmnName + "=" + rbi.insertion;
						
				mySQL.executeCommand(command);
			}
			
			 postResponse = JSONResponse.not_success( 777, roolBackException.getMessage() );
			 
			 //gson.toJson( postResponse, osw );
		}
		
		return postResponse;
	}
	
	@SuppressWarnings("unchecked")
	private Vector<LinkedTreeMap<String,Object>>  getParamMap( HttpServletRequest request, Gson gson ) throws Exception, IOException
	{
		Vector<LinkedTreeMap<String,Object>> resultVect = null;
		
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	int comment = line.lastIndexOf("//");
        	
        	if ( comment >= 0 )
        	{
        		line = line.substring( 0, comment);
        	}
        	
        	jsontxt += line;
        }

        jsontxt = jsontxt.trim();
        
        DebugServer.println( "" );
        DebugServer.println( jsontxt );
        DebugServer.println( "" );
        
        if ( jsontxt.charAt(0) == '[' )
        {
        	resultVect = gson.fromJson( jsontxt, Vector.class );
        }
        else
        {
        	LinkedTreeMap<String,Object> paramMap = (LinkedTreeMap<String, Object>)gson.fromJson( jsontxt, LinkedTreeMap.class );
        
        	resultVect = new Vector<LinkedTreeMap<String,Object>>(1);
        	
        	resultVect.add(paramMap);
        }
        
		return resultVect;
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		DebugServer.println( this.getServletName() + ".doPost"  );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		Vector<LinkedTreeMap<String, Object>> paramMapVect;
		
		try
		{
			paramMapVect = getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1251, e1.getMessage() ), osw );
			DebugServer.printException( "IOException", e1  );
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1261, e1.getMessage() ), osw );
			DebugServer.printException( "Exception", e1  );
			osw.flush();
			return;
		}	

		String key = getTableName() + "_" + MODE.INSERT.toString();
		
		PreparedStatmentStruct preparedStatmentStruct = PreparedStatmentStruct.get(key);
		
		MySQL mySQL = new MySQL();
		
		try
		{
			boolean useToken = preparedStatmentStruct.getAPIEntry().insert.useToken();
			
			if ( useToken && !verifyAuthorizationKeyPost( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
			}	
			else
			{
				Vector<JSONResponse> responseVect = new Vector<JSONResponse>();
				
				if ( paramMapVect.size() > 1 )
				{
					post_VectPreProcess( mySQL, paramMapVect, request, response );
				}
				
				for ( int i=0; i<paramMapVect.size(); i++ )
				{
					try
					{
						LinkedTreeMap<String, Object> paramMap = paramMapVect.get(i);
						
						post_PreProcess( mySQL, paramMap, request, response );
		
						JSONResponse actionResponse = postAction( mySQL, getTableName(), request, response, preparedStatmentStruct, paramMap);
					
						responseVect.add( actionResponse );
					}
					catch (Exception e)
					{
						JSONResponse actionResponse = JSONResponse.not_success( 1021, e.getMessage() );
						
						DebugServer.printException( "Exception", e  );
						
						responseVect.add( actionResponse );
					}
				}
				
				if ( paramMapVect.size() > 1 )
				{
					post_VectPosProcess( mySQL, paramMapVect, request, response, responseVect );
				}

				if ( responseVect.size() == 1 )
				{
					gson.toJson( responseVect.get(0), osw );
				}
				else
				{
					gson.toJson( responseVect, osw );
				}
			}
		} 
		catch (Exception e)
		{
			gson.toJson( JSONResponse.not_success( 1031, e.getMessage() ), osw );
			
			DebugServer.printException( "Exception", e  );
		} 
		finally
		{
			mySQL.close();
		}
				
		osw.flush();
	}

	

	protected int putAction( 	MySQL 							mySQL,
								HttpServletRequest 				request, 
								HttpServletResponse 			response, 
								PreparedStatmentStruct 			preparedStatmentStruct, 
								LinkedTreeMap<String,Object> 	paramMap,
								Gson 							gson,
								OutputStreamWriter 				osw,
								int								rowsAffected) throws SQLException
	{	
		APIEntry apiEntry = preparedStatmentStruct.getAPIEntry();

		String updateID = null;
		
		if ( apiEntry.foreingTables  != null && apiEntry.foreingTables.length > 0  )
		{
			int 	size 		= apiEntry.foreingTables.length;
			String  update_key 	= apiEntry.update.update_key;
			
			Object object  	= paramMap.get(update_key);
			
			if ( object instanceof String )
			{
				updateID 	= (String)paramMap.get(update_key);
				
				for ( int i=0; i<size; i++ )
				{
					String foreingTable = apiEntry.foreingTables[i].tablenName;
					String foreingColmn = apiEntry.foreingTables[i].columnName;
	
					LinkedTreeMap<String,Object> 	sub_paramMap = new LinkedTreeMap<String,Object>();
	
					Set<String> paramMap_keySet = paramMap.keySet();
	
					Iterator<String> paramMap_keySet_iterator = paramMap_keySet.iterator();
	
					while ( paramMap_keySet_iterator.hasNext() )
					{
						String paramName = paramMap_keySet_iterator.next();
					
						if ( paramName.toUpperCase().startsWith( foreingTable.toUpperCase() + "." ) )
						{									
							String sub_paramName = paramName.substring( foreingTable.length() + 1 );
							
							Object obj = paramMap.get( paramName );
							
							if ( obj instanceof String )
							{
								String subValue = (String)paramMap.get( paramName );
								
								sub_paramMap.put( sub_paramName, subValue);
							}
						}
					}
	
					if ( !sub_paramMap.isEmpty()  )
					{
						if ( updateID == null )
						{
							throw new SQLException( update_key + " = null" );
						}
						
						String tableName = preparedStatmentStruct.getTableName();
						
						String query = "SELECT " + foreingColmn + " FROM " + tableName + " WHERE " + update_key + " = " + updateID;
						
						String updateID2 	= mySQL.simpleQuery(query);
						
						if ( updateID2 != null && updateID2.equals("1") )
						{//Is the default, don't edit it.
							
							String 					key2 						= foreingTable + "_" + MODE.INSERT.toString();
							PreparedStatmentStruct 	pss 						= PreparedStatmentStruct.get(key2);
							PreparedStatmentExec 	pse 						= new PreparedStatmentExec();
							
							rowsAffected += pse.execute( mySQL.connect(), sub_paramMap, pss, null );	
							
							long 					subInsertID 				= mySQL.get_mysql_insert_id();
							
							if ( subInsertID > -1 )
							{
								String command = "UPDATE " + tableName + " SET " + foreingColmn + "=" + subInsertID + " WHERE " + update_key + "=" + updateID; 
							
								rowsAffected += mySQL.executeCommand(command);
							}
						}
						else
						{
							String key2 		= foreingTable + "_" + MODE.UPDATE.toString();
		
							PreparedStatmentStruct 	pss2 = PreparedStatmentStruct.get(key2);
							PreparedStatmentExec 	pse2 = new PreparedStatmentExec();
							
							rowsAffected += pse2.execute( mySQL.connect(), sub_paramMap, pss2, updateID2 );
						}
					}
				}
			}
		}
	
		PreparedStatmentExec pse = new PreparedStatmentExec();

		/*
		if ( apiEntry != null && apiEntry.insert.generete_uuid != null )
		{
			UUID 			uuid 		= UUID.randomUUID();
			String			confUUID 	= uuid.toString();
		
			paramMap.put( apiEntry.insert.generete_uuid, confUUID );
		}*/

		if ( updateID == null )
		{
			Object valueObj = paramMap.get( apiEntry.update.update_key );
			updateID = valueObj instanceof Double ? String.valueOf( (int)( (double)valueObj ) ): (String)valueObj ;
		}
		
		return rowsAffected + pse.execute( mySQL.connect(), paramMap, preparedStatmentStruct, updateID );
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		DebugServer.println( this.getServletName() + ".doPut"  );
		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		Vector<LinkedTreeMap<String, Object>> paramMapVect;
		try
		{
			paramMapVect = getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1041, e1.getMessage() ), osw );
			DebugServer.printException( "Exception", e1  );
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1051, e1.getMessage() ), osw );
			DebugServer.printException( "Exception", e1  );
			osw.flush();
			return;
		}

		
		String key = getTableName() + "_" + MODE.UPDATE.toString();
		
		PreparedStatmentStruct preparedStatmentStruct = PreparedStatmentStruct.get(key);

		MySQL mySQL = new MySQL();
		
		try
		{			
			boolean useToken = preparedStatmentStruct.getAPIEntry().update.useToken();
			
			if ( useToken && !verifyAuthorizationKeyPut( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
			}	
			else
			{
				LinkedTreeMap<String, Object> paramMap;
				
				Vector<JSONResponse> responseVect = new Vector<JSONResponse>();
				
				for ( int i =0; i<paramMapVect.size(); i++ )
				{
					try
					{
						paramMap = paramMapVect.get( i );
						
						put_PreProcess( mySQL, paramMap, request, response );
		
						int rowsAffected = putAction( mySQL, request, response, preparedStatmentStruct, paramMap, gson, osw, 0 );
					
						Map<String, Object> resultMap = new HashMap<String, Object>();
					    
						resultMap.put( "Rows affected: ", String.valueOf( rowsAffected ) );
	
						JSONResponse responseJSON = put_PosProcess( mySQL, request, response, resultMap, paramMap );
						
						responseVect.add( responseJSON );
					}
					catch (SQLException e)
					{
						JSONResponse responseJSON = JSONResponse.not_success( 1234, e.getMessage() );
						
						DebugServer.printException( "SQLException", e  );
						
						responseVect.add( responseJSON );
					}
				}
				
				if ( responseVect.size() == 1)
				{
					gson.toJson( responseVect.get(0), osw );
				}
				else
				{
					gson.toJson( responseVect, osw );
				}
			}
		} 
		catch (SQLException e)
		{
			gson.toJson( JSONResponse.not_success( 1234, e.getMessage() ), osw );
			
			DebugServer.printException( "SQLException", e  );
		} 
		finally
		{
			mySQL.close();
		}
				
		osw.flush();
	}


	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		DebugServer.println( this.getServletName() + ".doDelete"  );
		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		String jsontxt = "";
		
        String         line;
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        while ((line = br.readLine()) != null)
        {
        	jsontxt += line;
        }

		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,Object> paramMap = (LinkedTreeMap<String, Object>)gson.fromJson( jsontxt, LinkedTreeMap.class );
		
		String key = getTableName() + "_" + MODE.DELETE.toString();
		
		PreparedStatmentStruct preparedStatmentStruct = PreparedStatmentStruct.get(key);		
		
		MySQL mySQL = new MySQL();
						
		try
		{	
			boolean useToken = preparedStatmentStruct.getAPIEntry().delete.useToken();
			
			if ( useToken && !verifyAuthorizationKeyDelete( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
			}	
			else
			{
				PreparedStatmentExec pse = new PreparedStatmentExec();
				
				int result = pse.execute( mySQL.connect(), paramMap , preparedStatmentStruct, null );			
							
				Map<String, Object> resultMap = new HashMap<String, Object>();
			    
				resultMap.put( "Rows affected ", String.valueOf( result ) );
				
				JSONResponse posP = delete_PosProcess( mySQL, request, response, resultMap, paramMap );
			
			    gson.toJson( posP, osw );			
			}
		} 
		catch (SQLException e)
		{
			gson.toJson( JSONResponse.not_success( 1062, e.getMessage() ), osw );
		
			DebugServer.printException( "SQLException", e  );
		}
		finally
		{
			mySQL.close();
		}
		
		osw.flush();
	}
	
	/**
	 * @see HttpServlet#doOptions(HttpServletRequest, HttpServletResponse)
	 */
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		DebugServer.println( this.getServletName() + ".doOptions"  );
		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		Vector<LinkedTreeMap<String, Object>> paramMapVect;
		try
		{
			paramMapVect = getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1061, e1.getMessage() ), osw );
			DebugServer.printException( "IOException", e1  );
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1071, e1.getMessage() ), osw );
			DebugServer.printException( "Exception", e1  );
			osw.flush();
			return;
		}
		
		MySQL mySQL = new MySQL();
		
		try
		{			
			String key = getTableName() + "_" + MODE.OPTIONS.toString();
			
			PreparedStatmentStruct preparedStatmentStruct = PreparedStatmentStruct.get(key);

			boolean useToken = preparedStatmentStruct.getAPIEntry().options.useToken();
			
			if ( useToken && !verifyAuthorizationKeyOptions( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
			}	
			else
			{
				Vector<JSONResponse> responseVect = new Vector<JSONResponse>();
				
				for ( int i=0; i<paramMapVect.size(); i++ )
				{
					try
					{
						LinkedTreeMap<String, Object> paramMap = paramMapVect.get(i);
						
						options_PreProcess( mySQL,  paramMap,  request,  response );
		
						Map<String, Object> resultMap = new HashMap<String, Object>();
					    			
						JSONResponse responseJSON = options_PosProcess( mySQL, request, response, resultMap, paramMap );
						
						responseVect.add(responseJSON);
					}
					catch (SQLException e)
					{
						JSONResponse responseJSON = JSONResponse.not_success( 1234, e.getMessage() );
						
						DebugServer.printException( "SQLException", e  );
						
						responseVect.add(responseJSON);
					}
				}
				
				if ( responseVect.size() == 1 )
				{
					gson.toJson( responseVect.get(0), osw );
				}
				else
				{
					gson.toJson( responseVect, osw );
				}
			}
		} 
		catch (SQLException e)
		{
			gson.toJson( JSONResponse.not_success( 1234, e.getMessage() ), osw );
			
			DebugServer.printException( "SQLException", e  );
		} 
		finally
		{
			mySQL.close();
		}
				
		osw.flush();
	}

	//GET ----------------------------------------------
	
	protected void get_PreProcess(MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Vector<Map<String,String>> resultVect)
	{
	}

	protected JSONResponse get_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, JSONResponse jsonResponse )
	{
		return jsonResponse;
	}

	// POST --------------------------------------------
	
	protected void post_VectPreProcess(MySQL mySQL, Vector<LinkedTreeMap<String, Object>> paramMapVect, HttpServletRequest request, HttpServletResponse response)
	{
	}

	protected void post_VectPosProcess(MySQL mySQL, Vector<LinkedTreeMap<String, Object>> paramMapVect, HttpServletRequest request, HttpServletResponse response, Vector<JSONResponse> responseVect)
	{
	}

	protected void post_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{
	}

	protected JSONResponse post_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		if ( resultMap != null )
		{
			return JSONResponse.success( resultMap );
		}
		else
		{
			return JSONResponse.not_success( 777, "CERO affected rows");
		}
	}
	
	//PUT ---------------------------------------------------
	
	protected void put_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{
	}
	
	protected JSONResponse put_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		if ( resultMap != null )
		{
			return JSONResponse.success( resultMap );
		}
		else
		{
			return JSONResponse.not_success( 777, "CERO affected rows");
		}
	}
	
	//OPTIONS ----------------------------------------------------
	
	protected void options_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{
	}

	protected JSONResponse options_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap ) throws SQLException
	{
		if ( resultMap != null )
		{
			return JSONResponse.success( resultMap );
		}
		else
		{
			return JSONResponse.not_success( 777, "CERO affected rows");
		}
	}

	protected JSONResponse delete_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		if ( resultMap != null )
		{
			return JSONResponse.success( resultMap );
		}
		else
		{
			return JSONResponse.not_success( 777, "CERO affected rows");
		}
	}
	
	protected String getTableName()
	{
		String typeName = this.getClass().getSimpleName();
		
		return typeName;
	}
	
}
