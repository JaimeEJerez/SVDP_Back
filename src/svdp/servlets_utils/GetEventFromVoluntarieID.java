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

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class GetPostFromWP
 */

@WebServlet("/GetEventFromVoluntarieID")
public class GetEventFromVoluntarieID extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	String htmlText = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetEventFromVoluntarieID() 
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
			    
	    String voluntierID = request.getParameter("VoluntierID");
	    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

	    if ( voluntierID == null )
	    {
			gson.toJson( JSONResponse.not_success( 1181, "VoluntierID parameter is null" ), osw );
			 
			osw.flush();
			 
			return;
	    }

		String query = "SELECT Events.*, EventAdresses.* " 
						+ "FROM Events INNER JOIN EventAdresses ON ( Events.EventAdressID = EventAdresses.EventAdressID ) "
						+ "WHERE Events.EventID = ( SELECT EventID FROM EventVoluntiers WHERE VoluntierID = " + voluntierID + " ORDER BY EventID DESC limit 1 )";

		MySQL mySQL = new MySQL();
				
		try
		{
			Vector<Map<String, String>> resultVect = mySQL.simpleHMapQuery(query);
			
			if( mySQL.getLastError() != null )
			{
				gson.toJson( JSONResponse.not_success( 1191,  mySQL.getLastError() ), osw );
			}
			else
			{
				gson.toJson( JSONResponse.success( resultVect ), osw );
			}
			
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
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
