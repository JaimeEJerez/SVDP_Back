package svdp.servlets_utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

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
import svdp.tcp.DebugServer;

/**
 * Servlet implementation class GetClientsGroupsInvitedToEvents
 */
@WebServlet("/GetClientsGroupsInvitedToEvents")
public class GetClientsGroupsInvitedToEvents extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetClientsGroupsInvitedToEvents() 
    {
        super();
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    response.setContentType("application/json");
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
			gson.toJson( JSONResponse.not_success( 1201, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1211, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}

	    String eventName = paramMap.get("EventName");
	    
	    if ( eventName == null )
	    {
			gson.toJson( JSONResponse.not_success( 1181, "EventName parameter is null" ), osw );
			 
			osw.flush();
			 
			return;
	    }

		String query0 = "SELECT COUNT(*) FROM Events WHERE EventName=\"" + eventName + "\";";

		MySQL mySQL = new MySQL();
				
		try
		{
			String result0 = mySQL.simpleQuery(query0);
			
			boolean exist = Integer.valueOf( result0 ) != 0;
			
			DebugServer.println( query0 + " - " + result0 );
			
			if ( !exist )
			{
				gson.toJson( JSONResponse.not_success( 1307, "Event " + eventName + " not found."  ), osw );
			}
			else
			{
				Hashtable<String,String> resultmap = DoGetClientsGroupsInvitedToEvents( eventName );
				
				gson.toJson( JSONResponse.success( resultmap ), osw );
				
				response.setStatus( HttpServletResponse.SC_OK );
			}
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

	public static Hashtable<String,String> DoGetClientsGroupsInvitedToEvents( String eventName )
	{		
		String query1 = "SELECT ClientGroupName FROM ClientGroups ORDER BY ClientGroupName;";
		String query2 = "SELECT ClientGroupName FROM ClientsGroupsInvitedToEvents WHERE EventName=\"" + eventName + "\"" ;
	
		MySQL mySQL = new MySQL();
						
		Vector<String> allGroups = mySQL.simpleVQuery(query1);
		Vector<String> invitedGr = mySQL.simpleVQuery(query2);
		
		Hashtable<String,String> resultmap = new Hashtable<String,String>();
		
		for ( String helpGroupName : allGroups )
		{
			boolean isInvited = invitedGr.contains( helpGroupName );
			
			resultmap.put( helpGroupName, isInvited ? "yes" : "no" );
		}			
		
		return resultmap;
	}
}
