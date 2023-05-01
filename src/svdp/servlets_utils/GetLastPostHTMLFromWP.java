package svdp.servlets_utils;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * Servlet implementation class GetPostFromWP
 */

@WebServlet("/GetLastPostHTMLFromWP")
public class GetLastPostHTMLFromWP extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	String htmlText = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetLastPostHTMLFromWP() 
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

		String query = "SELECT post_date, post_title, post_content FROM wp_posts WHERE post_status='publish' AND post_type='post' ORDER BY post_date DESC LIMIT 1";
		
		MySQL mySQL = new MySQL( "wordpress" );
		
		JSONResponse posP = null;
		
		try
		{
			Vector<String> resultvect = htmlText != null ? new Vector<String>() : mySQL.simpleVQuery( query );
			
			if ( htmlText != null )
			{
				resultvect.add( "2020-07-01");
				resultvect.add( "Title");
				resultvect.add( htmlText );
			}
			
			int size = resultvect.size()/3;
			
			int ii = 0;
			
			final  	Vector<Map<String, Object>> resultVect 	= new  Vector<Map<String, Object>>();

			for ( int i=0; i<size; i++ )
			{
				Map<String, Object> resultMap 	= new HashMap<String, Object>();

				String post_date 	= resultvect.get( ii++ );
				String post_title 	= resultvect.get( ii++ );
				String post_content = resultvect.get( ii++ );
				
				resultMap.put( "post_date" , 	post_date );
				resultMap.put( "post_title" , 	post_title );
				
				Document 		document 			= Jsoup.parse(post_content);
				Elements	 	parragraphElements 	= document.body().select("*");

				Iterator<Element> iterator = parragraphElements.iterator();
				
				Vector<String> elemVect = new Vector<String>();
				
				while ( iterator.hasNext() )
				{
					Element element = iterator.next();
					
					String tagName = element.tag().normalName();
					
					if ( tagName.equalsIgnoreCase("p"))
					{
						String text = element.ownText();
						
						if ( text != null && !text.isEmpty() )
						{
							elemVect.add( "<p>" + text + "</p>" );
						}
					}
					if ( tagName.equalsIgnoreCase("img"))
					{
						Elements elmts = element.getElementsByAttribute("src");
						
						if ( elmts != null )
						{
							String src = elmts.attr("src");
							
							if ( src != null )
							{
								src = src.replace("\\", "").replace("\"", "");
								elemVect.add( "<img src=\"" + src + ">\"" );
							}
						}
					}
				}
								
				resultMap.put( "post_elements" , elemVect );
				
				resultVect.add( resultMap );
			}
			
			if ( mySQL.getLastError() != null )
			{
				posP = JSONResponse.not_success( 002,  mySQL.getLastError() );
			}
			else
			{
				posP = JSONResponse.success( resultVect );
			}
		}
		catch (Exception e )
		{
			System.out.println( e.getMessage() );
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
