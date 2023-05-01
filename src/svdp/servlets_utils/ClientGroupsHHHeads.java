package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.Util;

/**
 * Servlet implementation class ClientGroupsHHHeads
 */
@WebServlet("/ClientGroupsHHHeads")
public class ClientGroupsHHHeads extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientGroupsHHHeads() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String clientGroupName = request.getParameter("ClientGroupName").trim();
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

	    if ( clientGroupName == null )
	    {
			gson.toJson( JSONResponse.not_success( 1171, "ClientGroupName parameter is null" ), osw );
			 
			osw.flush();
			 
			return;
	    }

		String query = "SELECT HauseholdheadEmail FROM ClientGroupsHHHeads WHERE ClientGroupName=\"" + clientGroupName+ "\"";
		
		MySQL mySQL = new MySQL();
		
		JSONResponse posP;
		
		try
		{
			Vector<String> resultVect = mySQL.simpleVQuery(query);
			
			if ( mySQL.getLastError() != null )
			{
				posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query );
			}
			else
			{
				posP = JSONResponse.success( resultVect );
			}
		}
		finally
		{
			mySQL.close();
		}
		
		response.setStatus( HttpServletResponse.SC_OK );
		
	    gson.toJson( posP, osw );
	    
	    osw.flush();
	}

	/*
	private int insert( MySQL mySQL, String clientGroupName, String[] hauseholdheadEmailArr )
	{
		String command2 = "INSERT INTO ClientGroupsHHHeads (ClientGroupName, HauseholdheadEmail) VALUES\n";
		
		for ( String hauseholdheadEmail : hauseholdheadEmailArr )
		{
			command2 = command2 + "	(\"" + clientGroupName.trim() + "\",\"" + hauseholdheadEmail.trim() + "\"),\n";
		}

		command2 = command2.substring(0, command2.length()-2) + ";";
		
		return mySQL.executeCommand(command2);
	}*/
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson, true );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String clientGroupName 		= paramMap.get("clientGroupName");
		String hhhEmails 			= paramMap.get("hauseholdheadEmails");
		
		if ( clientGroupName == null || hhhEmails == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			clientGroupName 				= clientGroupName.trim();
			String[] hauseholdheadEmails 	= hhhEmails.split(",");
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP = null;
			
			try
			{
				posP = checkIfHHHExisst( mySQL, hauseholdheadEmails );
				
				if ( posP == null )
				{
					int afecctedRows = 0;
					
					for ( String hhhMail : hauseholdheadEmails )
					{
						String query1 = "SELECT COUNT(*) FROM ClientGroupsHHHeads WHERE "
										+ "ClientGroupName=\""    + clientGroupName + "\" AND "
										+ "HauseholdheadEmail=\"" + hhhMail + "\"";
						
						String count = mySQL.simpleQuery( query1 );
						
						if ( mySQL.getLastError() != null )
						{
							posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query1 );
						}
						else
						{
							int number = Integer.valueOf( count );
							
							if ( number == 0 )
							{
								String command2 = "INSERT INTO ClientGroupsHHHeads \n"
													+ "(ClientGroupName, HauseholdheadEmail) \n"
													+ "VALUES\n"
													+ "	(\"" + clientGroupName + "\",\"" + hhhMail + "\")";
						
								
								mySQL.executeCommand(command2);
								
								afecctedRows++;
							}
								
							if ( mySQL.getLastError() != null )
							{
								posP = JSONResponse.not_success( 002,  mySQL.getLastError() );
							}
						}
					}
					
					posP = JSONResponse.success( "sucsess: " + afecctedRows + " row(s) affected." );
				}
			}
			finally
			{
				mySQL.close();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}

	/*
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String clientGroupName 		= paramMap.get("clientGroupName");
		String hauseholdheadEmails 	= paramMap.get("hauseholdheadEmails");
		
		
		
		if ( clientGroupName == null || hauseholdheadEmails == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			clientGroupName = clientGroupName.trim();
		
			String[] hauseholdheadEmailArr = hauseholdheadEmails.split(",");
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP = null;
			
			try
			{
				posP = checkIfHHHExisst( mySQL, hauseholdheadEmailArr );
				 
				if ( posP == null )
				{
					String query1 = "SELECT HauseholdheadEmail FROM ClientGroupsHHHeads WHERE ClientGroupName=\"" + clientGroupName + "\"";
					
					String[] savedHauseholdheadEmails = mySQL.simpleAQuery( query1 );
					
					if ( mySQL.getLastError() != null )
					{
						posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query1 );
					}
					else
					{
						String command1 = " DELETE FROM ClientGroupsHHHeads WHERE ClientGroupName=\"" + clientGroupName.trim() + "\"";
						
						mySQL.executeCommand(command1);
						
						if ( mySQL.getLastError() != null )
						{
							posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query1 );
						}
						else
						{
							int affectedRows = insert( mySQL, clientGroupName, hauseholdheadEmailArr );
							
							if ( mySQL.getLastError() != null )
							{
								posP = JSONResponse.not_success( 002,  mySQL.getLastError() );
								
								insert( mySQL, clientGroupName, savedHauseholdheadEmails );
							}
							else
							{
								posP = JSONResponse.success( "sucsess: " + affectedRows + " affected rows." );
							}
						}
					}
				}
			}
			finally
			{
				mySQL.close();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}*/
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson, true );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String clientGroupName 		= paramMap.get("clientGroupName");
		String hhhEmails 			= paramMap.get("hauseholdheadEmail");
		
		if ( clientGroupName == null || hhhEmails == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			clientGroupName 				= clientGroupName.trim();
			String[] hauseholdheadEmails 	= hhhEmails.split(",");
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP = null;
			
			try
			{
				posP = checkIfHHHExisst( mySQL, hauseholdheadEmails );
				
				if ( posP == null )
				{
					int afecctedRows = 0;
					
					for ( String hhhMail : hauseholdheadEmails )
					{
						String query1 = "SELECT COUNT(*) FROM ClientGroupsHHHeads WHERE "
										+ "ClientGroupName=\""    + clientGroupName + "\" AND "
										+ "HauseholdheadEmail=\"" + hhhMail + "\"";
						
						String count = mySQL.simpleQuery( query1 );
						
						if ( mySQL.getLastError() != null )
						{
							posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query1 );
						}
						else
						{
							int number = Integer.valueOf( count );
							
							if ( number == 0 )
							{
								String command2 = "INSERT INTO ClientGroupsHHHeads \n"
													+ "(ClientGroupName, HauseholdheadEmail) \n"
													+ "VALUES\n"
													+ "	(\"" + clientGroupName + "\",\"" + hhhMail + "\")";
						
								
								mySQL.executeCommand(command2);
								
								afecctedRows++;
							}
								
							if ( mySQL.getLastError() != null )
							{
								posP = JSONResponse.not_success( 002,  mySQL.getLastError() );
							}
						}
					}
					
					posP = JSONResponse.success( "sucsess: " + afecctedRows + " row(s) affected." );
				}
			}
			finally
			{
				mySQL.close();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}

	private JSONResponse checkIfHHHExisst( MySQL mySQL, String[] hauseholdheadEmails )
	{
		JSONResponse posP = null;
		
		for ( String hhhMail : hauseholdheadEmails )
		{
			String id = mySQL.simpleQuery( "SELECT HauseholdheadID FROM Hauseholdheads WHERE Email=\"" + hhhMail.trim() + "\"" );
		
			if ( id == null )
			{
				posP = JSONResponse.not_success( 002, "There is no user with the mail '" + hhhMail + "'"  );
				
				break;
			}
		}
		
		return posP;

	}
	
}
