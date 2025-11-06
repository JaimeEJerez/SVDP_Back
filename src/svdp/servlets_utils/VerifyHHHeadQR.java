package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
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
import svdp.tcp.DebugServer;

/**
 * Servlet implementation class VerifyHHHeadQR
 */
@WebServlet("/VerifyHHHeadQR")
public class VerifyHHHeadQR extends UHttpServlet 
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
		DebugServer.println("VerifyHHHeadQR.doPost");
		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
	    
		String chapterID 			= this.getChapterID(request);

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
		
		String eventName 			= paramMap.get("EventName");
		String hauseholdhead_Token 	= paramMap.get("Hauseholdhead_Token");

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
					String where = " WHERE ";
					
					if ( chapterID != null )
					{
						where = " WHERE ChapterID=" + chapterID + " AND ";
					}
					
					String query3 = "SELECT ClientsInvitedToEventID FROM ClientsInvitedToEvents" + where + "HauseholdheadEmail=\"" + hauseholdheadEmail + "\" AND EventName=\"" + eventName + "\";";
							
					 String clientsInvitedToEventID = mySQL.simpleQuery( query3 );
					
					if ( clientsInvitedToEventID == null || clientsInvitedToEventID.isEmpty() )
					{
						DebugServer.println("The Client is not invited for event");
						
						posP = JSONResponse.not_success( 003, "The Client is not invited for this event" );
						
						String command = "INSERT IGNORE INTO ClientsNOTInvitedToEvents ( ChapterID, EventName, HauseholdheadEmail ) VALUES (\"" + chapterID + "\", \"" + eventName + "\", \"" + hauseholdheadEmail + "\");";
						
						//DebugServer.println( command );
						
						mySQL.executeCommand(command);
						
						if ( mySQL.getLastError() != null )
						{
							DebugServer.println( mySQL.getLastError() );
						}
					}
					else
					{
						DebugServer.println("The Client is invited for event");
						
		       			String command = "UPDATE ClientsInvitedToEvents SET Status =\"REALIZED\" WHERE ClientsInvitedToEventID=" + clientsInvitedToEventID;
		           		
		       			mySQL.executeCommand(command);
		       			
						Hashtable<String,String> resultMap = new Hashtable<String,String>();
						
						resultMap.put( "ClientsInvitedToEventID", clientsInvitedToEventID );

						Vector<Hashtable<String,String>> resultVect = new Vector<Hashtable<String,String>>();

						resultVect.add(resultMap);

		       			posP = JSONResponse.success( resultVect );
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
