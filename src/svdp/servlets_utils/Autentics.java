package svdp.servlets_utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.google.gson.internal.LinkedTreeMap;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class Autentics
 */
@WebServlet("/Autentics")
public class Autentics extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Autentics() 
    {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.getWriter().append("Not superted");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		LinkedTreeMap<String, String> paramMap = null;
		try
		{
			paramMap = getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1101, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1111, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}

		String shortCode 	= paramMap.get( "VALID_COD" );
		String uuid_token	= paramMap.get( "UUID");
		
		if ( shortCode == null || uuid_token == null )
		{
			gson.toJson( JSONResponse.not_success( 1121, "Error: parametros incompletos." ), osw );
			osw.flush();
			return;
		}
		
		MySQL mysql = new MySQL();
		
		try
		{
			String query =  "SELECT HauseholdheadID FROM AUTENTICS WHERE UUID=\"" + uuid_token + "\" AND VALID_COD=" + shortCode;
			
			String HauseholdheadID  = mysql.simpleQuery( query ); 

			if ( mysql.getLastError() != null )
			{
				gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() ), osw ); 
			}
			else
			{					
				if ( HauseholdheadID == null )
				{
					gson.toJson( JSONResponse.not_success( 101, "uuid_token o shortCode invalidos" ), osw );	
				}
				else
				{
					Map<String, String> map = new HashMap<String, String>();
	
				    map.put( "VALID_COD", "TRUE" );
	  
				    String command = "UPDATE Hauseholdheads SET Validated=\"Validated\" WHERE HauseholdheadID=" + HauseholdheadID;
				    
				    mysql.executeCommand(command);
				    
				    if ( mysql.getLastError() != null )
				    {
						gson.toJson( JSONResponse.not_success( 101, mysql.getLastError() ), osw );	
				    }
				    else
				    {
				    	/*
				    	if ( Globals.enableVisitorAssignmentMechanism )
				    	{
				    		String result = ValidateHHHeadCodeWebMode.requestVisitor( this, mysql, HauseholdheadID );
				    	
				    		map.put("requestVisitor", result);
				    	}
				    	*/
				    	
				    	gson.toJson( JSONResponse.success( map ), osw );
				    }
				}
			}
		}
		finally
		{
			mysql.close();
		}
	
		osw.flush();
	}
	
	private LinkedTreeMap<String,String>  getParamMap( HttpServletRequest request, Gson gson ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	int comment = line.lastIndexOf("//");
        	
        	if ( comment >= 0 )
        	{
        		line = line.substring( 0, comment);
        	}
        	
        	jsontxt += line;
        }

		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,String> paramMap = (LinkedTreeMap<String, String>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

}
