package svdp.servlets_auto;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.APIEntryPoint;
import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.froms_manager.client_invitation.Send_ClientsCancelation;
import svdp.froms_manager.client_invitation.Send_ClientsInvitation_Request;
import svdp.froms_manager.volunteers_invitation.Send_VolunteersCancelation;
import svdp.froms_manager.volunteers_invitation.Send_VolunteersInvitation_Request;
import svdp.general.Util;
import svdp.general.Util.EventInfo;
import svdp.servlets_utils.GetClientsGroupsInvitedToEvents;
import svdp.servlets_utils.GetVoluntiersGroupsInvitedToEvents;
import svdp.tcp.DebugServer;

/**
 * Servlet implementation class Events
 */
@WebServlet("/Events")
public class Events extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       	
	String				eventName			= null;
	ArrayList<String> 	clientGroupNames 	= null;
	ArrayList<String> 	helpGroupNames 		= null;

	Hashtable<String,String> 	old_vgie = null;
	Hashtable<String,String> 	old_cgie = null;
	EventInfo 					old_einf = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Events() 
    {
        super();
    }
    
	@SuppressWarnings("unchecked")
	protected void post_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{
		DebugServer.println( "Events.post_PreProcess() A" );
		
		clientGroupNames 	= (ArrayList<String>)paramMap.get( "ClientGroupName");
		helpGroupNames 		= (ArrayList<String>)paramMap.get( "HelpGroupName");
		eventName			= (String)paramMap.get( "EventName");
				
		DebugServer.println( "ClientGroupNames:" + clientGroupNames );
		DebugServer.println( "HelpGroupNames  :" + helpGroupNames   );
		
		paramMap.remove( "ClientGroupName" );
		paramMap.remove( "HelpGroupName" );
		
		DebugServer.println( "Events.post_PreProcess() B" );
	}

	private static void processClientGroups( MySQL 				mySQL, 
											String 				eventName, 
											ArrayList<String> 	clientGroupNames, 
											Map<String, Object> resultMap,
											boolean				eventUpdate )
	{
		DebugServer.println( "processClientGroups" );
		
		eventName = eventName.trim();
		
		for ( String clientGName : clientGroupNames )
		{
			clientGName = clientGName.trim();
			
			DebugServer.println( "processClientGroup( " + clientGName + " )" );
			
			String query = "SELECT HauseholdheadEmail FROM ClientGroupHHHeads WHERE ClientGroupName=\"" + clientGName + "\";";
			
			String[] HauseholdheadEmails = mySQL.simpleAQuery(query);

			DebugServer.println( "Insert ClientsInvitedToEvents ( " + HauseholdheadEmails.length + " )" );
			
			for ( String HauseholdheadEmail : HauseholdheadEmails )
			{
				String command = "INSERT IGNORE  INTO ClientsInvitedToEvents ( EventName, HauseholdheadEmail ) VALUES (\"" + eventName + "\",\"" + HauseholdheadEmail + "\" )";

				mySQL.executeCommand( command );
				
				if ( mySQL.getLastError() != null )
				{
					DebugServer.println( mySQL.getLastError() + " " + command );
				}
				else
				{
				}
			}
			
			String command = "INSERT IGNORE INTO ClientsGroupsInvitedToEvents ( ClientGroupName, EventName ) VALUES (\"" + clientGName + "\",\"" + eventName + "\" )";
			
			DebugServer.println( "Insert ClientsGroupsInvitedToEvents ( " + clientGName + " )" );
			
			mySQL.executeCommand( command );
			
			if ( mySQL.getLastError() != null )
			{
				DebugServer.println( mySQL.getLastError() + " " + command );
			}
			else
			{
				DebugServer.println( clientGName + " invited to " + eventName );
			}
		}
		
		Send_ClientsInvitation_Request sci2er = new Send_ClientsInvitation_Request( eventName, eventUpdate );

		try
		{
			String result = sci2er.action();
						
			resultMap.put( "ClientsInvitattionToEvent",result );
		} 
		catch (Exception e)
		{
			resultMap.put( "ClientsInvitattionToEvent",e.getMessage() );
			
			DebugServer.printException( "ClientsInvitattionToEvent", e );
		}

	}
	
	private static void processVolunteerGroups( MySQL 				mySQL, 
												String 				eventName, 
												ArrayList<String> 	helpGroupNames, 
												Map<String, Object> resultMap, 
												boolean 			update )
	{
		for ( String helpGName : helpGroupNames )
		{
			helpGName = helpGName.trim();
			
			String query = "SELECT VoluntierEmail FROM HelpGroupsVoluntier WHERE HelpGroupName=\"" + helpGName + "\";";
			
			String[] VoluntierEmails = mySQL.simpleAQuery(query);

			for ( String VoluntierEmail : VoluntierEmails )
			{
				String command = "INSERT IGNORE INTO VoluntiersInvitedToEvents ( EventName, VoluntierEmail ) VALUES (\"" + eventName + "\",\"" + VoluntierEmail + "\" )";

				mySQL.executeCommand( command );
				
				if ( mySQL.getLastError() != null )
				{
					DebugServer.println( mySQL.getLastError() + " " + command );
				}
				else
				{
				}
			}
			
			String command = "INSERT IGNORE INTO VoluntiersGroupsInvitedToEvents ( HelpGroupName, EventName ) VALUES (\"" + helpGName + "\",\"" + eventName + "\" )";
			
			mySQL.executeCommand( command );
			
			if ( mySQL.getLastError() != null )
			{
				DebugServer.println( mySQL.getLastError() + " " + command );
			}
			else
			{
				DebugServer.println( helpGName + " invited to " + eventName );
			}

		}
		
		Send_VolunteersInvitation_Request svir = new Send_VolunteersInvitation_Request( eventName, update );

		try
		{
			String result = svir.action();
			
			Hashtable<String,String> rMap = new Hashtable<String,String>();
			
			rMap.put( "mesageToInvitedVolunteers", result );
			
			resultMap.put( "VolunteersInvitattionToEvent",result );
		} 
		catch (Exception e)
		{
			resultMap.put( "VolunteersInvitattionToEvent",e.getMessage() );
			
			DebugServer.printException( "VolunteersInvitattionToEvent", e );
		}
		}
	
	
	protected JSONResponse post_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		DebugServer.println( "Events.post_PosProcess()" );
		
		if ( resultMap.containsKey( "insertedID" ) )
		{
			if ( eventName != null && clientGroupNames != null )
			{
				eventName = eventName.trim();
				
				processClientGroups( mySQL, eventName, clientGroupNames, resultMap, false );
			}
			
			if ( helpGroupNames != null && helpGroupNames != null )
			{
				eventName = eventName.trim();
				
				processVolunteerGroups( mySQL, eventName, helpGroupNames, resultMap, false );
			}
		}

		return super.post_PosProcess( mySQL, request, response, resultMap, paramMap );
	}
	

	@SuppressWarnings("unchecked")
	protected void put_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{				
		DebugServer.println( "Events.put_PreProcess()" );

		clientGroupNames 	= (ArrayList<String>)paramMap.get( "ClientGroupName");
		helpGroupNames 		= (ArrayList<String>)paramMap.get( "HelpGroupName");
		eventName			= (String)paramMap.get( "EventName");

		try
		{
			old_einf 	= Util.getEventInfo( mySQL, eventName );
			old_vgie 	= GetVoluntiersGroupsInvitedToEvents.DoGetVoluntiersGroupsInvitedToEvents( mySQL, eventName );
			old_cgie    = GetClientsGroupsInvitedToEvents.DoGetClientsGroupsInvitedToEvents( eventName );
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	//p1 = "yes" p2 = "no"
	Vector<String> calcTableChanges( Hashtable<String,String> a, ArrayList<String> b )
	{
		Vector<String> changes = new Vector<String>();
		
		Enumeration<String> a_keys = a.keys();
				
		while ( a_keys.hasMoreElements() )
		{
			String a_key = a_keys.nextElement();
			String a_val = a.get(a_key);
			
			if ( a_val.equalsIgnoreCase( "yes" ) && !b.contains( a_key ) )
			{
				changes.add( a_key );
			}
		}

		return changes;
	}
	
	protected JSONResponse put_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		if ( resultMap != null )
		{
			String eventName = (String)paramMap.get("EventName");

			try
			{
				EventInfo 					new_einf 	= Util.getEventInfo( mySQL, eventName );
				
				@SuppressWarnings("unchecked")
				ArrayList<String> helpGroupNames 	= (ArrayList<String>)paramMap.get("HelpGroupName");
				@SuppressWarnings("unchecked")
				ArrayList<String> clientGroupNames 	= (ArrayList<String>)paramMap.get("ClientGroupName");
				
				boolean addressDateChanged = 	!new_einf.city.equalsIgnoreCase(old_einf.city) 		||
												!new_einf.date.equalsIgnoreCase(old_einf.date) 		||
												!new_einf.time.equalsIgnoreCase(old_einf.time) 		||
												!new_einf.place.equalsIgnoreCase(old_einf.place) 	||
												!new_einf.state.equalsIgnoreCase(old_einf.state) 	||
												!new_einf.street.equalsIgnoreCase(old_einf.street);
		
				processClientGroups( mySQL, eventName, clientGroupNames, resultMap, addressDateChanged );
				processVolunteerGroups( mySQL, eventName, helpGroupNames, resultMap, addressDateChanged );

				Vector<String> volunteerGroupsCanceled	 = calcTableChanges( old_vgie, helpGroupNames );
				Vector<String> clientsGroupsCanceled 	 = calcTableChanges( old_cgie, clientGroupNames );

				if ( clientsGroupsCanceled != null && clientsGroupsCanceled.size() > 0 )
				{
					for ( String clientGroupName : clientsGroupsCanceled )
					{
						Send_ClientsCancelation scc = new Send_ClientsCancelation( eventName, old_einf,  clientGroupName );
						
						String result = scc.action();
						
						resultMap.put("Send_ClientsCancelation", result );
					}
				}
				
				if ( volunteerGroupsCanceled != null && volunteerGroupsCanceled.size() > 0  )
				{
					for ( String volunteerGroupName : volunteerGroupsCanceled )
					{
						Send_VolunteersCancelation svc = new Send_VolunteersCancelation( eventName, old_einf,  volunteerGroupName );
						
						String result = svc.action();
						
						resultMap.put("Send_VolunteersCancelation", result );
					}
				}
				
				System.out.println();
			} 
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return JSONResponse.success( resultMap );
		}
		else
		{
			return JSONResponse.not_success( 777, "CERO affected rows");
		}
	}

}
