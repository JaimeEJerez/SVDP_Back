package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

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
 * Servlet implementation class ResendMail2NonValidatedHHHeads
 */
@WebServlet("/ResendMail2NonValidatedHHHeads")
public class ResendMail2NonValidatedHHHeads extends UHttpServlet
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResendMail2NonValidatedHHHeads() 
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

    private boolean sendMail( MySQL mySQL, String eMail, String houseHoldHeadID ) throws IOException, MessagingException
    {
		String 	shortCode 	= generateCode();
		UUID 	uuid 		= UUID.randomUUID();

		String command = "INSERT INTO AUTENTICS ( UUID, VALID_COD, EMAIL, HauseholdheadID ) VALUES ( \"" + uuid.toString() + "\",\"" + shortCode + "\",\"" + eMail + "\"," + houseHoldHeadID + " )";
		
		mySQL.executeCommand(command);
		    	
    	if ( mySQL.getLastError() == null )
    	{
			String indexFile = "emails/code_verif_web/index1.html";
			
			HTMLFormer3 html = new HTMLFormer3( this, indexFile );
			
			html.addValue("serverURL", Globals.serverURL );
			html.addValue("uuid", uuid.toString() );
			html.addValue("shortCode", shortCode  );
			html.addValue("ValidationPoint", "ValidateHHHeadCodeWebMode" );	    				
					    				
			String htmlMessage = html.realice2String();	    				

			Util.sendHTMLMail( eMail, "Verificación de correo de cliente. (" + System.currentTimeMillis() + ")", htmlMessage );
    	
			return true;
    	}
    	
    	return false;
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		LinkedTreeMap<String, String> parametersMap = initJSONReponse( request,  response);
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		JSONResponse posP = null;
		
		MySQL mySQL = new MySQL();
		
		try
		{
			if ( !verifyAuthorizationKey( mySQL, request.getHeader( "Token" ) ) )
			{
				gson.toJson( JSONResponse.not_success( 1007, "Invalid or missing Token" ), osw );
				
				mySQL.close();
				
				osw.flush();
				
				return;
			}	

			String 						eMail 		= parametersMap == null ? null : parametersMap.get("eMail");
			
			if ( eMail != null )
			{
				Hashtable<String,String>	resultMap 	= new Hashtable<String,String>();

		    	String 	query1 				= "SELECT HauseholdheadID FROM Hauseholdheads WHERE Validated IS NULL && Email=\"" + eMail + "\"";
		    	String	hauseholdheadID 	= mySQL.simpleQuery(query1);
	
		    	if ( hauseholdheadID != null  )
		    	{
		    	    try
					{
						boolean success = sendMail( mySQL, eMail, hauseholdheadID );
						resultMap.put( eMail, success ? "Success" : "Unsuccess " );
					} 
		    	    catch (IOException | MessagingException e)
					{
						resultMap.put( eMail, e.getMessage() );
						e.printStackTrace();
					}
		    	}
		    	
		    	posP = JSONResponse.success( resultMap );
			}
			else
			{
				Vector<Hashtable<String,String>>	resultVect 	= new Vector<Hashtable<String,String>>();

		    	String 		query 	= "SELECT HauseholdheadID, Email FROM Hauseholdheads WHERE Validated IS NULL;";
		    	String[]	respn	= mySQL.simpleAQuery(query);
		    	
		    	for ( int i=0; i<respn.length; i+=2 )
		    	{
					Hashtable<String,String>	resultMap 	= new Hashtable<String,String>();

	    	    	String hhhID 	= respn[ i ];
	    	    	String email 	= respn[i+1];

		    	    try
					{
		    	    	boolean success = sendMail( mySQL, email, hhhID );
						resultMap.put( email, success ? "Success" : "Unsuccess " );
					} 
		    	    catch (IOException | MessagingException e)
					{
						resultMap.put( email, e.getMessage() );
						e.printStackTrace();
					}
		    	    
		    	    resultVect.add(resultMap);
		    	}
		    	
		    	posP = JSONResponse.success( resultVect );
			}
		} catch (Exception e1)
		{
			posP = JSONResponse.not_success( 002, e1.getMessage() );
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
