package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
 * Servlet implementation class FixTokens
 */
@WebServlet("/FixTokens")
public class FixTokens extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FixTokens() 
    {
        super();
    }

    protected int fixTokenInTable( MySQL mySQL, String tableName, String idName ) throws ServletException
    {
    	int fixedRows = 0;
    	
    	String query = "SELECT " + idName + " FROM " + tableName + " WHERE Token is NULL ";
    	
    	String[] IDs = mySQL.simpleAQuery(query);
    	
    	if ( mySQL.getLastError() != null )
    	{
    		throw new ServletException( " SQL Error:" + mySQL.getLastError() + "\r\n" + query );
    	}
    	
    	for ( String id : IDs )
    	{
    		UUID 			uuid 		= UUID.randomUUID();
			
    		String command = "UPDATE " + tableName + " SET Token=\"" + uuid + "\" WHERE " + idName + " = " + id;
    		
    		fixedRows = mySQL.executeCommand( command );
    		
    		if ( mySQL.getLastError() != null )
        	{
        		throw new ServletException( " SQL Error:" + mySQL.getLastError() + "\r\n" + query );
        	}
    	}   
    	
    	return fixedRows;
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
		
		MySQL mySQL = new MySQL();
		
		JSONResponse posP;
		
		try
		{
			 Map<String, String> resultMap = new HashMap<String, String>();
			 
			 int n1 = fixTokenInTable( mySQL, "ChapterMembers", "ChapterMemberID" );
			 resultMap.put("ChapterMembers", "fixed tokens->" + n1 );
			 
			 int n2 = fixTokenInTable( mySQL, "Voluntiers", "VoluntierID" );
			 resultMap.put("Voluntiers", "fixed tokens->" + n2 );
			 
			 int n3 = fixTokenInTable( mySQL, "Hauseholdheads", "HauseholdheadID" );
			 resultMap.put("Hauseholdheads", "fixed tokens->" + n3 );
							
			 posP = JSONResponse.success( resultMap );
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
