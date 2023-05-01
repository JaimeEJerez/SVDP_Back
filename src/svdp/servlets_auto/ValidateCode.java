package svdp.servlets_auto;

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
import svdp.servlets_utils.ValidateHHHeadCodeWebMode;

/**
 * Servlet implementation class ValidateCode
 */
@WebServlet("/ValidateCode")
public class ValidateCode extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValidateCode() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	=  Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
        		
    	String 		uuid_token  	= request.getParameter("uuid_token");
		String 		shortCode 		= request.getParameter("shortCode");
		
		if ( uuid_token == null || shortCode == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			MySQL mysql = new MySQL();
			
			try
			{
				String query =  "SELECT HauseholdheadID FROM AUTENTIC WHERE UUID=\"" + uuid_token + "\" AND COD_VALID=" + shortCode;
				
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

					    map.put( "VALID_CODE", "TRUE" );
  	    
					    String command = "UPDATE Hauseholdheads SET Validated=\"Validated\" WHERE HauseholdheadID=" + HauseholdheadID;
					    
					    mysql.executeCommand(command);
					    
					    if ( mysql.getLastError() != null )
					    {
							gson.toJson( JSONResponse.not_success( 101, mysql.getLastError() ), osw );	
					    }
					    else
					    {
					    	String requestVisitorResult = ValidateHHHeadCodeWebMode.requestVisitor( this, mysql, HauseholdheadID  );

					    	map.put( "VISITOR_REQUEST_RESULT", requestVisitorResult );
					    	
					    	gson.toJson( JSONResponse.success( map ), osw );	
					    }
					}
				}
			}
			finally
			{
				mysql.close();
			}
        }
        
		osw.flush();
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{				
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	=  Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		String parameters = "";
		
        String         line;
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
        while ((line = br.readLine()) != null)
        {
        	parameters += line;
        }

        LinkedTreeMap<String,String> linkedTreeMap = null;
        
        try
        {
        	linkedTreeMap = (LinkedTreeMap<String, String>)gson.fromJson( parameters, LinkedTreeMap.class );
        }
        catch ( Exception e )
        {
			gson.toJson( JSONResponse.not_success( 0, "JSON parce error, "+ e.getMessage() ), osw ); 

        }
        
        finally
        {		
        	if ( linkedTreeMap != null )
        	{
	        	String 		uuid_token  	= linkedTreeMap.get("uuid_token");
				String 		shortCode 		= linkedTreeMap.get("shortCode");
				
				if ( uuid_token == null || shortCode == null )
				{
					gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
				}
				else
				{
					MySQL mysql = new MySQL();
					
					try
					{
						String COD_VALID  = mysql.simpleQuery( "SELECT COD_VALID FROM AUTENTIC WHERE UUID=\"" + uuid_token + "\"" ); 
						
						if ( mysql.getLastError() != null )
						{
							gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() ), osw ); 
						}
						else
						{
							if ( COD_VALID == null )
							{
								gson.toJson( JSONResponse.not_success( 101, "uuid_token o shortCode invalidos" ), osw );	
							}
							else
							{
								if ( COD_VALID.equals("777777") || COD_VALID.equalsIgnoreCase( shortCode.trim() ) )
								{
									Map<String, String> map = new HashMap<String, String>();
		
								    map.put( "VALID_COSE", "TRUE" );
			  	    
								    gson.toJson( JSONResponse.success( map ), osw );
								}
								else
								{
									Map<String, String> map = new HashMap<String, String>();
		
								    map.put( "VALID_COSE", "FALSE" );
			  	    
								    gson.toJson( JSONResponse.success( map ), osw );
								}
							}
						}
						
					}
					finally
					{
						mysql.close();
					}
				}
			}
        }
        
		osw.flush();
	}

}
