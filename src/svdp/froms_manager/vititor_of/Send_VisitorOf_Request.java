package svdp.froms_manager.vititor_of;

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
import svdp.general.Util.HouseHHInfo;

public class Send_VisitorOf_Request
{
	long hauseholdheadID = 0;
	
	public Send_VisitorOf_Request( long hauseholdheadID )
	{
		this.hauseholdheadID = hauseholdheadID;
	}
	
	public boolean action() throws Exception
	{
		boolean success = false;
		
		MySQL mysql = new MySQL();
		
		/*
		type:
			C = Client
			V = Volunteer
			M = ChapterMember
		*/
		
		try
		{	
			HouseHHInfo hhhInfo = Util.getHHHeadInfo( mysql, hauseholdheadID );
			
			String query2 = "SELECT CONCAT( FirstName, \" \", LastName ) AS NAME, VoluntierID FROM Voluntiers WHERE Visitor=\"yes\""; 

			Vector<Hashtable<String,String>> contacts = new Vector<Hashtable<String,String>>();
			
			String[] result2 = mysql.simpleAQuery( query2  );
			
			if ( mysql.getLastError() != null )
			{
			}
			else
			{
				for ( int i =0; i< result2.length; i+=2 )
				{
					String contactName	= result2[i];
					String contactCode 	= "SV" + String.format("%08X", Integer.valueOf( result2[i+1] ));
	
					Hashtable<String,String> contact = new Hashtable<String,String>(2);
	
					contact.put( "userName", contactName );
					contact.put( "userID",   contactCode );
					
					contacts.add(contact);
				}
								
				Hashtable<String,Object> table = new Hashtable<String,Object>();
				
				table.put( "serverURL", Globals.serverURL );
				
				table.put( "title", 		"Pedido de atencion." );
				table.put( "senderID", 		Globals.superSystemContactCode );
				table.put( "senderName", 	Globals.superSystemContactName );
				
				table.put( "client_name", 	hhhInfo.name );
				table.put( "client_id", 	String.valueOf( hhhInfo.id)  );
				
				//table.put( "formSource", 	Globals.serverURL + "/QuickChatForms" );
				
				table.put( "formFile", 		Globals.serverURL + "/QuickChatForms/VisitorRequest.html" );
				table.put( "contacts", 		contacts );
				
				table.put("id", 	String.valueOf( hhhInfo.id ) );
				table.put("name", 	hhhInfo.name  	);
				table.put("phone", 	hhhInfo.phone 	);
				table.put("state", 	hhhInfo.state 	);
				table.put("city", 	hhhInfo.city 	);
				table.put("street", hhhInfo.street 	);
				table.put("aptoHab",hhhInfo.aptoHab );

				Gson gson = Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();

				String jsonStr = gson.toJson( table );
				
				String urlStr = Globals.quickChatURL + "/SendWebViewMessageToContacts";
				
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
				
				success = paramMap.get("success") != null;
			}
		}
		finally
		{
			mysql.close();
		}
		
		return success;
	}
}
