package svdp.servlets_utils;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import fast_track.JSONResponse;
import fast_track.MySQL;
import svdp.general.Globals;

/**
 * Servlet implementation class fixChapterCoordens
 */
@WebServlet("/fixChurchesCoordens")
public class fixChurchesCoordens extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
    
	int 			nCountGeoloc 	= 0;
	int 			nFixedGeoloc 	= 0;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public fixChurchesCoordens() 
    {
        super();
    }

	private Vector<String> lonLatByZip( MySQL mySQL,  String zip )
	{
		String query1 = "SELECT LON, LAT FROM zip_codes WHERE ZIP_CODE=\"" + zip + "\";";

		Vector<String> resultVect = mySQL.simpleVQuery( query1 );

		return resultVect;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		nCountGeoloc 	= 0;
		nFixedGeoloc 	= 0;

		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

	
		MySQL mySQL1 = new MySQL();
		MySQL mySQL2 = new MySQL();
		
		
		JSONResponse 	posP			= null;
		
		try
		{
			String query = "SELECT ChurcheID, LON, LAT, ZipCode FROM Churches;";

			mySQL1.callBackQuery( query, null, new MySQL.QueryCallBack()
			{
				@Override
				public boolean execute( ResultSet rs, int rowCount, int columnNumber, Object linkObj ) throws SQLException, IOException 
				{
					String cid = rs.getString( 1 );
					String lon = rs.getString( 2 );
					String lat = rs.getString( 3 );
					String zip = rs.getString( 4 );
					
					nCountGeoloc++;
					
					if ( lon == null || lat == null )
					{
						Vector<String> resultVect2 = lonLatByZip( mySQL2, zip );

						if ( resultVect2 != null && resultVect2.size() == 2 )
						{
							lon = resultVect2.get(0);
							lat = resultVect2.get(1);
							
							String command = "UPDATE Churches SET LON=" + lon + ", LAT=" + lat + " WHERE ChurcheID=" + cid;
							
							mySQL2.executeCommand( command );
							
							nFixedGeoloc++;
						}
					}
					
					return true;
				}
		
			} );
			
			if ( mySQL1.getLastError() != null )
			{
				posP = JSONResponse.not_success(002,  mySQL1.getLastError() );
			}
			else
			{
				Map<String, String> 		resultMap 	= new HashMap<String, String>();
				
				resultMap.put( "nCountGeoloc=", String.valueOf( nCountGeoloc ) );
				resultMap.put( "nFixedGeoloc=", String.valueOf( nFixedGeoloc ) );
				
				posP = JSONResponse.success( resultMap );
			}
		}
		finally
		{
			mySQL1.close();
			mySQL2.close();
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
		doGet(request, response);
	}

}
