package users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import svdp.general.MyCookie;


/**
 * Servlet implementation class Register
 */
@WebServlet("/user/Register")
public class Register extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final String[] VALID_IP_HEADER_CANDIDATES = 
		{ 
		    "X-Forwarded-For",
		    "Proxy-Client-IP",
		    "WL-Proxy-Client-IP",
		    "HTTP_X_FORWARDED_FOR",
		    "HTTP_X_FORWARDED",
		    "HTTP_X_CLUSTER_CLIENT_IP",
		    "HTTP_CLIENT_IP",
		    "HTTP_FORWARDED_FOR",
		    "HTTP_FORWARDED",
		    "HTTP_VIA",
		    "REMOTE_ADDR" 
		 };

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() 
    {
        super();
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

		String 	verifUUID 		= ltm.get("verifUUID");
		String 	nickName  		= ltm.get("nickName");
		String 	password 		= ltm.get("password");
		String 	userAgent 		= ltm.get("User-Agent");
		String 	ipAddress 		= getClientIpAddress( request );
		
		if ( nickName == null || password == null || verifUUID == null  )
		{
			gson.toJson( JSONResponse.not_success( 0, "Error de Parametros de entrada" ), osw ); 
		}
		else
		{
			MySQL mysql = new MySQL();
			
			try
			{
				String email = mysql.simpleQuery( "SELECT CORREO FROM AUTENTIC WHERE UUID='" + verifUUID + "'" );
					
				if ( email == null )
				{				
					gson.toJson( JSONResponse.not_success( 0, "No se pudo completar su registro por error interno #7770" ), osw ); 
				}
				else
				{
					boolean isSupor = email.endsWith( "@yoifirst.com" );
					boolean isAdmin = email.endsWith( ".admin@yoifirst.com" );
		
					if ( mysql.getLastError() != null )
					{
						gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() ), osw ); 
					}
					else
					{
						UUID 	uuid 		= UUID.randomUUID();
						String 	userUUID 	= uuid.toString();
												
						String 	tipo		= "USUARIO";
						
						if ( isSupor )
						{
							tipo = "SOPORTE";
						}
						
						if ( isAdmin )
						{
							tipo = "ADMIN";
						}

						String comand1 =  	"INSERT INTO USUARIOS ( TIPO, CORREO, CLAVE, UUID, APODO, AGENTE_WEB, DIR_IP )"
											+ " VALUES"
											+ " ( \"" + tipo + "\",\"" + email + "\",\"" + password + "\",\""+ userUUID + "\",\"" + nickName + "\",\"" + userAgent + "\",\"" + ipAddress + "\" )";
								
						mysql.executeCommand( comand1 );
						
						if ( mysql.getLastError() != null )
						{
							gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() ), osw ); 
						}	
						else
						{
							Map<String, String> map = new HashMap<String, String>();
						    
						    map.put( "UUID", userUUID );
						    map.put( "CORREO", email );
												    	    
						    gson.toJson( JSONResponse.success( map ), osw );
							
							MyCookie cookie = new MyCookie( MyCookie.CookieNames.USER_UUID, userUUID, 0 );
							
							response.addCookie( cookie );			
						}
					}
				}
			}
			finally
			{
				mysql.close();
			}
		}
		
		osw.flush();
	}
	
	public static String getClientIpAddress(HttpServletRequest request) 
	{
	    for (String header : VALID_IP_HEADER_CANDIDATES) 
	    {
	        String ipAddress = request.getHeader(header);
	        if (ipAddress != null && ipAddress.length() != 0 && !"unknown".equalsIgnoreCase(ipAddress)) 
	        {
	            return ipAddress;
	        }
	    }
	    return request.getRemoteAddr();
	}


}
