package svdp.servlets_auto;

import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.APIEntryPoint;
import fast_track.JSONResponse;
import fast_track.MySQL;

/**
 * Servlet implementation class ClientsInvitedToEvents
 */
@WebServlet("/ClientsGroupsInvitedToEvents")
public class ClientsGroupsInvitedToEvents extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientsGroupsInvitedToEvents() 
    {
        super();
    }

    @Override
	protected JSONResponse post_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{ 
    	JSONResponse jSONResponse = null;
    	
    	System.out.println( "ClientsGroupsInvitedToEvents.post_VectPosProcess" );
    	
		String eventName 		= (String)paramMap.get( "EventName" );
		String clientGroupName 	= (String)paramMap.get( "ClientGroupName" );
		
		try
		{
			String query1 = "SELECT HauseholdheadEmail FROM ClientGroupHHHeads WHERE ClientGroupName=\"" + clientGroupName + "\";";
			
			String[] hauseholdheadEmails = mySQL.simpleAQuery(query1);
			
			if ( hauseholdheadEmails != null )
			{
				for( String hauseholdheadEmail : hauseholdheadEmails )
				{
					String query2 = "INSERT INTO ClientsInvitedToEvents ( HauseholdheadEmail, EventName ) VALUES( \"" + hauseholdheadEmail + "\",\"" + eventName + "\");";
				
					mySQL.executeCommand( query2 );
				}
			}
			
			jSONResponse = JSONResponse.success( "Registered " + hauseholdheadEmails.length + " Client(s) in ClientsInvitedToEvents"  );			
		} 
		catch (Exception e)
		{
			jSONResponse = JSONResponse.not_success( 779, e.getMessage() );
			
			e.printStackTrace();
		}
		
		return jSONResponse;
	}

}
