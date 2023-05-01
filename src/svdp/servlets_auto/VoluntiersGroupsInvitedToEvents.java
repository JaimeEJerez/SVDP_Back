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
import svdp.froms_manager.volunteers_invitation.Send_VolunteersInvitation_Request;

/**
 * Servlet implementation class PhisicalAddresses
 */
@WebServlet("/VoluntiersGroupsInvitedToEvents")
public class VoluntiersGroupsInvitedToEvents extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VoluntiersGroupsInvitedToEvents() 
    {
        super();
    }

    @Override
	protected void post_VectPosProcess(MySQL mySQL, Vector<LinkedTreeMap<String, Object>> paramMapVect, HttpServletRequest request, HttpServletResponse response, Vector<JSONResponse> responseVect)
	{
    	System.out.println( "VoluntiersGroupsInvitedToEvents.post_VectPosProcess" );
    	
    	if ( paramMapVect.size() > 0 )
    	{
    		LinkedTreeMap<String, Object> map = paramMapVect.get(0);
    		
    		String eventName = (String)map.get( "EventName" );
    		
    		Send_VolunteersInvitation_Request vli2er = new Send_VolunteersInvitation_Request( eventName, false );

    		try
			{
				String result = vli2er.action();
				
				Hashtable<String,String> rMap = new Hashtable<String,String>();
				
				rMap.put( "mesageToInvitedVolunteers", result );
				
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
