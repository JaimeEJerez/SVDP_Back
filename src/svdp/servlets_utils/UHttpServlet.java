package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;
import svdp.general.Util;
import svdp.servlets_auto.SecurityTokens;

public class UHttpServlet  extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected 		Gson 				gson 	= null;
	protected		OutputStreamWriter 	osw 	= null;

	
	protected boolean verifyAuthorizationKey( MySQL mySQL, String token ) throws SQLException
	{
		String result = SecurityTokens.validateToken( mySQL, token );
		
		return result != null && !result.equalsIgnoreCase( SecurityTokens.expiredTime );
	}

	protected LinkedTreeMap<String, String> initJSONReponse(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
		
		gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		LinkedTreeMap<String, String> paramMap = null;
		try
		{
			paramMap = Util.getParamMap( request, gson, true );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
		}

		return paramMap;
	}
}
