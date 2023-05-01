package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
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
 * Servlet implementation class VerifyHHHeadQR
 */
@WebServlet("/VerifyHHHeadQR")
public class VerifyHHHeadQR extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VerifyHHHeadQR() 
    {
        super();
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
		
		String eventName 			= paramMap.get("EventName").trim();
		String hauseholdhead_Token 	= paramMap.get("Hauseholdhead_Token").trim();
		
		if ( eventName == null || hauseholdhead_Token == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				String query1 = "SELECT Email FROM Hauseholdheads WHERE Token=\"" + hauseholdhead_Token + "\"";

				String hauseholdheadEmail = mySQL.simpleQuery(query1);

				if ( hauseholdheadEmail == null )
				{
					posP = JSONResponse.not_success( 1003, "Hauseholdhead not found." );
				}
				else
				{
					String query2 = "SELECT ClientGroupName FROM Events WHERE EventName=\"" + eventName + "\"";
					
					String clientGroupName = mySQL.simpleQuery( query2 );
					
					if ( clientGroupName == null )
					{
						posP = JSONResponse.not_success( 003, "Event not found or not ClientGroupName assigned." );
					}
					else
					{
						String query3 = "SELECT * FROM ClientsInvitedToEvents WHERE HauseholdheadEmail=\"" + hauseholdheadEmail + "\" AND EventName=\"" + eventName + "\" ORDER BY InvitationID DESC";
								
						 Vector<Map<String, String>> resultMap = mySQL.simpleHMapQuery( query3 );
						
						if ( resultMap.isEmpty() )
						{
							posP = JSONResponse.not_success( 003, "The Client is not invited for this event" );
						}
						else
						{
							posP = JSONResponse.success( resultMap );
							
			       			String command = "INSERT INTO ClientsInvitedToEvents ( EventName, HauseholdheadEmail, Status ) VALUES (\"" + eventName + "\",\"" + hauseholdheadEmail + "\",\"REALIZED\")";
			           		
			       			mySQL.executeCommand(command);

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
