package svdp.froms_manager.volunteers_invitation;

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
import svdp.general.Util.EventInfo;


/**
 * Servlet implementation class WebViewFormResponse
 */
@WebServlet("/VolunteersInvitation_FormAction")
public class VolunteersInvitation_FormAction extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VolunteersInvitation_FormAction() 
    {
        super();
    }
    
    private String getParameter( Map<String, String[]> parameterMap, String paramName )
    {
    	String[] response 	= parameterMap.get( paramName );
    	
    	if ( response == null || response.length == 0 )
    	{
    		return "";
    	}
    	
    	return response[0];
    }
    
	protected String reply(String messageID, String senderID, String receiverID, Map<String, String[]> parameterMap) throws IOException
	{
		@SuppressWarnings("unused")
		String title 		= getParameter( parameterMap, "title" );
		getParameter( parameterMap, "event_id" );
		String event_name 	= getParameter( parameterMap, "event_name" );
		String response		= getParameter( parameterMap, "response" );
		
		boolean accepted 	= response.equalsIgnoreCase("yes");
		
		String messageText = "Gracias por su amable participación.";
		
		MySQL mysql = new MySQL();
		
		Boolean success = false;
		
		try
		{
			long   voluntID 	= Long.parseLong( receiverID.substring( 2 ), 16 );
			String query 		= "SELECT Email FROM Voluntiers WHERE VoluntierID=" + voluntID ;
			String volunMail 	= mysql.simpleQuery( query );
			
			String state 	= accepted ? "ACCEPTED" : "REJECTED";
			String command 	= "UPDATE VoluntiersInvitedToEvents SET Status=\"" + state + "\" WHERE VoluntierEmail=\"" + volunMail + "\" AND EventName=\"" + event_name + "\";"; 
			mysql.executeCommand(command);
		
			String urlStr = Globals.quickChatURL + "/ReplaceMessage";
			URL url = new URL( urlStr );
			
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			
			String finalMessage = messageText;
			
			if ( accepted )
			{
				EventInfo ee = Util.getEventInfo( mysql, event_name );

				finalMessage = "<center>"
								+ "<H3>Lo esperamos en el evento</H3>" 
								+ "<H4>" + ee.name.toUpperCase() + "</H4>"
								+ "</center>"
								+ "<b>Para :</b>" + ee.type + "<br>"
								+ "<b>Fecha :</b>" + ee.date + "<br>"
								+ "<b>Hora :</b>" + ee.time + "<br>"
								+ "<b>Direccion :</b><br>"
								+ ee.state	+ "<br>"
								+ ee.city	+ "<br>"
								+ ee.street	+ "<br>"
								+ ee.place;
			}
			
			String jsonInputString 	= "{\"senderID\":\"" + senderID + "\", \"recipiID\":\"" + receiverID + "\", \"messageID\":\"" + messageID + "\", \"messageText\":\"" + finalMessage + "\" }";
			
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
		catch (Exception e)
		{
			e.printStackTrace();
			
			throw new IOException( e.getMessage() );
		}
		finally
		{
			mysql.close();
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
