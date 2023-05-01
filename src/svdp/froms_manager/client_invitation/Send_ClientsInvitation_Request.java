package svdp.froms_manager.client_invitation;

import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.Util;
import svdp.general.Util.EventInfo;
import svdp.tcp.DebugServer;

public class Send_ClientsInvitation_Request
{
	String 	eventName			= null;
	boolean eventUpdate			= false;
	
	public Send_ClientsInvitation_Request( String eventName, boolean eventUpdate )
	{
		this.eventName		= eventName;
		this.eventUpdate	= eventUpdate;
	}
		
	public String action() throws Exception
	{		
		DebugServer.println( "Send_ClientsInvitation_Request.action ( " + eventName + " )" );

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
						
			//Select to all clients invited to the event
			String query2 = null;
			
			DebugServer.println( "this.eventUpdate ( " + this.eventUpdate + " )" );

			if ( this.eventUpdate )
			{
				query2 = "SELECT CONCAT( Name, \" \", LastName ) AS NAME, HauseholdheadID, Email AS ID FROM ClientsInvitedToEvents INNER JOIN Hauseholdheads ON ClientsInvitedToEvents.HauseholdheadEmail=Hauseholdheads.Email WHERE ( Status=\"REGISTERED\" OR Status=\"INVITED\" OR Status=\"ACCEPTED\" ) AND EventName=\"" + eventInfo.name + "\""; 
			}
			else
			{
				query2 = "SELECT CONCAT( Name, \" \", LastName ) AS NAME, HauseholdheadID, Email AS ID FROM ClientsInvitedToEvents INNER JOIN Hauseholdheads ON ClientsInvitedToEvents.HauseholdheadEmail=Hauseholdheads.Email WHERE Status=\"REGISTERED\" AND EventName=\"" + eventInfo.name + "\""; 
			}
			
			Vector<Hashtable<String,String>> contacts = new Vector<Hashtable<String,String>>();
			
			String[] result2 = mysql.simpleAQuery( query2  );
			
			if ( mysql.getLastError() != null )
			{
			}
			else
			{
				for ( int i =0; i< result2.length; i+=3 )
				{
					String contactName	= result2[i];
					String contactCode 	= "SC" + String.format("%08X", Integer.valueOf( result2[i+1] ));
					String contactMail	= result2[i+2];
	
					Hashtable<String,String> contact = new Hashtable<String,String>(2);
	
					contact.put( "userName", contactName );
					contact.put( "userID",   contactCode );
					contact.put( "userMail", contactMail );
					
					contacts.add(contact);
				}
				
				DebugServer.println( "contacts.size()=" + contacts.size() );
				
				if ( contacts.size() > 0 )
				{
					Hashtable<String,Object> table = new Hashtable<String,Object>();
					
					table.put( "serverURL", Globals.serverURL );
					
					if ( this.eventUpdate )
					{
						table.put( "title", 		"Actualización de evento." );
					}
					else
					{
						table.put( "title", 		"Invitacion a evento." );
					}
					
					table.put( "senderID", 		Globals.superSystemContactCode );
					table.put( "senderName", 	Globals.superSystemContactName );
					//table.put( "formSource", 	Globals.serverURL + "/QuickChatForms" );
					table.put( "formFile", 		Globals.serverURL + "/QuickChatForms/ClientInvitationRequest.html" );
					
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
									
					Gson gson = Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
	
					String jsonStr = gson.toJson( table );
					
					String urlStr = Globals.quickChatURL + "/SendWebViewMessageToContacts";
				
					DebugServer.println( "urlStr=" + urlStr );

					URL url = new URL( urlStr );
					
					HttpURLConnection con = null;
					
					con = (HttpURLConnection)url.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type", "application/json");
					con.setRequestProperty("Accept", "application/json");
					con.setRequestProperty( "SecurityToken", "602d544c-5219-42dc-8e46-883de0de7613" );
					con.setDoOutput(true);
					
					OutputStream 	os 		= con.getOutputStream();
				    byte[] 			input 	= jsonStr.getBytes("utf-8");
				    os.write(input, 0, input.length);			
										
					LinkedTreeMap<String, Object> paramMap = svdp.general.Util.getParamMap( con.getInputStream(), gson, false );
					
					boolean success = paramMap.get("success") != null;
					
					if ( success )
					{
						DebugServer.println( "Set Status=INVITED to all clients invited to the event" );
						//Set Status=INVITED to all clients invited to the event
						for ( int i =0; i< result2.length; i+=3 )
						{
							String contactMail	= result2[i+2];
			
							String command = "UPDATE ClientsInvitedToEvents SET Status=\"INVITED\" WHERE HauseholdheadEmail=\"" + contactMail + "\";";
			
							mysql.executeCommand(command);
						}
						
						result = "SUCCESS: invitation successfully sent to " + contacts.size() + " client(s).";
					}
					else
					{
						result = "ERROR: Internal error...";
					}
				}
				else
				{
					result = "SUCCESS: All clients have already been invited.";
				}
			}
		}
		finally
		{
			mysql.close();
		}
		
		DebugServer.println( "result=" + result );

		return result;
	}
}
