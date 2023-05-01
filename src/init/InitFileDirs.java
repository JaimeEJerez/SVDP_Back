package init;


import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import svdp.general.Globals;
import svdp.general.Util;


/**
 * Inicializa los directorios internos
 */
@WebServlet("/InitDirs")
public class InitFileDirs extends HttpServlet 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 133242L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public InitFileDirs() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.getWriter().append("InitDirs\r\n");
		
		response.getWriter().append( Globals.rootDirectory );
		
		String[] dirTree =  Globals.rootDirectory.split( File.separator );
		
		for ( String s : dirTree )
		{
			response.getWriter().append( s ).append( "\r\n" );
		}
		
		String directPath = Util.createDirectoryTree( dirTree );
		
		response.getWriter().append("directPath:").append( directPath );
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
