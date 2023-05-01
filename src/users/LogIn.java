package users;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fast_track.JSONResponse;
import fast_track.MySQL;


/**
 * Servlet implementation class LogIn
 */
@WebServlet(
		description = "Acceso", 
		urlPatterns = { "/user/LogIn" }, 
		initParams = { 
				@WebInitParam(name = "email", value = "email", description = "e-Mail"), 
				@WebInitParam(name = "challenge", value = "", description = "Challenge text")
		})

public class LogIn extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogIn() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unused")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String 	email  			= request.getParameter("email");
		String 	password 		= request.getParameter("password");
		String 	challenge 		= request.getParameter("challenge");
		String 	uuid 			= null;
		boolean prettyPrinting 	= request.getParameter("prettyPrinting") != null;
				
		Gson gson =  prettyPrinting ? new GsonBuilder().setPrettyPrinting().create() : new GsonBuilder().create();
		
		String jsonResponse = null;
		
		if ( email == null || password == null )
		{
			jsonResponse = gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ) ); 
		}
		
		if ( jsonResponse == null )
		{
			MySQL mysql = new MySQL();
			
			try
			{
				if ( email != null )
				{				
					uuid = mysql.simpleQuery("SELECT UUID FROM SVDP.USUARIOS WHERE CORREO=\"" + email + "\" AND CLAVE=\"" + password + "\"" ); 
				}
	
				if ( mysql.getLastError() != null )
				{
					jsonResponse = gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() ) ); 
				}
				else
				{
					if ( uuid == null )
					{
						jsonResponse = gson.toJson( JSONResponse.not_success( 0, "Usuario o clave invalidos" ) );	
					}
					else
					{
						jsonResponse = gson.toJson( JSONResponse.success( uuid ) );	
					}
				}
			}
			finally
			{
				mysql.close();
			}
		}
		
	    response.setContentType("application/json");
	    
	    response.setCharacterEncoding("UTF-8");
	    
	    response.getWriter().write(jsonResponse);		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
