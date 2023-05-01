package svdp.froms_manager.vititor_of;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.Util;
import svdp.general.Util.HouseHHInfo;


/**
 * Servlet implementation class WebViewFormResponse
 */
@WebServlet("/VisitorOf_FormAction")
public class VisitorOf_FormAction extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final Object sinchronizer = new Object();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VisitorOf_FormAction() 
    {
        super();
    }

    public static boolean sendQuickChatTextMessage( String senderID, String receiverID, String message ) throws IOException
    {
    	/*
    	https://tc.yoifirst.com/QuickChat/SendTextMessage
    	{
    	    "senderID":"US00000002", // Luis M
    	    "recipiID":"US00000001", // Carlos Z
    	    "textMessage":"Hola Luis M  soy Carlos Z desde Postman 33"
    	}
    	*/
    	
		String urlStr = Globals.quickChatURL + "/SendTextMessage";
		URL url = new URL( urlStr );
		
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty( "SecurityToken", "602d544c-5219-42dc-8e46-883de0de7613" );
		con.setDoOutput(true);
		
		String jsonInputString 	= "{\"senderID\":\"" + senderID + "\", \"recipiID\":\"" + receiverID + "\", \"textMessage\":\"" + message + "\" }";
		
		OutputStream 	os 		= con.getOutputStream();
	    byte[] 			input 	= jsonInputString.getBytes("utf-8");
	    os.write(input, 0, input.length);			
		
		Gson	gson	= svdp.general.Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		
		LinkedTreeMap<String, Object> paramMap;
		try
		{
			paramMap = Util.getParamMap( con.getInputStream(), gson, false );
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			
			throw new IOException( e.getMessage() );
		}
		
		boolean success = (Boolean)paramMap.get("success");

    	return success;
    }
    
    public static final String getParameter(  Map<String, String[]> parameterMap, 
    										  String 				paramName,
    										  String 				defauld )
    {
    	String values[] = parameterMap.get( paramName );
    	
    	return (values != null && values.length > 0) ? values[0] : defauld;
    }
    
	protected String reply(String messageID, String senderID, String receiverID, Map<String, String[]> parameterMap) throws IOException
	{
		@SuppressWarnings("unused")
		String  title 		= getParameter( parameterMap, "title", "" );
		String  clientName 	= getParameter( parameterMap, "clientName", "" );
		long    clientID	= Long.valueOf( getParameter( parameterMap, "clientID", "0" ) );
		String  response 	= getParameter( parameterMap, "response", "" );		
				
		String messageText = "Gracias por su amable colaboración";
		
		if ( response.equalsIgnoreCase( "si" ) )
		{
			MySQL mysql = new MySQL();
			
			try
			{
				boolean conectEachUne = false;
				
				synchronized ( sinchronizer ) 
				{
					String query = "SELECT AsignedVisitor FROM Hauseholdheads WHERE HauseholdheadID=" + clientID;
					
					String asignedVisitor = mysql.simpleQuery(query);
					
					if ( asignedVisitor == null || asignedVisitor.isEmpty() )
					{
						messageText += ( ", " + clientName + " le ha sido asignado." );
						
						long id = Long.parseLong( receiverID.substring(2), 16) ;
						
						String visitorEMail = mysql.simpleQuery( "SELECT Email FROM Voluntiers WHERE VoluntierID=" + id );
						
						String command = "UPDATE Hauseholdheads SET AsignedVisitor=\"" + visitorEMail + "\" WHERE HauseholdheadID=" + clientID;
						
						mysql.executeCommand(command);
						
						conectEachUne = true;
					}
					else
					{
						messageText += ( ", " + clientName + " ya le ha sido asignado a otro voluntario." );
					}
				}
				
				if ( conectEachUne )
				{
					String volunteerCode 	= receiverID;
					String clientCode		= "SC" + String.format("%08X", clientID ).toUpperCase();
					
					HouseHHInfo hhhInfo = Util.getHHHeadInfo( mysql,  clientID );

					String message1 = "Hola, soy el voluntario asignado a su caso.";

					String message2 = 	  "<b>Hola, soy el nuevo cliente que le ha sido asignado:</b><br>"
										+ "<b>Nombre  :</b>" 	+ hhhInfo.name 		+ "<br>"
										+ "<b>Telefono:</b>" 	+ hhhInfo.phone 	+ "<br>"
										+ "<b>Ciudad  :</b>" 	+ hhhInfo.city 		+ "<br>"
										+ "<b>Estado  :</b>" 	+ hhhInfo.state  	+ "<br>"
										+ "<b>Calle   :</b>" 	+ hhhInfo.street 	+ "<br>"
										+ "<b>Apto.   :</b>" 	+ hhhInfo.aptoHab ;
					
				    sendQuickChatTextMessage( volunteerCode, clientCode,  message1 );
				    sendQuickChatTextMessage( clientCode, volunteerCode,  message2 );
				}
			}
			finally
			{
				mysql.close();
			}
		}
		else
		{
			messageText += ", esperamos continuar contando con su valioso apoyo..";
		}

		Boolean success = false;
		{
			String urlStr = Globals.quickChatURL + "/ReplaceMessage";
			URL url = new URL( urlStr );
			
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			
			String jsonInputString 	= "{\"senderID\":\"" + senderID + "\", \"recipiID\":\"" + receiverID + "\", \"messageID\":\"" + messageID + "\", \"messageText\":\"" + messageText + "\" }";
			
			OutputStream 	os 		= con.getOutputStream();
		    byte[] 			input 	= jsonInputString.getBytes("utf-8");
		    os.write(input, 0, input.length);			
			
			Gson	gson	= svdp.general.Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
			
			LinkedTreeMap<String, Object> paramMap;
			try
			{
				paramMap = Util.getParamMap( con.getInputStream(), gson, false );
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				
				throw new IOException( e.getMessage() );
			}
			
			success = (Boolean)paramMap.get("success");
		}
		
		String htmlRespone = "<html>";
		
		if ( success )
		{
			htmlRespone += "<body><p>" + messageText + "</p></body>";
		}
		else
		{
			htmlRespone += "<body><p>No se ha podido ejecutarla el comando por un error interno.</p></body>";
		}
		
		htmlRespone += "</html>";
		
		return htmlRespone;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		PrintWriter writer = response.getWriter();
				
		String messageID 	= request.getParameter("messageID");
		String senderID 	= request.getParameter("senderID");
		String receiverID 	= request.getParameter("receiverID");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
				
		String htmlRespone = reply(messageID, senderID, receiverID, parameterMap);
		
		writer.println(htmlRespone);
	}

}
