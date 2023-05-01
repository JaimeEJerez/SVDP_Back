package svdp.servlets_utils;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.Util;

/**
 * Servlet implementation class ValidateCodeWebMode
 */
@WebServlet("/ValidateVoluntierCodeWebMode")
public class ValidateVoluntierCodeWebMode extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static svdp.general.HTMLFormer3 htmlFormer3;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValidateVoluntierCodeWebMode() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html; charset=UTF-8");
		
		String verifCode 	= request.getParameter( "VALID_COD" );
		String voluntierID 	= request.getParameter( "UUID" );
		
		String eMail		= null;
		
		if ( htmlFormer3 == null )
		{
			htmlFormer3 = new svdp.general.HTMLFormer3( this, "emails/code_verif_web/index2.html" );
      	}
					
		String message = "";
		
    	if ( voluntierID == null || verifCode == null )
    	{

    	}
    	else
    	{
	    	htmlFormer3.addValue( "serverURL", Globals.serverURL );

			MySQL mysql = new MySQL();
				
			try
			{
	    		String query = "SELECT VerifCode FROM Voluntiers WHERE VoluntierID=\"" + voluntierID + "\"";
	
				String VerifCode  = mysql.simpleQuery( query ); 
					
				if ( VerifCode == null || !VerifCode.equalsIgnoreCase(verifCode) )
				{
					message = "Su correo NO ha podido ser</br>verificado exitosament.";
					
					htmlFormer3.addValue( "QRCode", "" );
					
			    	htmlFormer3.addValue( "displayStyle", "display:none" );
				}
				else
				{
					String command = "UPDATE Voluntiers SET VerifCode=\"VERIFD\" WHERE VoluntierID=" + voluntierID;
					
					mysql.executeCommand(command);
				    
				    message = "Su correo ha sido </br>verificado exitosament.";
				    
					String token	= mysql.simpleQuery( "SELECT Token FROM Voluntiers WHERE VoluntierID=" + voluntierID );

			    	htmlFormer3.addValue( "QRCode", token );

			    	htmlFormer3.addValue( "displayStyle", "table-row" );
			    	
				    String query2 = "SELECT Email FROM Voluntiers WHERE VoluntierID=" + voluntierID;

				    eMail = mysql.simpleQuery(query2);
				}
			}
			finally
			{
				mysql.close();
			}
    	}
    	
		htmlFormer3.addValue("message", message );
		
		htmlFormer3.realice( response.getWriter() );
		
		if ( eMail != null)
		{
			String htmlMessage = htmlFormer3.realice2String();	 
			
			try
			{
				Util.sendHTMLMail( eMail, "Verificación de correo de Voluntario exitosa.  (" + System.currentTimeMillis() + ")", htmlMessage );
			} 
			catch (MessagingException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
