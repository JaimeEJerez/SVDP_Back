package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class UpcomingEvents
 */
@WebServlet("/UpcomingEvents")
public class UpcomingEvents extends UHttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static SimpleDateFormat 	sdf 		= new SimpleDateFormat( "yyyy-MM-dd");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpcomingEvents() 
    {
        super();
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    	    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		
		String actualDate = sdf.format(new Date());
					
		String query1 = "SELECT Events.*, DATE_FORMAT(CONCAT(Events.EvenDate,\" \",Events.StartTime), '%a %d %I:%i %p') AS StartDayTime, EventAdresses.* " 
						+ "FROM Events INNER JOIN EventAdresses ON ( Events.EventAdressID = EventAdresses.EventAdressID ) "
						+ "WHERE EvenDate=\"" + actualDate + "\"  limit 1";
		
		String query2 = "SELECT Events.*, DATE_FORMAT(CONCAT(Events.EvenDate,\" \",Events.StartTime), '%a %d %I:%i %p') AS StartDayTime, EventAdresses.* " 
						+ "FROM Events INNER JOIN EventAdresses ON ( Events.EventAdressID = EventAdresses.EventAdressID ) "
						+ "WHERE EvenDate>\"" + actualDate + "\" ORDER BY EvenDate limit 1";

		MySQL mySQL = new MySQL();
				
		try
		{			
			if ( !verifyAuthorizationKey( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
				
				mySQL.close();
				
				osw.flush();
				
				return;
			}	

			mySQL.executeCommand( "SET lc_time_names = 'es_ES'" );

			Vector<Map<String, String>> resultVect1 = mySQL.simpleHMapQuery(query1);
			
			if( mySQL.getLastError() != null )
			{
				gson.toJson( JSONResponse.not_success( 1221,  mySQL.getLastError() ), osw );
				
				mySQL.close();
				
				osw.flush();
				
				return;
			}

			if ( resultVect1.size() == 0 )
			{
				resultVect1.add( new HashMap<String, String>() );
			}
			else
			{
				Map<String, String> resultMap = resultVect1.get(0);
				
				String eventName 	= resultMap.get( "EventName" );
				String cl_invited 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM ClientsInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"INVITED\" OR Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String cl_acepted 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM ClientsInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String cl_scanned 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM ClientsInvitedToEvents WHERE EventName=\"" + eventName + "\" AND Status=\"REALIZED\"" );
			
				resultMap.put( "Clients.invited", cl_invited) ;
				resultMap.put( "Clients.acepted", cl_acepted) ;
				resultMap.put( "Clients.scanned", cl_scanned) ;
				
				String vl_invited 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM VoluntiersInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"INVITED\" OR Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String vl_acepted 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM VoluntiersInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String vl_scanned 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM VoluntiersInvitedToEvents WHERE EventName=\"" + eventName + "\" AND Status=\"REALIZED\"" );
			
				resultMap.put( "Volunteers.invited", vl_invited) ;
				resultMap.put( "Volunteers.acepted", vl_acepted) ;
				resultMap.put( "Volunteers.scanned", vl_scanned) ;
				
				//resultVect1.add( resultMap );
			}
			
			Vector<Map<String, String>> resultVect2 = mySQL.simpleHMapQuery(query2);
			
			if( mySQL.getLastError() != null )
			{
				gson.toJson( JSONResponse.not_success( 1231,  mySQL.getLastError() ), osw );
				
				mySQL.close();
				
				return;
			}

			if ( resultVect2.size() == 0 )
			{
				resultVect1.add( new HashMap<String, String>() );
			}
			else//VoluntiersParticipations
			{
				Map<String, String> resultMap = resultVect2.get(0);
				
				String eventName 	= resultMap.get( "EventName" );
				String cl_invited 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM ClientsInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"INVITED\" OR Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String cl_acepted 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM ClientsInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String cl_scanned 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM ClientsInvitedToEvents WHERE EventName=\"" + eventName + "\" AND Status=\"REALIZED\"" );
			
				resultMap.put( "Clients.invited", cl_invited) ;
				resultMap.put( "Clients.acepted", cl_acepted) ;
				resultMap.put( "Clients.scanned", cl_scanned) ;
				
				String vl_invited 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM VoluntiersInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"INVITED\" OR Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String vl_acepted 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM VoluntiersInvitedToEvents WHERE EventName=\"" + eventName + "\" AND ( Status=\"ACCEPTED\" OR Status=\"REALIZED\" )"  );
				String vl_scanned 		= mySQL.simpleQuery( "SELECT COUNT(*) FROM VoluntiersInvitedToEvents WHERE EventName=\"" + eventName + "\" AND Status=\"REALIZED\"" );
			
				resultMap.put( "Volunteers.invited", vl_invited) ;
				resultMap.put( "Volunteers.acepted", vl_acepted) ;
				resultMap.put( "Volunteers.scanned", vl_scanned) ;
			}
			
			resultVect1.addAll( resultVect2 );
			
			gson.toJson( JSONResponse.success( resultVect1 ), osw );
			
			response.setStatus( HttpServletResponse.SC_OK );
		}
		catch (Exception e )
		{
			response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			
			gson.toJson( JSONResponse.not_success( 1007, e.getMessage() ), osw );
			
			System.out.println( e.getMessage() );
		}
		finally
		{
			mySQL.close();
		}
					    
	    osw.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
