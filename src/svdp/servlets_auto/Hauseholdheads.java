package svdp.servlets_auto;

import java.util.Map;
import java.util.UUID;

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
import svdp.servlets_utils.ValidateHHHeadCodeWebMode;

/**
 * Servlet implementation class Hauseholdheads
 */
@WebServlet("/Hauseholdheads")
public class Hauseholdheads extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Hauseholdheads() 
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
	protected boolean useAuthorizationKeyPost()
	{
		return false;
	}

    @Override
	protected void post_PreProcess(MySQL mySQL, LinkedTreeMap<String,Object> paramMap, HttpServletRequest request, HttpServletResponse response)
	{
    	String email 		= (String)paramMap.get( "Email" );
    	String query 		= "SELECT Validated FROM Hauseholdheads WHERE Email=\"" + email + "\"";
    	String validated 	= mySQL.simpleQuery(query);
    	
    	if ( validated == null || validated.isEmpty() )
    	{
        	String command = "DELETE FROM Hauseholdheads WHERE Email=\"" + email + "\"";

        	mySQL.executeCommand( command );
    	}
	}
    
    @Override
	protected JSONResponse post_PosProcess( MySQL mysql, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
    	@SuppressWarnings("unused")
		String userAgent = request.getHeader("User-Agent");
    	
		if ( resultMap != null )
		{
    		String 	email 		= (String)paramMap.get( "Email" );
    	    String 	webMode 	= request.getParameter( "webmode" );
    		
    		if ( email != null )
    		{
    			String shortCode 	= generateCode();
    			String insertedID	= (String)resultMap.get("insertedID");
    			
    			try
				{
					UUID uuid = UUID.randomUUID();
					
					{												
	    				String indexFile = (webMode != null && webMode.equalsIgnoreCase("yes")) ? "emails/code_verif_web/index1.html" : "emails/code_verif_app/index.html";
	    				
	    				HTMLFormer3 html = new HTMLFormer3( this, indexFile );
	    				
	    				html.addValue("serverURL", Globals.serverURL );
	    				html.addValue("shortCode", shortCode  );
	    				html.addValue("uuid", uuid.toString() );
	    				html.addValue("ValidationPoint", "ValidateHHHeadCodeWebMode" );	    				
	    						    				
	    				String htmlMessage = html.realice2String();	    				

						Util.sendHTMLMail( email, "Verificación de correo de cliente. (" + System.currentTimeMillis() + ")", htmlMessage );
					}
					
					String command = "INSERT INTO AUTENTICS ( UUID, VALID_COD, EMAIL, HauseholdheadID ) VALUES ( \"" + uuid.toString() + "\",\"" + shortCode + "\",\"" + email + "\"," + insertedID + " )";
					
					mysql.executeCommand(command);
					
					if ( mysql.getLastError() != null )
					{
						resultMap.put( "SQL Error:" , mysql.getLastError() );
					}
					
					resultMap.put( "uuid_token" , uuid.toString() );
					resultMap.put( "mail_result" ,  "Email sent successfully to " + email );
				} 
    			catch (Exception e)
				{
    				resultMap.put( "Exception" , e.getMessage() );
    				
    				String command = "DELETE FROM Hauseholdheads WHERE HauseholdheadID=" + insertedID ;
    				
    				mysql.executeCommand( command );
				}
    		}

			return JSONResponse.success( resultMap );
		}
		else
		{
			return JSONResponse.not_success( 777, "CERO affected rows");
		}
	}
    
    public static final boolean isYes( String vaule )
    {
    	return vaule != null && vaule.equalsIgnoreCase( "yes" );
    }
	
    
    @SuppressWarnings("unused")
	@Override
	protected JSONResponse put_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap )
	{
    	String 	hauseholdheadID = (String)paramMap.get( "HauseholdheadID" );
		
		{
			if ( hauseholdheadID != null )
			{
				String query = "SELECT "
						+ "AsignedVisitor, Email, Need_job, Need_food, Need_immigration, Need_medical, Need_legal, Need_others "
						+ "FROM Hauseholdheads "
						+ "WHERE HauseholdheadID=" + hauseholdheadID + ";";

				String[] response2 = mySQL.simpleAQuery(query); 
		
				if ( response2 == null || response2.length == 0 )
				{
					return JSONResponse.not_success( 777, "HoseHoldHead :" + hauseholdheadID + " not found.");
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
					
					//Si el pedido es solo de comida no necesata vesita
					boolean onlyNeedFood = Need_food && !Need_job && !Need_immigration && !Need_medical && !Need_legal && !Need_others;
							
					if ( ( asignedVisitor == null || asignedVisitor.isEmpty() ) && !onlyNeedFood )
					{
			    		String result = ValidateHHHeadCodeWebMode.requestVisitor( this, mySQL, hauseholdheadID );

			    		resultMap.put( "requestVisitor", result );
					}
				}
			}
			
			return JSONResponse.success( resultMap );
		}
	}

}
