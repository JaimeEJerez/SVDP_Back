package svdp.servlets_auto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import fast_track.APIEntryPoint;

/**
 * Servlet implementation class Chapters
 */
@WebServlet("/Chapters")
public class Chapters extends APIEntryPoint 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Chapters() 
    {
        super();
    }
}
