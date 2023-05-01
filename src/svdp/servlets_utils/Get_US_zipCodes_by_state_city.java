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

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class GetUSZipCodesBySateCity
 */
@WebServlet("/Get_US_zipCodes_by_state_city")
public class Get_US_zipCodes_by_state_city extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Get_US_zipCodes_by_state_city() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

		String state 	= request.getParameter("state");
		String city 	= request.getParameter("city");
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		//String query = "SELECT CITY FROM zip_codes WHERE STATE LIKE \"" + state.trim() + "%\" GROUP BY CITY ORDER BY CITY;";

		String query = "SELECT ZIP_CODE FROM zip_codes WHERE ACTIVE=1 AND STATE = \"" + state.trim() + "\" AND CITY = \"" + city.trim() + "\" ORDER BY ZIP_CODE";
		
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
