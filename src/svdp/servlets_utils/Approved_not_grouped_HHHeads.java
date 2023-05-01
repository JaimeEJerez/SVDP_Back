package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class Approved_not_grouped_HHHeads
 */
@WebServlet("/Approved_not_grouped_HHHeads")
public class Approved_not_grouped_HHHeads extends UHttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Approved_not_grouped_HHHeads() 
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

		String query1 = "SELECT * FROM Hauseholdheads WHERE HelpApproveDate IS NOT NULL AND Email NOT IN (SELECT HauseholdheadEmail FROM ClientGroupsHHHeads);";
				
		MySQL mySQL = new MySQL();
		
		JSONResponse posP = null;
		
		try
		{
			if ( !verifyAuthorizationKey( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
				
				mySQL.close();
				
				osw.flush();
				
				return;
			}	

			Vector<Map<String, String>> resultVectMap = mySQL.simpleHMapQuery(query1);
			
			if ( mySQL.getLastError() != null )
			{
				posP = JSONResponse.not_success(002, mySQL.getLastError() + "\r\n" + query1 );
			}
			else
			{
				posP = JSONResponse.success( resultVectMap );
			}				
		} 
		catch (JsonIOException e)
		{
			posP = JSONResponse.not_success( 002, e.getMessage() );
			e.printStackTrace();
		} 
		catch (SQLException e)
		{
			posP = JSONResponse.not_success( 002, e.getMessage() );
			e.printStackTrace();
		}
		finally
		{
			mySQL.close();
		}
		
		response.setStatus( HttpServletResponse.SC_OK );
		
	    gson.toJson( posP, osw );
		
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
