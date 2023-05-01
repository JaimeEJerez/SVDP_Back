package svdp.servlets_auto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import fast_track.APIEntryPoint;

/**
 * Servlet implementation class ClientGroups
 */
@WebServlet("/ClientGroups")
public class ClientGroups extends APIEntryPoint  
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientGroups() 
    {
        super();
    }

}
