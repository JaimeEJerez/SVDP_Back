package svdp.servlets_utils;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class GetPostFromWP
 */
@WebServlet("/GetLastPostURLFromWP")
public class GetLastPostURLFromWP extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetLastPostURLFromWP() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		String query = "SELECT post_name FROM wp_posts WHERE post_status='publish' AND post_type='post' ORDER BY post_date DESC LIMIT 1";
		
		MySQL mySQL = new MySQL( "wordpress" );
		
		JSONResponse posP;
		
		try
		{			
			String post_name = mySQL.simpleQuery( query );
			
			if ( mySQL.getLastError() != null )
			{
				posP = JSONResponse.not_success( 002,  mySQL.getLastError() + "\r\n" + query );
			}
			else
			{
				String url = "http://wp.svdp-help.com/" + post_name + "/";

				Map<String, String> resultMap = new HashMap<String, String>();

				resultMap.put("URL", url);
				
				posP = JSONResponse.success( resultMap );
			}
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
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
