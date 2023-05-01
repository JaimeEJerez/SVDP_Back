package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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
 * Servlet implementation class Statistics
 */
@WebServlet("/Statistics")
public class Statistics extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Statistics() 
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

		String query1 = "SELECT COUNT(*) FROM Hauseholdheads WHERE Validated IS NOT NULL;";
		String query2 = "SELECT COUNT(*) FROM Hauseholdheads WHERE Validated IS NOT NULL AND HelpApproveDate IS NULL;";
		String query3 = "SELECT COUNT(*) FROM Hauseholdheads WHERE Validated IS NOT NULL AND HelpApproveDate IS NOT NULL;";
		String query4 = "SELECT COUNT(*) FROM Voluntiers WHERE VerifCode=\"VERIFD\";";
		String query5 = "SELECT COUNT(*) FROM HelpGroups";
		String query6 = "SELECT COUNT(*) FROM ClientGroups";

		MySQL mySQL = new MySQL();
				
		try
		{
			String countHauseholdheads 			= mySQL.simpleQuery( query1 );
			String countPendingClients 			= mySQL.simpleQuery( query2 );
			String countsAprovedClients			= mySQL.simpleQuery( query3 );
			String countActiveVoluntiers		= mySQL.simpleQuery( query4 );
			String countVoluntiersGroups		= mySQL.simpleQuery( query5 );
			String countClientGroups			= mySQL.simpleQuery( query6 );

			Map<String, String> map = new HashMap<String, String>();

			map.put("countRegisteredClients", countHauseholdheads);
			map.put("countPendingClients", countPendingClients);
			map.put("countsAprovedClients", countsAprovedClients);			
			map.put("countActiveVoluntiers", countActiveVoluntiers);
			map.put("countVoluntiersGroups", countVoluntiersGroups);
			map.put("countClientGroups", countClientGroups);
			
			gson.toJson( JSONResponse.success( map ), osw );
			
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
