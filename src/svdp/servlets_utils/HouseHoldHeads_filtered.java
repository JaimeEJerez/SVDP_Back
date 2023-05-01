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
@WebServlet("/HouseHoldHeads_filtered")
public class HouseHoldHeads_filtered extends UHttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HouseHoldHeads_filtered() 
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

		String dependentSonsNumbr 	= request.getParameter("dependentSonsNumbr");
		String approved_help 		= request.getParameter("approvedHelp");
		String name 				= request.getParameter("name");
		String grouped				= request.getParameter("grouped");

		String query1 = "SELECT Hauseholdheads.*, PhisicalAddresses.* FROM Hauseholdheads INNER JOIN PhisicalAddresses ON Hauseholdheads.PhisicalAddressID=PhisicalAddresses.PhisicalAddressID ";
				
		String where = "  WHERE Validated IS NOT NULL";
		
		if ( approved_help != null && approved_help.equalsIgnoreCase("yes"))
		{
			where += " AND HelpApproveDate IS NOT NULL";
		}
		
		if ( approved_help != null && approved_help.equalsIgnoreCase("no"))
		{
			where += " AND HelpApproveDate IS NULL";
		}
		
		if ( dependentSonsNumbr != null )
		{
			where += " AND dependentSonsNumbr=" + dependentSonsNumbr;
		}
		
		if (  name != null )
		{
			where += " AND Name LIKE \"%" + name + "%\"";
		}
		
		if ( grouped != null )
		{
			if ( grouped.equalsIgnoreCase("yes" ) )
			{
				where += " AND Email IN (SELECT HauseholdheadEmail FROM ClientGroupsHHHeads);";

			}
			else
			{
				where += " AND Email NOT IN (SELECT HauseholdheadEmail FROM ClientGroupsHHHeads);";
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
				query1 = query1 + " " + where;
			}
			
			Vector<Map<String, String>> resultVectMap = mySQL.simpleHMapQuery( query1 );
			
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
