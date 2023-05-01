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
@WebServlet("/Voluntiers_filtered")
public class Voluntiers_filtered extends UHttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Voluntiers_filtered() 
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
		String 				name 	= request.getParameter("name");
		String 				visitor = request.getParameter("visitor");
		String 				grouped = request.getParameter("grouped");
		
		String query = "SELECT * FROM Voluntiers";
				
		String where = " WHERE VerifCode=\"VERIFD\"";
				
		if (  name != null )
		{
			where += " AND ( FirstName LIKE \"%" + name + "%\" OR LastName LIKE \"%" + name + "%\" )";
		}
		
		if (  visitor != null )
		{
			visitor = visitor.toLowerCase();
			
			if ( visitor.equals("yes") )
			{
				where += " AND Visitor=\"" + visitor + "\"";
			}
			else
			{
				where += " AND ( Visitor IS NULL OR Visitor=\"" + visitor + "\" )";
			}
		}

		if ( grouped != null )
		{
			if ( grouped.equalsIgnoreCase("yes") )
			{
				where += " AND Email IN (SELECT VoluntierEmail FROM HelpGroupsVoluntier)";
			}
			else
			{
				where += " AND Email NOT IN (SELECT VoluntierEmail FROM HelpGroupsVoluntier)";
			}
		}
		
		
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
			
			if ( where != null )
			{
				query = query + " " + where;
			}
			
			Vector<Map<String, String>> resultVectMap = mySQL.simpleHMapQuery( query );
			
			if ( mySQL.getLastError() != null )
			{
				posP = JSONResponse.not_success(002, mySQL.getLastError() + "\r\n" + query );
			}
			else
			{
				posP = JSONResponse.success( resultVectMap );
			}				
		} 
		catch (JsonIOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
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
