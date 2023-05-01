package svdp.servlets_utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;
import svdp.tcp.DebugServer;

/**
 * Servlet implementation class GetQuickChatContacts
 */
@WebServlet("/GetQuickChatContacts")
public class GetQuickChatContacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetQuickChatContacts() 
    {
        super();
    }

	private LinkedTreeMap<String,String>  getParamMap( HttpServletRequest request, Gson gson ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	//int comment = line.lastIndexOf("//");
        	
        	//if ( comment >= 0 )
        	//{
        	//	line = line.substring( 0, comment);
        	//}
        	
        	jsontxt += line;
        }

        DebugServer.println( "getParamMap:"  + jsontxt );
        
		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,String> paramMap = (LinkedTreeMap<String, String>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

    public static boolean isNumeric(String strNum) 
    {
        if (strNum == null) 
        {
            return false;
        }
        try 
        {
            Integer.parseInt(strNum);
        } 
        catch (NumberFormatException nfe) 
        {
        	DebugServer.printException( "NumberFormatException:", nfe);
        	
            return false;
        }
        
        return true;
    }

    private void personContacts( MySQL mysql, char type, char kind, long rIgD, Vector<Hashtable<String, String>> resultVect, Gson gson,OutputStreamWriter 	osw )
    {
    	DebugServer.println( "personContacts()" );
    	
		// Super System Contact
		{
			DebugServer.println( "Super System Contact" );
			
			Hashtable<String,String> contact = new Hashtable<String,String>(4);
				
			contact.put( "uKind", 		 Globals.superSystemContactKind );
			contact.put( "uType", 		 Globals.superSystemContactType );
			contact.put( "uID", 		 Globals.superSystemContactID );
			contact.put( "displayName",  Globals.superSystemContactName );
			
			resultVect.add(contact);
		}
		
		if ( type == 'M' )//ChapterMember
		{
			DebugServer.println( "ChapterMember" );
			
			// Chapter Member Group
			{ 
				Hashtable<String,String> contacts = new Hashtable<String,String>(3);

				contacts.put( "uKind", 		 "G" );
				contacts.put( "uType", 		 "M" );
				contacts.put( "uID", 		 "1" );
				contacts.put( "displayName", "Chapter Members" );
				
				resultVect.add(contacts);
			}									
			
			{
				// Select all help groups
				String query0 = "SELECT HelpGroupID, HelpGroupName FROM HelpGroups;";
				
				String[] response1 = mysql.simpleAQuery( query0 );
				
				if ( response1 != null)
				{
					for ( int i=0; i<response1.length; i+=2 )
					{
						int 	groupID 	= Integer.valueOf(response1[i]);
						String 	groupName 	= response1[i+1];
						
						// Volunteer Group
						{
							Hashtable<String,String> contact = new Hashtable<String,String>(4);
		
							contact.put( "uKind", 		 "G" );
							contact.put( "uType", 		 "V" );
							contact.put( "uID", 		 String.valueOf(groupID) );
							contact.put( "displayName",  groupName );
							
							resultVect.add(contact);
						}	
					}
				}
			}
			/*
			{
				// Select all clients groups
				String query0 = "SELECT ClientGroupID, ClientGroupName FROM ClientGroups;";
				
				String[] response1 = mysql.simpleAQuery( query0 );
				
				if ( response1 != null)
				{
					for ( int i=0; i<response1.length; i+=2 )
					{
						int 	groupID 	= Integer.valueOf(response1[i]);
						String 	groupName 	= response1[i+1];
						
						// Client Group
						{
							Hashtable<String,String> contact = new Hashtable<String,String>(4);
		
							contact.put( "uKind", 		 "G" );
							contact.put( "uType", 		 "C" );
							contact.put( "uID", 		 String.valueOf(groupID) );
							contact.put( "displayName",  groupName );
							
							resultVect.add(contact);
						}	
					}
				}
			}*/
		}
		else
		if ( type == 'V' )//Volunteer
		{	
			DebugServer.println( "Volunteer" );
			
			// Self Volunteer
			{
				DebugServer.println( "Self Volunteer" );
				
				String query1 = "SELECT VoluntierID, CONCAT( FirstName, \" \", LastName ) FROM Voluntiers WHERE VoluntierID=" + rIgD + ";";
			
				String[] response1 = mysql.simpleAQuery( query1 );
				
				if ( mysql.getLastError() != null )
				{
					DebugServer.println( "mysql.getLastError()" + mysql.getLastError() + " " + query1 );
				}
				
				if ( response1 != null )
				{
					Hashtable<String,String> contact = new Hashtable<String,String>(4);

					contact.put( "uKind", 		 "S" );
					contact.put( "uType", 		 "V" );
					contact.put( "uID", 		 response1[0] );
					contact.put( "displayName",  response1[1] );
					
					resultVect.add(contact);
				}	
			}
			
			// Groups where this volunteer is included
			{
				DebugServer.println( "Groups where this volunteer is included" );
				
				String query1 = "SELECT HelpGroupName FROM HelpGroupsVoluntier WHERE VoluntierEmail=( SELECT Email FROM Voluntiers WHERE VoluntierID=" + rIgD + " );";
				
				String[] helpGroupNames = mysql.simpleAQuery( query1 );
				
				if ( helpGroupNames != null)
				{
					for ( int i=0; i<helpGroupNames.length; i++ )
					{
						String query2 = "SELECT HelpGroupID FROM HelpGroups WHERE HelpGroupName=\"" + helpGroupNames[i] + "\";";

						String 	helpGroupID 	= mysql.simpleQuery( query2 );
						int 	groupID 		= Integer.valueOf( helpGroupID );
						String 	groupName 		= helpGroupNames[i];
						
						// Volunteer Group
						{
							Hashtable<String,String> contact = new Hashtable<String,String>(4);
		
							contact.put( "uKind", 		 "G" );
							contact.put( "uType", 		 "V" );
							contact.put( "uID", 		 String.valueOf(groupID) );
							contact.put( "displayName",  groupName );
							
							resultVect.add(contact);
						}	
						
						//Volunteer Contacts
						/*
						{
							String query2 =   "SELECT Voluntiers.VoluntierID AS ID, CONCAT( FirstName, \" \", LastName ) AS NAME\r\n"
											+ "FROM Voluntiers\r\n"
											+ "INNER JOIN HelpGroupsVoluntier \r\n"
											+ "ON Voluntiers.Email=HelpGroupsVoluntier.VoluntierEmail WHERE HelpGroupName=\"" + groupName + "\" GROUP BY Voluntiers.VoluntierID";
									
							String[] response2 = mysql.simpleAQuery( query2 );
							
							for ( int j=0; j<response2.length; j+=2 )
							{
								int 	volID 	= Integer.valueOf( response2[j] );
								String 	volName = response2[j+1];
								
								Hashtable<String,String> contact = new Hashtable<String,String>(3);
								
								contact.put( "uKind", 		"S" );
								contact.put( "uType", 		"V" );
								contact.put( "uID", 		String.valueOf(volID) );
								contact.put( "displayName", volName );
								
								resultVect.add(contact);
							}
						}*/
					}
				}
			}
			
			// Assigned Clients
			{
				DebugServer.println( "Assigned Clients" );
				
				String visitorEMail = mysql.simpleQuery( "SELECT Email FROM Voluntiers WHERE VoluntierID=" + rIgD );

				String query2 = "SELECT CONCAT( Name, \" \", LastName ) AS CNAME, HauseholdheadID FROM Hauseholdheads WHERE AsignedVisitor=\"" + visitorEMail + "\"";
				
				String[] response2 = mysql.simpleAQuery( query2 );
				
				for ( int j=0; j<response2.length; j+=2 )
				{
					String 	clieName 	= response2[j];
					int 	clieID 		= Integer.valueOf( response2[j+1] );
					
					Hashtable<String,String> contact = new Hashtable<String,String>(3);
					
					contact.put( "uKind", 		"S" );
					contact.put( "uType", 		"C" );
					contact.put( "uID", 		String.valueOf(clieID) );
					contact.put( "displayName", clieName );
					
					resultVect.add(contact);
				}
			}
		}
		else
		if ( type == 'C' )//Client
		{	
			DebugServer.println( "Client" );
			
			// Self Contact
			{				
				DebugServer.println( "Self Contact" );
				
				String query = "SELECT HauseholdheadID, CONCAT( Name, \" \", LastName ) FROM Hauseholdheads WHERE HauseholdheadID=" + rIgD + ";";

				String[] response = mysql.simpleAQuery( query );
				
				if ( response != null )
				{
					Hashtable<String,String> contact = new Hashtable<String,String>(4);
						
					contact.put( "uKind", 		"S"  );
					contact.put( "uType", 		"C" );
					contact.put( "uID", 		response[0] );
					contact.put( "displayName", response[1] );
					
					resultVect.add(contact);
				}
			}
			
			// Assigned visitor
			{
				DebugServer.println( "Assigned visitor" );
				
				String visitorEMail = mysql.simpleQuery( "SELECT AsignedVisitor FROM Hauseholdheads WHERE HauseholdheadID=" + rIgD );
				
				if ( visitorEMail != null )
				{
					String query = "SELECT VoluntierID, CONCAT( FirstName, \" \", LastName ) FROM Voluntiers WHERE Email=\"" + visitorEMail + "\"";
	
					String[] response = mysql.simpleAQuery( query );
					
					if ( response != null )
					{
						Hashtable<String,String> contact = new Hashtable<String,String>(4);
							
						contact.put( "uKind", 		"S"  );
						contact.put( "uType", 		"V" );
						contact.put( "uID", 		response[0] );
						contact.put( "displayName", response[1] );
						
						resultVect.add(contact);
					}
				}
			}
			
			/*
			// Select the groups where this client is included
			{
				String query1 = "SELECT ClientGroupHHHeadsID, ClientGroupName FROM ClientGroupHHHeads WHERE HauseholdheadEmail=( SELECT Email FROM Hauseholdheads WHERE HauseholdheadID=" + registryID + " ) GROUP BY ClientGroupName;";
				
				String[] response1 = mysql.simpleAQuery( query1 );
				
				if ( response1 != null)
				{
					for ( int j=0; j<response1.length; j+=2 )
					{
						int 	groupID 	= Integer.valueOf(response1[j]);
						String 	groupName 	= response1[j+1];
						
						//Client Group
						{ 
							Hashtable<String,String> contact = new Hashtable<String,String>(4);
		
							contact.put( "uKind", 		"G" );
							contact.put( "uType", 		"C" );
							contact.put( "uID", 		String.valueOf(groupID) );
							contact.put( "displayName",	groupName );
							
							resultVect.add(contact);
						}	
						
						
						//Select clients in this group
						String query2 =   "SELECT Voluntiers.VoluntierID AS ID, CONCAT( FirstName, \" \", LastName ) AS NAME\r\n"
										+ "FROM Voluntiers\r\n"
										+ "INNER JOIN HelpGroupsVoluntier \r\n"
										+ "ON Voluntiers.Email=HelpGroupsVoluntier.VoluntierEmail WHERE HelpGroupName=\"" + groupName + "\" GROUP BY Voluntiers.VoluntierID";
								
						String[] response2 = mysql.simpleAQuery( query2 );
						
						for ( int j=0; j<response2.length; j+=2 )
						{
							int 	volID 	= Integer.valueOf( response2[0] );
							String 	volName = response2[1];
							//Client Contact
							{
								Hashtable<String,String> contact = new Hashtable<String,String>(3);
								
								contact.put( "uKind", 		"S" );
								contact.put( "uType", 		"C" );
								contact.put( "uID", 		String.valueOf(volID) );
								contact.put( "displayName", volName );
								
								resultVect.add(contact);
							}
						}
					}
				}
			}
			
			// Assigned Visitor
			{
				String query2 =   "SELECT VoluntierID,  CONCAT( FirstName, \" \", LastName ) \r\n"
								+ "FROM Voluntiers \r\n"
								+ "WHERE Email=( SELECT AsignedVisitor FROM Hauseholdheads WHERE HauseholdheadID=" + rIgD + " );";
				
				String[] response2 = mysql.simpleAQuery( query2 );
				
				for ( int j=0; j<response2.length; j+=2 )
				{
					int 	voluntID 	= Integer.valueOf( response2[j] );
					String 	voluntName 	= response2[j+1];
					
					Hashtable<String,String> contact = new Hashtable<String,String>(3);
					
					contact.put( "uKind", 		"S" );
					contact.put( "uType", 		"V" );
					contact.put( "uID", 		String.valueOf(voluntID) );
					contact.put( "displayName", voluntName );
					
					resultVect.add(contact);
				}
			}*/
		}
		
		DebugServer.println( "personContacts()-END" );
    }
    
    private void groupContacts( MySQL mysql, char type, char kind, long rIgD, Vector<Hashtable<String, String>> resultVect, Gson gson,OutputStreamWriter 	osw )
    {
		if ( type == 'M' )//ChapterMember
		{						
			// Self Group
			{ 
				Hashtable<String,String> contacts = new Hashtable<String,String>(3);

				contacts.put( "uKind", 		 "G" );
				contacts.put( "uType", 		 "M" );
				contacts.put( "uID", 		 "1" );
				contacts.put( "displayName", "Chapter Members" );
				
				resultVect.add(contacts);
			}	
			
			// Chapter GROUP Members contacts
			{
				String query = 	"SELECT ChapterMemberID, CONCAT( FirstName,\" \", LastName ) FROM ChapterMembers";
				
				String[] result = mysql.simpleAQuery( query  );
	
				if ( mysql.getLastError() != null )
				{
					gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() + " " + query ), osw );
				}
				else
				for ( int j =0; j< result.length; j+=2 )
				{
					Hashtable<String,String> contacts = new Hashtable<String,String>(3);

					contacts.put( "uKind", 		 "S" );
					contacts.put( "uType", 		 "M" );
					contacts.put( "uID", 		 result[j] 	);
					contacts.put( "displayName", result[j+1] );
					
					resultVect.add(contacts);
				}
			}
		}
		else
		if ( type == 'V' )//Volunteer
		{			
			Integer 	groupID 	= null;
			String 		groupName 	= null;

			// Select self volunteer group
			{
				String query0 = "SELECT HelpGroupName FROM HelpGroups WHERE HelpGroupID=" + rIgD;

				String helpGroupName = mysql.simpleQuery( query0 );
				
				if ( helpGroupName != null )
				{
					String query1 = "SELECT HelpGroupsVoluntierID, HelpGroupName FROM HelpGroupsVoluntier WHERE helpGroupName=\"" + helpGroupName + "\"";
					
					String[] response1 = mysql.simpleAQuery( query1 );
					
					if ( response1 != null)
					{
						groupID 	= Integer.valueOf(response1[0]);
						groupName 	= response1[1];
						
						// Volunteer Group
						{
							Hashtable<String,String> contact = new Hashtable<String,String>(4);
		
							contact.put( "uKind", 		 "G" );
							contact.put( "uType", 		 "V" );
							contact.put( "uID", 		 String.valueOf(groupID) );
							contact.put( "displayName",  groupName );
							
							resultVect.add(contact);
						}	
					}
				}
			}
			
			if ( groupName != null)
			{
				//Volunteer Group Contacts
				{
					String query2 =   "SELECT Voluntiers.VoluntierID AS ID, CONCAT( FirstName, \" \", LastName ) AS NAME\r\n"
									+ "FROM Voluntiers\r\n"
									+ "INNER JOIN HelpGroupsVoluntier \r\n"
									+ "ON Voluntiers.Email=HelpGroupsVoluntier.VoluntierEmail WHERE HelpGroupName=\"" + groupName + "\" GROUP BY Voluntiers.VoluntierID";
							
					String[] response2 = mysql.simpleAQuery( query2 );
					
					for ( int j=0; j<response2.length; j+=2 )
					{
						int 	volID 	= Integer.valueOf( response2[j] );
						String 	volName = response2[j+1];
						
						Hashtable<String,String> contact = new Hashtable<String,String>(3);
						
						contact.put( "uKind", 		"S" );
						contact.put( "uType", 		"V" );
						contact.put( "uID", 		String.valueOf(volID) );
						contact.put( "displayName", volName );
						
						resultVect.add(contact);
					}
				}
			}
			
			// Chapter GROUP Members contacts
			{
				String query = 	"SELECT ChapterMemberID, CONCAT( FirstName,\" \", LastName ) FROM ChapterMembers";
				
				String[] result = mysql.simpleAQuery( query  );
	
				if ( mysql.getLastError() != null )
				{
					gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() + " " + query ), osw );
				}
				else
				for ( int j =0; j< result.length; j+=2 )
				{
					Hashtable<String,String> contacts = new Hashtable<String,String>(3);

					contacts.put( "uKind", 		 "S" );
					contacts.put( "uType", 		 "M" );
					contacts.put( "uID", 		 result[j] 	);
					contacts.put( "displayName", result[j+1] );
					
					resultVect.add(contacts);
				}
			}
		}
		else
		if ( type == 'C' )//Client
		{			
			//Integer 	groupID 	= null;
			//String 		groupName 	= null;

			/*
			// Self Client Group
			{
				String query0 = "SELECT ClientGroupName FROM ClientGroups WHERE ClientGroupID=" + rIgD + ";";
	
				String[] response1 = mysql.simpleAQuery( query0 );
				
				if ( response1 != null)
				{
					groupID 	= Integer.valueOf(response1[0]);
					groupName 	= response1[1];
					
					{
						Hashtable<String,String> contact = new Hashtable<String,String>(4);
	
						contact.put( "uKind", 		 "G" );
						contact.put( "uType", 		 "V" );
						contact.put( "uID", 		 String.valueOf(groupID) );
						contact.put( "displayName",  groupName );
						
						resultVect.add(contact);
					}	
				}
			}
			
			//Select contacts in client group
			if ( groupName != null)
			{							
				String query2 =   "SELECT Voluntiers.VoluntierID AS ID, CONCAT( FirstName, \" \", LastName ) AS NAME\r\n"
								+ "FROM Voluntiers\r\n"
								+ "INNER JOIN HelpGroupsVoluntier \r\n"
								+ "ON Voluntiers.Email=HelpGroupsVoluntier.VoluntierEmail WHERE HelpGroupName=\"" + groupName + "\" GROUP BY Voluntiers.VoluntierID";
						
				String[] response2 = mysql.simpleAQuery( query2 );
				
				for ( int j=0; j<response2.length; j+=2 )
				{
					int 	volID 	= Integer.valueOf( response2[j] );
					String 	volName = response2[j+1];
					//Client Contact
					{
						Hashtable<String,String> contact = new Hashtable<String,String>(3);
						
						contact.put( "uKind", 		"S" );
						contact.put( "uType", 		"C" );
						contact.put( "uID", 		String.valueOf(volID) );
						contact.put( "displayName", volName );
						
						resultVect.add(contact);
					}
				}
			}*/
		}
	
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		DebugServer.println( "GetQuickChatContacts" );
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    	    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		LinkedTreeMap<String, String> paramMap = null;
		try
		{
			DebugServer.println( "getParamMap()" );
			
			paramMap = getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1201, e1.getMessage() ), osw );
			DebugServer.printException( "IOException:", e1);
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1211, e1.getMessage() ), osw );
			DebugServer.printException( "IOException:", e1);
			osw.flush();
			return;
		}

		Vector<Hashtable<String, String>> resultVect 	= new Vector<Hashtable<String, String>>();
		
		/*
	    String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	TraceListener.println( "SecurityToken" );
	    	
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }
		 */
		
		String kindStr 		= paramMap.get("kind");
		String typeStr 		= paramMap.get("type");
		String registryID	= paramMap.get("registryID");

		DebugServer.println( "GetQuickChatContacts -- BEGIN " + kindStr + " " + typeStr + " " + registryID );
		
		if ( kindStr == null || (kindStr.equals("G") && kindStr.equals("S")) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null kind" ), osw ); 
			DebugServer.println( "Input Parameters error bad or null kind" );
		}
		else
		if ( typeStr == null || typeStr.length() != 1 )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null type" ), osw ); 
			DebugServer.println( "Input Parameters error bad or null type" );
		}
		if ( !isNumeric( registryID ) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null registryID" ), osw ); 
			DebugServer.println( "Input Parameters error bad or null registryID" );
		}
		else
		{			
			char kind 	= paramMap.get("kind").charAt(0);
			char type 	= paramMap.get("type").charAt(0);
			long rIgD	= Long.valueOf( registryID );
			
			DebugServer.println( "kind: " + kind );
			DebugServer.println( "type: " + type );
			DebugServer.println( "rIgD: " + rIgD );
			
			MySQL mysql = new MySQL();
	
			/*
			type:
				C = Client
				V = Volunteer
				M = ChapterMember
			*/
			
			try
			{
				if ( kind == 'G' )//Group contacts
				{
				    groupContacts( mysql, type, kind, rIgD, resultVect, gson, osw );					
				}
				if ( kind == 'S' ) //Single person contacts
				{
				    personContacts( mysql, type, kind, rIgD, resultVect, gson, osw );
				}
			} 
			catch (Exception e)
			{
				DebugServer.printException( "groupContacts/groupContacts", e );
			}
			finally
			{
				mysql.close();
			}
	
			//TraceListener.println( "GetQuickChatContacts -- END" );
			
			JSONResponse posP = JSONResponse.success( resultVect );
						
		    gson.toJson( posP, osw );		   
		}
		
		response.setStatus( HttpServletResponse.SC_OK );
		
		osw.flush();
	}
}
