package svdp.servlets_auto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.APIEntryPoint;
import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.HTMLFormer3;
import svdp.general.Util;

/**
 * Servlet implementation class Volunteers
 */
@WebServlet("/Voluntiers")
public class Voluntiers extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Voluntiers() 
    {
        super();
    }

    private String generateCode()
    {
    	String retValue = "";
    	
    	for ( int i=0; i<6; i++ )
    	{
    		int n = (int)Math.round( Math.random() * 9 );
    		
    		retValue += String.valueOf( n );
    	}
    	
    	return retValue;
    }

    @Override
	protected void post_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{
    	String email 	= (String)paramMap.get( "Email" );
		String command 	= "DELETE FROM Voluntiers WHERE VerifCode!=\"VERIFD\" AND Email=\"" + email + "\"";
	
		mySQL.executeCommand( command );
	}
    
    @Override
	protected JSONResponse post_PosProcess( MySQL mysql, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
		if ( resultMap != null )
		{
			String voluntierID 	= (String)resultMap.get( "insertedID" );
    		String email 		= (String)paramMap.get( "Email" );
    	    String webMode 		= request.getParameter( "webmode" );
			String shortCode 	= generateCode();
			
			try
			{
				String indexFile = (webMode != null && webMode.equalsIgnoreCase("yes")) ? "emails/code_verif_web/index1.html" : "emails/code_verif_app/index.html";
				
				HTMLFormer3 html = new HTMLFormer3( this, indexFile );
				
				html.addValue("serverURL", Globals.serverURL );
				html.addValue("shortCode", shortCode  );
				html.addValue("uuid", voluntierID );
				html.addValue("ValidationPoint", "ValidateVoluntierCodeWebMode" );	    				
				
				String htmlMessage = html.realice2String();  
				    				    				    				
				Util.sendHTMLMail( email, "Verificación de correo de Voluntario.  (" + System.currentTimeMillis() + ")", htmlMessage );
								
				String command = "UPDATE  Voluntiers SET VerifCode=\"" + shortCode + "\" WHERE VoluntierID=" + voluntierID;
				
				mysql.executeCommand(command);
				
				if ( mysql.getLastError() != null )
				{					
					return JSONResponse.not_success( 1700,  "SQL Error:" + mysql.getLastError() + "\r\n" + command );
				}
				else
				{
					resultMap.put( "mail_result" , "Email sent successfully to " + email );
					
					return JSONResponse.success( resultMap );
				}
			} 
			catch (Exception e)
			{				
				return JSONResponse.not_success( 1091, "Exception" + e.getMessage() );

			}
		}
		else
		{
			return JSONResponse.not_success( 1002, "CERO affected rows");
		}
	}

    @Override
	protected JSONResponse options_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap ) throws SQLException
	{
    	String voluntierID 	= (String)paramMap.get( "VoluntierID" );
    	String verifCode 	= (String)paramMap.get( "VerifCode" );
    	
    	if ( voluntierID == null || verifCode == null )
    	{
    		return JSONResponse.not_success( 1003, "Missing parameters" );
    	}
    	else
    	{
    		String query = "SELECT VerifCode FROM Voluntiers WHERE VoluntierID=\"" + voluntierID + "\"";
    		
    		String result = mySQL.simpleQuery( query );
    		
    		if ( result == null )
    		{    			
				return JSONResponse.not_success( 1701,  "VoluntierID not found." );
    		}
    		else
    		if ( result.equals( "VERIFD" ) )
    		{
    			resultMap.put("Result", "Code already verified" );
    			
				return JSONResponse.success( resultMap );
    		}
    		else
    		if ( result.equals(verifCode) )
    		{
    			resultMap.put("Result", "Success code verification" );
    			
				String command = "UPDATE  Voluntiers SET VerifCode=\"VERIFD\" WHERE VoluntierID=" + voluntierID;
				
				mySQL.executeCommand(command);

				if ( mySQL.getLastError() != null )
				{					
					return JSONResponse.not_success( 1700,  "SQL Error:" + mySQL.getLastError() + "\r\n" + command );
				}
				else
				{				
					HTMLFormer3 html;
					try
					{
						html = new HTMLFormer3( this, "emails/new_voluntier/index.html" );
						
						html.addValue("serverURL", Globals.serverURL );
	    				
						String htmlMessage = html.realice2String();
						    				    				    				
						Util.sendHTMLMail( "info@svdpdoral.org", "Se ha registrado un nuevo voluntario. (" + System.currentTimeMillis() + ")", htmlMessage );

						return JSONResponse.success( resultMap );
					} 
					catch (IOException e)
					{
						e.printStackTrace();
						return JSONResponse.not_success( 1701,  "IOException:" +  e.getMessage() + "\r\n" + command );

					} 
					catch (MessagingException e)
					{
						e.printStackTrace();
						return JSONResponse.not_success( 1702,  "MessagingException:" + e.getMessage() + "\r\n" + command );
					}
				
				}
    		}
    		else
    		{
        		return JSONResponse.not_success( 1004, "Bad verification code" );
    		}
    	}
	}

}
