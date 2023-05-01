package svdp.servlets_utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.MySQL;
import svdp.froms_manager.vititor_of.Send_VisitorOf_Request;
import svdp.general.Globals;
import svdp.general.HTMLFormer3;
import svdp.general.Util;

/**
 * Servlet implementation class ValidateCodeWebMode
 */
@WebServlet("/ValidateHHHeadCodeWebMode")
public class ValidateHHHeadCodeWebMode extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static svdp.general.HTMLFormer3 htmlFormer3;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValidateHHHeadCodeWebMode() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html; charset=UTF-8");
		
		String shortCode 	= request.getParameter( "VALID_COD" );
		String uuid_token 	= request.getParameter( "UUID" );
		
		if ( htmlFormer3 == null )
		{
			htmlFormer3 = new svdp.general.HTMLFormer3( this, "emails/code_verif_web/index2.html" );
      	}
					
		String message 	= "";
		String eMail 	= null;
		String token	= null;
		
		MySQL mysql = new MySQL();

		try
		{
			String query =  "SELECT HauseholdheadID FROM AUTENTICS WHERE UUID=\"" + uuid_token + "\" AND VALID_COD=" + shortCode;

			String hauseholdheadID  = mysql.simpleQuery( query ); 
			
			if ( mysql.getLastError() != null )
			{
				throw new ServletException( mysql.getLastError() + " " + query );
			}
			
			if ( hauseholdheadID == null )
			{
				message = "Su correo NO ha podido ser</br>verificado exitosament.";
			}
			else
			{
			    String query2 = "SELECT Token, Email FROM Hauseholdheads WHERE HauseholdheadID=" + hauseholdheadID;
	
			    String[] response2 = mysql.simpleAQuery(query2);
			    
			    if ( mysql.getLastError() != null )
			    {
			    	throw new ServletException( mysql.getLastError() + " " + query2 );
			    }
			    
			    if ( response2 == null || response2.length != 2  )
			    {			    	
			    	message = "Su correo NO ha podido ser</br>verificado exitosament. El usuario no se encuantra en BD.";
			    	hauseholdheadID = null;
			    }
	
			    token = response2[0];
			    eMail = response2[1];
	
			    if ( token == null || eMail == null )
			    {			    	
			    	message = "Su correo NO ha podido ser</br>verificado exitosament. El usuario no se encuantra en BD.";
			    	hauseholdheadID = null;
			    }
			}

			if ( hauseholdheadID == null )
			{
				htmlFormer3.addValue( "QRCode", "" );
				
		    	htmlFormer3.addValue( "displayStyle", "display:none" );
			}
			else
			{

			    String command = "UPDATE Hauseholdheads SET Validated=\"Validated\" WHERE HauseholdheadID=" + hauseholdheadID;
			    
			    mysql.executeCommand( command );
			    
			    message = "Su correo ha sido</br>verificado exitosament.";
			    			    
		    	htmlFormer3.addValue( "QRCode", token );
		    	htmlFormer3.addValue( "serverURL", Globals.serverURL );
		    	htmlFormer3.addValue( "displayStyle", "table-row" );
		    	
		    	//String requestVisitorResult = Globals.enableVisitorAssignmentMechanism ? requestVisitor( this, mysql, hauseholdheadID  ) : "nvr";
		    	
		    	//message += ("</br>" + requestVisitorResult );
			}
		}
		finally
		{
			mysql.close();
		}
	    
		htmlFormer3.addValue("message", message );
		
		htmlFormer3.realice( response.getWriter() );
		
		//public static void sendHTMLMail(String to, String subject, String htmlContent, final String user, final String pass) throws MessagingException

		if ( eMail != null)
		{
			String htmlMessage = htmlFormer3.realice2String();	 
			
			try
			{
				Util.sendHTMLMail( eMail, "Verificación de correo de Cliente exitosa.  (" + System.currentTimeMillis() + ")", htmlMessage );
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
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson, true );
		} 
		catch (IOException e1)
		{
			throw new ServletException( e1.getMessage() );
		} 
		catch (Exception e1)
		{
			throw new ServletException( e1.getMessage() );
		}
		
		String shortCode 	= paramMap.get( "VALID_COD" );
		String uuid_token 	= paramMap.get( "UUID" );

		response.setContentType("text/html; charset=UTF-8");
				
		if ( htmlFormer3 == null )
		{
			htmlFormer3 = new svdp.general.HTMLFormer3( this, "emails/code_verif_web/index2.html" );
      	}
					
		String message = "";
		
		MySQL mysql = new MySQL();

		try
		{
			String query =  "SELECT HauseholdheadID FROM AUTENTICS WHERE UUID=\"" + uuid_token + "\" AND VALID_COD=" + shortCode;

			String hauseholdheadID  = mysql.simpleQuery( query ); 
				
			if ( hauseholdheadID == null )
			{
				message = "Su correo NO ha podido ser</br>verificado exitosament.";
				
				htmlFormer3.addValue( "QRCode", "" );
				
		    	htmlFormer3.addValue( "displayStyle", "display:none" );
			}
			else
			{
			    String command = "UPDATE Hauseholdheads SET Validated=\"Validated\" WHERE HauseholdheadID=" + hauseholdheadID;
			    
			    mysql.executeCommand(command);
			    
			    message = "Su correo ha sido</br>verificado exitosament.";
			    
				String token	= mysql.simpleQuery( "SELECT Token FROM Hauseholdheads WHERE HauseholdheadID=" + hauseholdheadID );

		    	htmlFormer3.addValue( "QRCode", token );

		    	htmlFormer3.addValue( "displayStyle", "table-row" );
		    	
		    	String requestVisitorResult = requestVisitor( this, mysql, hauseholdheadID  );
		    	
		    	message += ("</br>" + requestVisitorResult );
			}
		}
		finally
		{
			mysql.close();
		}
	    
		htmlFormer3.addValue("message", message );
		
		htmlFormer3.realice( response.getWriter() );
	}

	static public LinkedTreeMap<String,String>  getParamMap( HttpServletRequest request, Gson gson ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	int comment = line.lastIndexOf("//");
        	
        	if ( comment >= 0 )
        	{
        		line = line.substring( 0, comment);
        	}
        	
        	jsontxt += line;
        }

		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,String> paramMap = (LinkedTreeMap<String, String>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

	public static String requestVisitor( HttpServlet servlet, MySQL mySQL, String hauseholdheadID  )
	{		 						     	 
		try 
		{
			if ( hauseholdheadID != null )
			{
				HTMLFormer3 html = new HTMLFormer3( servlet, "emails/request_received/index.html" );
				
				html.addValue("serverURL", Globals.serverURL );
						    				
				String htmlMessage = html.realice2String();
				
				/*
				Need_job				ENUM( "yes", "no" ),
				Need_food				ENUM( "yes", "no" ),
				Need_immigration		ENUM( "yes", "no" ),
				Need_medical			ENUM( "yes", "no" ),
				Need_legal				ENUM( "yes", "no" ),
				Need_others				ENUM( "yes", "no" ),
			    Need_others_explain		VARCHAR(1024),
				*/
				
				String query = "SELECT "
								+ "AsignedVisitor, Email, Need_job, Need_food, Need_immigration, Need_medical, Need_legal, Need_others "
								+ "FROM Hauseholdheads "
								+ "WHERE HauseholdheadID=" + hauseholdheadID + ";";

				String[] response2 = mySQL.simpleAQuery(query); 

				if ( response2 == null || response2.length == 0 )
				{
					return "HoseHoldHead :" + hauseholdheadID + " not found.";
				}
											
				if ( response2 != null && response2.length == 8 )
				{					
					int c=0;
					String 		asignedVisitor 		= response2[c++];
					String 		email 				= response2[c++];
					boolean 	Need_job 			= isYes( response2[c++] );
					boolean 	Need_food 			= isYes( response2[c++] );
					boolean 	Need_immigration 	= isYes( response2[c++] );
					boolean 	Need_medical 		= isYes( response2[c++] );
					boolean 	Need_legal 			= isYes( response2[c++] );
					boolean 	Need_others 		= isYes( response2[c++] );

					Util.sendHTMLMail( email, "Pedido de ayuda. (" + System.currentTimeMillis() + ")", htmlMessage );

					//Si el pedido es solo de comida no necesata vesita
					boolean onlyNeedFood = Need_food && !Need_job && !Need_immigration && !Need_medical && !Need_legal && !Need_others;
							
					if ( ( asignedVisitor == null || asignedVisitor.isEmpty() ) && !onlyNeedFood )
					{
						try
						{
							boolean success = (new Send_VisitorOf_Request( Integer.valueOf( hauseholdheadID ) )).action();
						
							if ( success )
							{
								return "Prontamente sera contactado pur un voluntario.";
							}
							else
							{
								return "Error interno en asignacion de voluntario.";
							}
						} 
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
			}
		} 
		catch (MessagingException e) 
		{
			e.printStackTrace();
			
			return "MessagingException:" + e.getMessage();
		} 
		catch (IOException e)
		{
			e.printStackTrace();

			return "IOException:" +  e.getMessage();
		} 
	
		return "...";
	}
	
    public static final boolean isYes( String vaule )
    {
    	return vaule != null && vaule.equalsIgnoreCase( "yes" );
    }

}
