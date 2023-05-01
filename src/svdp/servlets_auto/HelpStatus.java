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
import svdp.general.Globals;
import svdp.servlets_utils.ValidateHHHeadCodeWebMode;
import svdp.tcp.DebugServer;

/**
 * Servlet implementation class HelpStatus
 */
@WebServlet("/HelpStatus")
public class HelpStatus extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelpStatus() 
    {
        super();
    }
    
	protected JSONResponse post_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		/*
		DebugServer.println( "Events.post_PosProcess()" );
		
		if ( resultMap.containsKey( "insertedID" ) )
		{
	    	if ( Globals.enableVisitorAssignmentMechanism )
	    	{
	    		String HauseholdheadID = "";
	    		
	    		String result = ValidateHHHeadCodeWebMode.requestVisitor( this, mySQL, HauseholdheadID );
	    	
	    		//map.put("requestVisitor", result);
	    	}

		}*/
		
		return super.post_PosProcess(mySQL, request, response, resultMap, paramMap);
	}

}
