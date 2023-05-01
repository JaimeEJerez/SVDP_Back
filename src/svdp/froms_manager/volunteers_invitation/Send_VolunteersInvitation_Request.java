package svdp.froms_manager.volunteers_invitation;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.Util;
import svdp.general.Util.EventInfo;
import svdp.tcp.DebugServer;

public class Send_VolunteersInvitation_Request
{
	String 	eventName			= null;
	boolean eventUpdate			= false;
	
	public Send_VolunteersInvitation_Request( String eventName, boolean eventUpdate )
	{
		this.eventName			= eventName;
		this.eventUpdate		= eventUpdate;
	}
		
	public String action() throws Exception
	{		
		DebugServer.println( "Send_VolunteersInvitation_Request ( " + this.eventName + " )" );

		String result = null;
		
		MySQL mysql = new MySQL();
		
		/*
		type:
			C = Client
			V = Volunteer
			M = ChapterMember
		*/
		
		try
		{	
			EventInfo eventInfo = Util.getEventInfo( mysql, eventName );
						
			String query2 = null;

			DebugServer.println( "eventUpdate=" + eventUpdate );

			if ( eventUpdate )
			{
				query2 = "SELECT CONCAT( FirstName, \" \", LastName ) AS userName, CONCAT( \"SV\", LPAD( HEX(VoluntierID), 8, 0 ) ) AS userID, Email AS userMail FROM VoluntiersInvitedToEvents INNER JOIN Voluntiers ON VoluntiersInvitedToEvents.VoluntierEmail=Voluntiers.Email WHERE ( Status=\"REGISTERED\" OR Status=\"INVITED\" OR Status=\"ACCEPTED\" ) AND EventName=\"" + eventInfo.name + "\""; 
			}
			else
			{
				query2 = "SELECT CONCAT( FirstName, \" \", LastName ) AS userName, CONCAT( \"SV\", LPAD( HEX(VoluntierID), 8, 0 ) ) AS userID, Email AS userMail FROM VoluntiersInvitedToEvents INNER JOIN Voluntiers ON VoluntiersInvitedToEvents.VoluntierEmail=Voluntiers.Email WHERE Status=\"REGISTERED\" AND EventName=\"" + eventInfo.name + "\""; 
			}
			
			Vector<Map<String, String>> contacts = new Vector<Map<String, String>>();
			
			mysql.simpleHMapQueryAdd( query2, contacts );
			
			if ( mysql.getLastError() != null )
			{
			}
			else
			{
				if ( contacts.size() > 0 )
				{
					Hashtable<String,Object> table = new Hashtable<String,Object>();
					
					table.put( "serverURL", 	Globals.serverURL );
					
					if ( this.eventUpdate )
					{
						table.put( "title", 		"Invitacion a evento." );
					}
					else
					{
						table.put( "title", 		"Actualización de evento." );
					}

					table.put( "senderID", 		Globals.superSystemContactCode );
					table.put( "senderName", 	Globals.superSystemContactName );
					table.put( "formFile", 		Globals.serverURL + "/QuickChatForms/VolunteerInvitationRequest.html" );
					
					table.put( "contacts", 		contacts );
					table.put( "event_id", 		eventInfo.id );
					table.put( "event_name", 	eventInfo.name );
					table.put( "event_type", 	eventInfo.type );
					table.put( "event_date", 	eventInfo.date );
					table.put( "event_time", 	eventInfo.time );
					table.put( "event_state", 	eventInfo.state );
					table.put( "event_city", 	eventInfo.city );
					table.put( "event_street", 	eventInfo.street );
					table.put( "event_place", 	eventInfo.place );
														
					String urlStr = Globals.quickChatURL + "/SendWebViewMessageToContacts";
					
					DebugServer.println( "urlStr=" + urlStr );

					LinkedTreeMap<String, Object> paramMap = Util.postObjectLikeJSON( urlStr, table );
					
					boolean success = paramMap.get("success") != null;
					
					if ( success )
					{
						DebugServer.println( "Set Status=INVITED to all volunteers invited to the event" );
						//Set Status=INVITED to all volunteers invited to the event
						for (  Map<String, String> contact : contacts )
						{
							String contactMail	= contact.get( "userMail" );
			
							String command = "UPDATE VoluntiersInvitedToEvents SET Status=\"INVITED\" WHERE VoluntierEmail=\"" + contactMail + "\";";
			
							mysql.executeCommand(command);
						}

						result = "SUCCESS: invitation successfully sent to " + contacts.size() + " volunteer(s).";
					}
					else
					{
						result = "ERROR: Internal error...";
					}
				}
				else
				{
					result = "SUCCESS: All voluntters have already been invited.";
				}
			}
		}
		finally
		{
			mysql.close();
		}
		
		DebugServer.println( "result="  + result );

		return result;
	}
}
