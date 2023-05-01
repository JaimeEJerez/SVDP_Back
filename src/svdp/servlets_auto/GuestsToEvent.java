package svdp.servlets_auto;


import java.util.Vector;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.APIEntryPoint;
import fast_track.MySQL;

/**
 * Servlet implementation class GuestsToEvent
 * 
 */
@WebServlet("/GuestsToEvent")
public class GuestsToEvent extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GuestsToEvent() 
    {
        super();
    }
  
    @Override
    protected void post_VectPreProcess(MySQL mySQL, Vector<LinkedTreeMap<String, Object>> paramMapVect, HttpServletRequest request, HttpServletResponse response)
	{
    	boolean sameEventName = true;
    	
    	String eventName0 = (String)paramMapVect.get(0).get("EventName");
    	
    	if ( eventName0 != null )
    	{
    		for ( int i=1; i<paramMapVect.size(); i++ )
    		{
    			String eventName1 = (String)paramMapVect.get(i).get("EventName");
    			
    			if ( eventName1 != null && !eventName1.equalsIgnoreCase(eventName0) )
    			{
    				sameEventName = false;
    				break;
    			}
    		}
    		
    		if ( sameEventName )
    		{
    			String command = "DELETE FROM GuestsToEvent WHERE EventName=\"" + eventName0 + "\"";
    			
    			mySQL.executeCommand( command );
    		}
    	}
	}


}
