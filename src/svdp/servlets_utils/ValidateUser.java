package svdp.servlets_utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
import svdp.general.Util;
import svdp.servlets_auto.SecurityTokens;

/**
 * Servlet implementation class ValidateUser
 */
@WebServlet("/ValidateUser")
public class ValidateUser extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       	

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValidateUser() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson, true );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String user = paramMap.get("user");
		String pass = paramMap.get("pass");
		
		if ( user == null || pass == null )
		{
			gson.toJson( JSONResponse.not_success( 1003, "NULL parameter error" ), osw );
		}
		else 
		{			
			MySQL mySQL = new MySQL();
			
			try
			{			    
				String query1 = "SELECT 'Hauseholdhead'    as Role, "
										+ "Token, "
										+ "HauseholdheadID 	 as ID, "
										+ "ChapterID, "
										+ "CONCAT( Name, \" \", LastName ) AS UserName, "
										+ "(HelpApproveDate <= now() and HelpExpireDate >= now() ) AS ActiveHelp, "
										+ "ChapterID FROM Hauseholdheads "
										+ "WHERE Email=\"" + user + "\" AND Password=\"" + pass + "\" AND VALIDATED IS NOT NULL" ; 
				
				String query2 = "SELECT 'ChapterMember' as Role, "
										+ "Token, "
										+ "AccessType,"
										+ "ChapterMembers.ChapterMemberID as ID, "
										+ "Churches.Name as Chourche, "										
										+ "CONCAT( ChapterMembers.FirstName, \" \", ChapterMembers.LastName ) AS UserName  "
										+ "FROM ChapterMembers INNER JOIN Churches ON ChapterMembers.ChurcheID=Churches.ChurcheID "
										+ "WHERE ChapterMembers.Email=\"" + user + "\" AND ChapterMembers.Password=\"" + pass + "\""; 
				
				String query3 = "SELECT 'Voluntier' as Role, "
										+ "Token, "
										+ "VoluntierID  as ID, "
										+ "ChapterID, "
										+ "CONCAT( FirstName, \" \", LastName ) AS UserName, "
										+ "ChapterID FROM Voluntiers     "
										+ "WHERE Email=\"" + user + "\" AND Password=\"" + pass + "\" AND VerifCode IS NOT NULL";
			
				Vector<Map<String, String>> resultVect = new Vector<Map<String, String>>();
				
				mySQL.simpleHMapQueryAdd(query1, resultVect);
				
				if( mySQL.getLastError() != null )
				{
					gson.toJson( JSONResponse.not_success( 1701,  mySQL.getLastError() ), osw );
				}
				else
				{
					mySQL.simpleHMapQueryAdd(query2, resultVect);
					
					if( mySQL.getLastError() != null )
					{
						gson.toJson( JSONResponse.not_success( 1701,  mySQL.getLastError() ), osw );
					}
					else
					{
						mySQL.simpleHMapQueryAdd(query3, resultVect);

						if( mySQL.getLastError() != null )
						{
							gson.toJson( JSONResponse.not_success( 1701,  mySQL.getLastError() ), osw );
						}
					}
				}
				
				if ( resultVect.size() == 0 )
				{
					Map<String, String> 		resultMap 	= new HashMap<String, String>();

					resultMap.put( "result", "Invalid user or password" );
					
					gson.toJson( JSONResponse.success( resultMap ), osw );
				}
				else
				{
					Map<String, String> 		resultMap 	= new HashMap<String, String>();

					String securityTokens = SecurityTokens.createnToken( mySQL, user );
					
					resultMap.put("SecurityToken", securityTokens);
					
					resultVect.insertElementAt(resultMap, 0);
					
					gson.toJson( JSONResponse.success( resultVect ), osw );
				}
			} 
			catch (Exception e)
			{
				gson.toJson( JSONResponse.not_success( 1701, e.getMessage() ), osw );
				
				e.printStackTrace();
			} 
			finally
			{
				mySQL.close();
			}
		}
		
		osw.flush();
	}

}
