package svdp.servlets_utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.internal.LinkedTreeMap;

import fast_track.JSONResponse;
import fast_track.MySQL;

/**
 * Servlet implementation class VerifyHHHeadQR
 */
@WebServlet("/VerifyVoluntierQR")
public class VerifyVoluntierQR extends UHttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VerifyVoluntierQR() 
    {
        super();
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		LinkedTreeMap<String, String> paramMap = initJSONReponse( request, response );
		
		if ( paramMap == null )
		{
			return;
		}
		
		String eventName 		= paramMap.get("EventName").trim();
		String voluntier_Token 	= paramMap.get("Voluntier_Token").trim();
		
		if ( eventName == null || voluntier_Token == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				String query1 = "SELECT Email FROM Voluntiers WHERE VerifCode = \"VERIFD\" AND Token=\"" + voluntier_Token + "\"";

				String voluntierEmail = mySQL.simpleQuery(query1);

				if ( voluntierEmail == null )
				{
					posP = JSONResponse.not_success( 1003, "Voluntier not found." );
				}
				else
				{
					String query2 = "SELECT HelpGroupName FROM Events WHERE EventName=\"" + eventName + "\"";
					
					String helpGroupName1 = mySQL.simpleQuery( query2 );
					
					if ( helpGroupName1 == null )
					{
						posP = JSONResponse.not_success( 003, "Event not found or not HelpGroup assigned." );
					}
					else
					{
						String query3 = "SELECT HelpGroupName FROM HelpGroupsVoluntier WHERE VoluntierEmail=\"" + voluntierEmail + "\";";
								
						String helpGroupName2 = mySQL.simpleQuery( query3 );
						
						if ( !helpGroupName1.equalsIgnoreCase(helpGroupName2) )
						{
							posP = JSONResponse.not_success( 003, "The Voluntier is not invited for this event" );
						}
						else
						{
							posP = JSONResponse.success( "OK" );
						}
						
		       			String command = "UPDATE VoluntiersInvitedToEvents SET Status=\"REALIZED\" WHERE EventName=\"" + eventName + "\" AND VoluntierEmail=\"" + voluntierEmail + "\" )";
		           		
		       			mySQL.executeCommand(command);
					}
				}
			}
			finally
			{
				mySQL.close();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}
	

}
