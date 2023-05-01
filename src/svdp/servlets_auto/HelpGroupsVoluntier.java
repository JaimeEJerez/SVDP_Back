package svdp.servlets_auto;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fast_track.APIEntryPoint;
import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class HelpGroupsVoluntier
 */
@WebServlet("/HelpGroupsVoluntier")
public class HelpGroupsVoluntier extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelpGroupsVoluntier() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doOptions(HttpServletRequest, HttpServletResponse)
	 */
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    	    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		String query1 = "SELECT VoluntierEmail FROM HelpGroupsVoluntier;";
		String query2 = "SELECT Email, CONCAT( \"FirstName\", \" \", LastName ) AS Name FROM Voluntiers WHERE VerifCode IS NOT NULL;";

		MySQL mySQL = new MySQL();
				
		try
		{
			String[] response1 			= mySQL.simpleAQuery( query1 );
			String[] response2 			= mySQL.simpleAQuery( query2 );

			Hashtable<String,Boolean> map1 = new Hashtable<String,Boolean>();
			
			
			for ( int i=0; i<response1.length; i++ )
			{
				map1.put( response1[i].toUpperCase(), true );
			}
			
			Vector<Hashtable<String,String>> vctr = new Vector<Hashtable<String,String>>();
			for ( int i=0; i<response2.length; i+=2 )
			{
				if ( !map1.containsKey( response2[i].toUpperCase() ) )
				{
					Hashtable<String,String> map2 = new Hashtable<String,String>();
					map2.put("email", response2[i]);
					map2.put("name", response2[i+1]);
					vctr.add(map2);
				}
				else
				{
					System.out.println( response2[i] );
				}
			}
			
			gson.toJson( JSONResponse.success( vctr ), osw );
			
			response.setStatus( HttpServletResponse.SC_OK );
		}
		catch (Exception e )
		{
			response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			
			gson.toJson( JSONResponse.not_success( 1007, e.getMessage() ), osw );
			
			System.out.println( e.getMessage() );
		}
		finally
		{
			mySQL.close();
		}
					    
	    osw.flush();
	}


}
