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
import svdp.general.Util.EventInfo;

public class Send_ClientsCancelation
{
	String 		eventName			= null;
	EventInfo	eventInfo			= null;
	String 		clientGroupName		= null;
	
	public Send_ClientsCancelation( String eventName, EventInfo	eventInfo, String clientGroupName )
	{
		this.eventName			= eventName;
		this.eventInfo			= eventInfo;
		this.clientGroupName	= clientGroupName;
	}
		
	public String action() throws Exception
	{		
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
			//Select to all HauseholdheadEmail from ClientGroupName
			String query1 = "SELECT HauseholdheadEmail FROM ClientGroupHHHeads WHERE ClientGroupName=\"" + clientGroupName + "\";";
			Vector<String> result1 = mysql.simpleVQuery( query1  );

			//Select to all clients invited to the event
			String query2 = null;
			query2 = "SELECT CONCAT( Name, \" \", LastName ) AS NAME, HauseholdheadID, Email AS ID FROM ClientsInvitedToEvents INNER JOIN Hauseholdheads ON ClientsInvitedToEvents.HauseholdheadEmail=Hauseholdheads.Email WHERE ( Status=\"INVITED\" OR Status=\"ACCEPTED\" ) AND EventName=\"" + eventName + "\""; 
			
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
	
					if ( result1.contains(contactMail) )
					{
						Hashtable<String,String> contact = new Hashtable<String,String>(2);
		
						contact.put( "userName", contactName );
						contact.put( "userID",   contactCode );
						contact.put( "userMail", contactMail );
						
						contacts.add(contact);
					}
				}
				
				if ( contacts.size() > 0 )
				{
					Hashtable<String,Object> table = new Hashtable<String,Object>();
					
					table.put( "serverURL", Globals.serverURL );
					
					table.put( "title", 		"Evento Cancelado." );
					table.put( "senderID", 		Globals.superSystemContactCode );
					table.put( "senderName", 	Globals.superSystemContactName );					
					table.put( "contacts", 		contacts );
					table.put( "textMessage",  "El evento " + this.eventName + " ha sido cancelado." );

					Gson gson = Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
	
					String jsonStr = gson.toJson( table );
					
					String urlStr = Globals.quickChatURL + "/SendTextMessage2Contacts";
					
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
						for ( Hashtable<String, String> contact : contacts )
						{
							String contactMail	= contact.get( "userMail" );
			
							String command = "DELETE FROM ClientsInvitedToEvents WHERE EventName = \"" + eventName + "\" AND HauseholdheadEmail=\"" + contactMail + "\";";
			
							mysql.executeCommand(command);
						}
						
						result = "SUCCESS: cancelations successfully sent to " + contacts.size() + " client(s).";
					}
					else
					{
						result = "ERROR: Internal error...";
					}
				}
				else
				{
					result = "SUCCESS: No clients have been canceled.";
				}
				
				{
					String command = "DELETE FROM ClientsGroupsInvitedToEvents WHERE EventName = \"" + eventName + "\" AND ClientGroupName=\"" + clientGroupName + "\";";

					mysql.executeCommand(command);
				}
			}
		}
		finally
		{
			mysql.close();
		}
		
		return result;
	}
}
