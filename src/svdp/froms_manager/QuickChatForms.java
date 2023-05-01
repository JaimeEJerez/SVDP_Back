package svdp.froms_manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import svdp.general.Globals;
import svdp.general.HTMLFormer3;
import svdp.general.Util;

/**
 * Servlet implementation class QuickChatForms
 */
@WebServlet("/QuickChatForms")
public class QuickChatForms extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QuickChatForms() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		
		LinkedTreeMap<String,Object> paremMap = null;
		try
		{
			paremMap = Util.getParamMap( request.getInputStream(), gson, false );
			
			@SuppressWarnings("unchecked")
			ArrayList<LinkedTreeMap<String,String>> contacts = (ArrayList<LinkedTreeMap<String,String>>)paremMap.get("contacts");

			String formFile		= (String)paremMap.get("formFile");
			
			if ( contacts != null && formFile != null )
			{
				HTMLFormer3 html = new HTMLFormer3( this, "QuickChatForms/" + formFile );
				
				html.addValue( "serverURL", Globals.serverURL );
				
				if ( paremMap != null )
				{
					 Iterator<String> keySet_iterator = paremMap.keySet().iterator();
					
					while ( keySet_iterator.hasNext() )
					{
						String key = keySet_iterator.next();
						Object val = paremMap.get(key);
						
						if ( val instanceof String )
						{
							html.addValue( key, (String)val );
						}
					}
				}
				
				String htmlMessage = html.realice2String();
				
				response.setContentType("text/html");
				
				response.getWriter().println( htmlMessage );
			}
		} 
		catch (IOException e1)
		{
			response.getWriter().println( "ERROR:" + e1.getMessage() );
			e1.printStackTrace();
			return;
		} 
		catch (Exception e1)
		{
			response.getWriter().println( "ERROR:" + e1.getMessage() );
			e1.printStackTrace();
			return;
		}
	}

}
