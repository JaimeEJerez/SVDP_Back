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
import svdp.general.Globals;
import svdp.general.Util;

/**
 * Servlet implementation class SendMail2SVP
 */
@WebServlet("/SendMail2SVP")
public class SendMail2SVP extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendMail2SVP() 
    {
        super();
        // TODO Auto-generated constructor stub
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

		String 	targetMail 		= ltm.get("targetMail");
		String 	mailTitle 		= ltm.get("mailTitle");
		String 	mailBody  		= ltm.get("mailBody");
		
		if ( targetMail == null || mailTitle == null || mailBody == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			try 
			{
				Util.sendMail( targetMail, mailTitle, mailBody );
				
				Map<String, String> resultMap = new HashMap<String, String>();
				
				resultMap.put( "Mail sent to:", targetMail );

				gson.toJson( JSONResponse.success( resultMap ), osw );
				
			} 
			catch (MessagingException e) 
			{
				gson.toJson( JSONResponse.not_success( 0, e.getMessage() ), osw );
				e.printStackTrace();
			}
		}
		
		osw.flush();
	}

}
