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
 * Servlet implementation class HelpGroupsVoluntier
 */
@WebServlet("/VoluntiersInGroup")
public class VoluntiersInGroup extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VoluntiersInGroup() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String helpGroupName = request.getParameter("HelpGroupName");
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

	    if ( helpGroupName == null )
	    {
			gson.toJson( JSONResponse.not_success( 1241, "HelpGroupName parameter is null" ), osw );
			 
			osw.flush();
			 
			return;
	    }

		String query = "SELECT VoluntierEmail FROM HelpGroupsVoluntier WHERE HelpGroupName=\"" + helpGroupName+ "\"";
		
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

	private void insert( MySQL mySQL, String helpGroupName, String[] voluntierEmailsArr )
	{
		String command2 = "INSERT INTO HelpGroupsVoluntier (HelpGroupName, VoluntierEmail) VALUES\n";
		
		for ( String voluntierMail : voluntierEmailsArr )
		{
			command2 = command2 + "	(\"" + helpGroupName.trim() + "\",\"" + voluntierMail.trim() + "\"),\n";
		}

		command2 = command2.substring(0, command2.length()-2) + ";";
		
		mySQL.executeCommand(command2);
	}
	
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
		
		String helpGroupName 	= paramMap.get("helpGroupName");
		String voluntierEmails 	= paramMap.get("voluntierEmails");
		
		if ( helpGroupName == null || voluntierEmails == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			String[] voluntierEmailsArr = voluntierEmails.split(",");
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				String query1 = "SELECT VoluntierEmail FROM HelpGroupsVoluntier WHERE HelpGroupName=\"" + helpGroupName + "\"";
				
				String[] savedVoluntierEmails = mySQL.simpleAQuery( query1 );
				
				if ( mySQL.getLastError() != null )
				{
					posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query1 );
				}
				else
				{
					String command1 = " DELETE FROM HelpGroupsVoluntier WHERE HelpGroupName=\"" + helpGroupName + "\"";
					
					mySQL.executeCommand(command1);
					
					if ( mySQL.getLastError() != null )
					{
						posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query1 );
					}
					else
					{
						insert( mySQL, helpGroupName, voluntierEmailsArr );
						
						if ( mySQL.getLastError() != null )
						{
							posP = JSONResponse.not_success( 002,  mySQL.getLastError() );
							
							insert( mySQL, helpGroupName, savedVoluntierEmails );
						}
						else
						{
							posP = JSONResponse.success( "sucsess: " + voluntierEmailsArr.length + " vulontier(S) inserted" );
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
	}

}
