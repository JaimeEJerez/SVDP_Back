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
 * Servlet implementation class GetVoluntierEvents
 */
@WebServlet("/GetVoluntierEvents")
public class GetVoluntierEvents extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetVoluntierEvents() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String voluntierEMail = request.getParameter("voluntierEMail");
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		String query1 = "SELECT HelpGroups.HelpGroupName\r\n"
						+ "FROM HelpGroups\r\n"
						+ "INNER JOIN HelpGroupsVoluntier ON HelpGroupsVoluntier.HelpGroupName=HelpGroups.HelpGroupName \r\n"
						+ "WHERE HelpGroupsVoluntier.VoluntierEmail=\"" + voluntierEMail + "\"";
		
		//String query1 = "SELECT HelpGroupID FROM HelpGroupsVoluntier WHERE VoluntierEmail=\"" + voluntierEMail + "\";";
		
		MySQL mySQL = new MySQL();
		
		JSONResponse posP;
		
		try
		{
			Vector<String> resultVect = mySQL.simpleVQuery( query1 );
			
			if ( mySQL.getLastError() != null )
			{
				posP = JSONResponse.not_success(002, mySQL.getLastError() + "\r\n" + query1 );
			}
			else
			{
				if ( resultVect.size() == 0 )
				{
					posP = JSONResponse.not_success(002, "Not found any volunteer with this mail:\"" + voluntierEMail + "\"" );
				}
				else
				{
					String helpGroupNames;
					
					helpGroupNames = "HelpGroupName=\"" + resultVect.get(0) + "\"";
					
					for ( int i=1; i<resultVect.size(); i++ )
					{
						helpGroupNames = helpGroupNames + " AND HelpGroupName=\"" + resultVect.get(i) + "\"";
					}
					
					mySQL.executeCommand( "SET lc_time_names = 'es_ES'" );
					
					String query2 = "SELECT DATE_FORMAT(CONCAT(Events.EvenDate,\" \",Events.StartTime), '%a %d %I:%i %p') AS StartDayTime , "
									+ "Events.*, EventAdresses.* "
									+ "FROM Events INNER JOIN EventAdresses "
									+ "ON EventAdresses.EventAdressID=Events.EventAdressID "
									+ "WHERE EvenDate>=CURDATE()  AND " + helpGroupNames 
									+ " ORDER BY EvenDate;";
					
					Vector<Map<String, String>> resultVectMap = mySQL.simpleHMapQuery(query2);
					
					if ( mySQL.getLastError() != null )
					{
						posP = JSONResponse.not_success(002, mySQL.getLastError() + "\r\n" + query2 );
					}
					else
					{
						posP = JSONResponse.success( resultVectMap );
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
		
		osw.flush();
	}

}
