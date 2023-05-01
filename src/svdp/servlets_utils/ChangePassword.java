package svdp.servlets_utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
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
import svdp.general.HTMLFormer3;
import svdp.general.Util;

/**
 * Servlet implementation class ChangePassword
 */
@WebServlet("/ChangePassword")
public class ChangePassword extends HttpServlet 
{
	private static char[] validchars = { '0', '1', '2', '3','4', '5', '6', '7', '8', '9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'G', 'g', 'H', 'h', 'I', 'i', 'J', 'j', 'K', 'k', 'L', 'l', 'M', 'm', 'n', 'O', 'o', 'P', 'p', 'Q', 'q', 'R', 'r', 'M', 'm', 'N', 'n', 'O', 'o', 'P', 'p', 'Q', 'q', 'R', 'r', 'S', 's', 'T', 't', 'U', 'u', 'V', 'v', 'W', 'w', 'X', 'x', 'Y', 'y', 'Z', 'z' }; 
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangePassword() 
    {
        super();
    }
    
    public static boolean is_a_HouseHoldHead( MySQL mySQL, String email )
    {
		String query = "SELECT Validated FROM Hauseholdheads WHERE Email=\"" + email.trim() + "\";";
			
		String result = mySQL.simpleQuery(query);
			
		return result != null;
    }

    public static boolean is_a_Voluntier( MySQL mySQL, String email )
    {
    	String query = "SELECT VerifCode FROM Voluntiers WHERE Email=\"" + email.trim() + "\";";

    	String result = mySQL.simpleQuery(query);
    	
    	return result != null && result.equalsIgnoreCase("VERIFD");
    }
    
    public static boolean is_a_ChapterMember( MySQL mySQL, String email )
    {
    	String query = "SELECT AccessType FROM ChapterMembers WHERE Email=\"" + email.trim() + "\";";
    	
    	String result = mySQL.simpleQuery(query);

    	return  result != null && ( result.equalsIgnoreCase("Admin") ||  result.equalsIgnoreCase("SuperAdmin") );
    }
    
	private String generateTempPassw(byte n)
	{
		String tempPassw = "";
		
		for ( int i=0; i<n; i++)
		{
			int r = (int)Math.round( Math.random() * (validchars.length-1) );
			
			char c = validchars[r];
			
			tempPassw += c;
		}
		
		return tempPassw;
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String email = request.getParameter("email");
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    		
		Gson 				gson 	= new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

	    if ( email == null )
	    {
			gson.toJson( JSONResponse.not_success( 1131, "email parameter is null" ), osw );
			 
			osw.flush();
			 
			return;
	    }

		MySQL mySQL = new MySQL();
		
		JSONResponse posP;
		
		try
		{
			if ( is_a_HouseHoldHead( mySQL, email ) || 
				 is_a_Voluntier( mySQL, email )     ||
				 is_a_ChapterMember( mySQL, email ) )
			{
				String tempPass = generateTempPassw( (byte)8 );

				HTMLFormer3 html = new HTMLFormer3( this, "emails/changeEmail/index.html" );
				
				html.addValue("serverURL", Globals.serverURL );
				html.addValue("tempPass", tempPass );
						    				
				String htmlMessage = html.realice2String();
				    					
				Util.sendHTMLMail( email, "Código temporal.  (" + System.currentTimeMillis() + ")", htmlMessage );

				Map<String, String> resultMap = new HashMap<String, String>();				

				resultMap.put( "mail_result" , "Email sent successfully to " + email );
				
				String command = "INSERT INTO ChangePassword ( Email, Temporal ) VALUES (\"" + email + "\",\"" + tempPass + "\")";
				
				mySQL.executeCommand(command);
				
				posP = JSONResponse.success( resultMap );			
			}
			else
			{
				posP = JSONResponse.not_success( 1141, "No user found with this email:" + email );
			}
		} 
		catch (MessagingException e)
		{
			e.printStackTrace();
			
			posP = JSONResponse.not_success( 777, "MessagingException:" + e.getMessage());
		}
		finally
		{
			mySQL.close();
		}
		
		response.setStatus( HttpServletResponse.SC_OK );
		
	    gson.toJson( posP, osw );
		
		osw.flush();
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
		{
	
		response.setContentType("application/json");
		response.setStatus( HttpServletResponse.SC_OK );
		response.setCharacterEncoding("UTF-8");
	
		Gson 				gson 	=  Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
	
		String parameters = "";
	
		String         line;
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
		while ((line = br.readLine()) != null)
		{
			parameters += line;
		}
	
		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,String> ltm = (LinkedTreeMap<String, String>)gson.fromJson( parameters, LinkedTreeMap.class );
	
		String 	email 			= ltm.get("email");
		String 	tempCode 		= ltm.get("tempCode");
		String 	newPassword  	= ltm.get("newPassword");
	
		if ( email == null || tempCode == null || newPassword == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try 
			{
				String query = "SELECT TIMEDIFF( NOW(), DATE_TIME ) "
						+ "FROM ChangePassword "
						+ "WHERE Email=\"" + email + "\" AND Temporal=\"" + tempCode + "\"";
				
				String timeDiff = mySQL.simpleQuery( query );
								
				if ( timeDiff == null || timeDiff.isEmpty() )
				{    			
					posP = JSONResponse.not_success( 1151, "No ChangePassword register found for this email:" + email + " and temporal code:" + tempCode  );			
				}
				else
				{	
					int 	indx = timeDiff.indexOf( ':' );
					String 	subS = timeDiff.substring(0, indx);
					
					int hours = Integer.valueOf(subS);
					
					if ( hours > 0 )
					{
						posP = JSONResponse.not_success( 1002, "Temporal code in ChangePassword register is expired."  );			
					}
					else
					{
						int count = 0;
						
						if (is_a_HouseHoldHead( mySQL, email ) )
						{
							String command = "UPDATE Hauseholdheads SET Password=\"" + newPassword + "\" WHERE Email=\"" + email + "\"";
							
							mySQL.executeCommand(command);
							
							count++;
						}
						
						if (is_a_Voluntier( mySQL, email ) )
						{
							String command = "UPDATE Voluntiers SET Password=\"" + newPassword + "\" WHERE Email=\"" + email + "\"";
							
							mySQL.executeCommand(command);
							
							count++;
						}
	
						if (is_a_ChapterMember( mySQL, email ) )
						{
							String command = "UPDATE ChapterMembers SET Password=\"" + newPassword + "\" WHERE Email=\"" + email + "\"";
							
							mySQL.executeCommand(command);
							
							count++;
						}
						
						if ( count > 0 )
						{
							posP = JSONResponse.success( "The password was successfully updated." );
						}
						else
						{
							posP = JSONResponse.not_success( 1161, "No user found with this email:" + email );
						}
					}
				}
				
			} 
			catch (Exception e) 
			{	
				posP = JSONResponse.not_success( 0, e.getMessage() );
				
				e.printStackTrace();
			}
			finally
			{
				mySQL.close();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
			
			osw.flush();
	
		}
		}
}
