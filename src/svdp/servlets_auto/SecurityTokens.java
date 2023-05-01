package svdp.servlets_auto;

import java.sql.SQLException;
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

/**
 * Servlet implementation class SecurityTokens
 */
@WebServlet("/SecurityTokens")
public class SecurityTokens extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
	public static final String expiredTime = "Expired time";
	
	public static String createnToken( MySQL mySQL, String eMail )
	{
		/*
		     TOKEN              char(36)                         NOT NULL    UNIQUE,
		    EMAIL              varchar(36),
		    WEB_AGENT          varchar(1024),
		    IPADDRESS          varchar(32),
		 */
		UUID token = UUID.randomUUID();
		
		String command = "INSERT INTO SecurityTokens ( TOKEN, EMAIL ) VALUES ( \"" + token + "\",\"" + eMail + "\" )";
		
		mySQL.executeCommand(command);
		
		return token.toString();
	}

	public static String validateToken( MySQL mySQL, String token ) throws SQLException
	{
		if ( token == null )
		{
			return null;
		}
		else
		{    		
			String query = "SELECT TIMEDIFF( NOW(), DATE_TIME ) FROM SecurityTokens WHERE TOKEN=\"" + token + "\"";
			
			String timeDiff = mySQL.simpleQuery( query );
			
			if ( mySQL.getLastError() != null )
			{					
				throw new SQLException( "SQL Error:" + mySQL.getLastError() + "\r\n" + query );
			}
	
			if ( timeDiff == null || timeDiff.isEmpty() )
			{    			
				return "";
			}
			else
			{	
				int 	indx = timeDiff.indexOf( ':' );
				String 	subS = timeDiff.substring(0, indx);
				
				int hours = Integer.valueOf(subS);
				
				if ( hours > 48 )
				{
	    			return expiredTime;
				}
				else
				{
					return timeDiff;
				}
			}
		}
	}
	

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SecurityTokens() 
    {
        super();
    }

    @Override
	protected JSONResponse options_PosProcess( MySQL mySQL, HttpServletRequest request, HttpServletResponse response, Map<String, Object> resultMap, LinkedTreeMap<String,Object> paramMap ) throws SQLException
	{
    	String token = (String)paramMap.get( "TOKEN" );
    	
    	if ( token == null )
    	{
    		return JSONResponse.not_success( 1007, "Missing TOKEN" );
    	}
    	else
    	{
    		String result = validateToken( mySQL, token );
    		
    		if ( result.isEmpty() )
    		{
				return JSONResponse.not_success( 1007,  "Invalid Token" );
    		}
    		
    		if ( result.equalsIgnoreCase( expiredTime ) )
    		{
    			return JSONResponse.not_success( 1008,  "Token expired time\r\n" );
    		}
    		
			resultMap.put("time", result );
			
			return JSONResponse.success( resultMap );
    	}
	}

}
