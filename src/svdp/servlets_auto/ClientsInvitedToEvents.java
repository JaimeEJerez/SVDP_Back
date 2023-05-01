package svdp.servlets_auto;

import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.APIEntryPoint;
import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.froms_manager.client_invitation.Send_ClientsInvitation_Request;

/**
 * Servlet implementation class ClientsInvitedToEvents
 */
@WebServlet("/ClientsInvitedToEvents")
public class ClientsInvitedToEvents extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientsInvitedToEvents() 
    {
        super();
    }

    @Override
	protected void post_VectPosProcess(MySQL mySQL, Vector<LinkedTreeMap<String, Object>> paramMapVect, HttpServletRequest request, HttpServletResponse response, Vector<JSONResponse> responseVect)
	{
    	System.out.println( "ClientsInvitedToEvents.post_VectPosProcess" );
    	
    	if ( paramMapVect.size() > 0 )
    	{
    		LinkedTreeMap<String, Object> map =paramMapVect.get(0);
    		
    		String eventName = (String)map.get( "EventName" );
    		
    		Send_ClientsInvitation_Request sci2er = new Send_ClientsInvitation_Request( eventName, false );

    		try
			{
				String result = sci2er.action();
				
				Hashtable<String,String> rMap = new Hashtable<String,String>();
				
				rMap.put( "mesageToInvitedClients", result );
				
				responseVect.add( JSONResponse.success( rMap ) );
			} 
    		catch (Exception e)
			{
    			responseVect.add( JSONResponse.not_success( 779, e.getMessage() ) );
    			
				e.printStackTrace();
			}
    		
    	}
	}
   	

}
