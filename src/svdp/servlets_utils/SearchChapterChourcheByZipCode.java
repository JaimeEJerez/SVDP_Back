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
 * Servlet implementation class SearchShapterByZipCode
 */
@WebServlet("/SearchChapterChourcheByZipCode")
public class SearchChapterChourcheByZipCode extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       	
	private double mmet = 1000000000;
	private String mcid 			= null;

	private int    mActive		 	= 0;
	private String mChapterName 	= null;
	private String mChurcheName 	= null;
	private String mPhoneNumber 	= null;
	private String mStreet 			= null;
	private String mCity 			= null;
	private String mState 			= null;
	private String mZip_Code 		= null;
	private String mWebSite			= null;

	
	private static final double kMaxDist = 11265.4;//7 Miles in meters
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchChapterChourcheByZipCode() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String zipCode = request.getParameter("ZipCode");
		
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
			    
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );

		MySQL mySQL1 = new MySQL();
		
		JSONResponse posP = null;
		
		try
		{
			Vector<String> resultVect1 = lonLatByZip( mySQL1, zipCode );
			
			mmet = 1000000000;
			 
			if ( resultVect1 != null && resultVect1.size() == 2 )
			{
				final double lon1 = Double.valueOf( resultVect1.get( 0 ) );
				final double lat1 = Double.valueOf( resultVect1.get( 1 ) );
	
				String query2 = "SELECT "
								+ "ChapterID, "
								+ "LON,"
								+ "LAT,"
								+ "ChapterName, "
								+ "PhoneNumber, "
								+ "Street, "
								+ "City,"
								+ "State, "
								+ "Zip_Code, "
								+ "WebSite, "
								+ "Active "
								+ "FROM Chapters;";

				mySQL1.callBackQuery( query2, null, new MySQL.QueryCallBack()
				{
					@Override
					public boolean execute( ResultSet rs, int rowCount, int columnNumber, Object linkObj ) throws SQLException, IOException 
					{

						if ( rs.getString( 1 ) != null && rs.getString( 2 ) != null )
						{
							String cid 			= rs.getString( 1 );
							double lon2 		= rs.getDouble( 2 );
							double lat2 		= rs.getDouble( 3 );
							String ChapterName 	= rs.getString( 4 );
							String PhoneNumber 	= rs.getString( 5 );
							String Street 		= rs.getString( 6 );
							String City 		= rs.getString( 7 );
							String State 		= rs.getString( 8 );
							String Zip_Code 	= rs.getString( 9 );
							String WebSite		= rs.getString( 10 );
							int    Active		= rs.getInt( 11 );
							
							double met = calcDist( lat1, lon1, lat2, lon2);
							
							if ( met < mmet )
							{
								mmet 			= met;
								mcid 			= cid;
								mChapterName 	= ChapterName;
								mPhoneNumber 	= PhoneNumber;
								mStreet 		= Street;
								mCity			= City;
								mState 			= State;
								mZip_Code 		= Zip_Code;
								mWebSite		= WebSite;
								mActive			= Active;
							}
						}
						
						return true;
					}
			
				});

				if ( mySQL1.getLastError() != null )
				{
					posP = JSONResponse.not_success(002,  mySQL1.getLastError() );
				}
				else
				if ( mmet < kMaxDist )
				{
					Map<String, String> 		resultMap 	= new HashMap<String, String>();
					
					if ( mActive == 1)
					{
						resultMap.put( "Result", "An active nearby Chapter has been found, you can fill out the help request form." );
					}
					else
					{
						resultMap.put( "Result", "A nearby Chapter has been found, please contact them at the following phone number." );
					}
					
					resultMap.put( "ChapterName", mChapterName );
					resultMap.put( "PhoneNumber", mPhoneNumber );
					resultMap.put( "Street", mStreet );
					resultMap.put( "City", mCity );
					resultMap.put( "State", mState );
					resultMap.put( "Zip_Code", mZip_Code );
					resultMap.put( "WebSite", mWebSite );
					resultMap.put( "ChapterID", mcid );
					resultMap.put( "Active", mActive==1 ? "YES" : "NO" );
					resultMap.put( "FillForm",  mActive==1 ? "YES" : "NO"  );
					
					posP = JSONResponse.success( resultMap );
				}
				else
				{
					String query3 = "SELECT ChurcheID, "
										+ "LON, "
										+ "LAT, "
										+ "ZipCode, "
										+ "Name,"
										+ "Street,"
										+ "City,"
										+ "State,"
										+ "PhoneNumber,"
										+ "WebSite"
										+ " FROM Churches;";

					mySQL1.callBackQuery( query3, null, new MySQL.QueryCallBack()
					{
						@Override
						public boolean execute( ResultSet rs, int rowCount, int columnNumber, Object linkObj ) throws SQLException, IOException 
						{

							if ( rs.getString( 1 ) != null && rs.getString( 2 ) != null )
							{
								String cid 			= rs.getString( 1 );
								double lon2 		= rs.getDouble( 2 );
								double lat2 		= rs.getDouble( 3 );
								String ZipCode 		= rs.getString( 4 );
								String Name 		= rs.getString( 5 );
								String Street 		= rs.getString( 6 );
								String City 		= rs.getString( 7 );
								String State 		= rs.getString( 8 );
								String PhoneNumber 	= rs.getString( 9 );
								String WebSite		= rs.getString( 10 );
								
								double met = calcDist( lat1, lon1, lat2, lon2);
								
								if ( met < mmet )
								{
									mmet 			= met;
									mcid 			= cid;
									mZip_Code 		= ZipCode;
									mChurcheName 	= Name;;
									mStreet			= Street;
									mCity			= City;
									mState			= State;	
									mPhoneNumber	= PhoneNumber;
									mWebSite		= WebSite;
								}
							}
							
							return true;
						}
				
					});
					
					if ( mySQL1.getLastError() != null )
					{
						posP = JSONResponse.not_success(002,  mySQL1.getLastError() );
					}
					else
					if ( mmet < kMaxDist )
					{
						Map<String, String> 		resultMap 	= new HashMap<String, String>();
						
						resultMap.put( "Result", "A nearby Church has been found, please contact them at the following phone number." );
						resultMap.put( "ChurcheName", mChurcheName );
						resultMap.put( "PhoneNumber", mPhoneNumber );
						resultMap.put( "Street", mStreet );
						resultMap.put( "City", mCity );
						resultMap.put( "State", mState );
						resultMap.put( "Zip_Code", mZip_Code );
						resultMap.put( "ChurcheID", mcid );
						resultMap.put( "WebSite", mWebSite );
						resultMap.put( "Active", "NO" );
						resultMap.put( "FillForm", "NO"  );
						
						posP = JSONResponse.success( resultMap );
					}
					else
					{
						Map<String, String> 		resultMap 	= new HashMap<String, String>();
						
						resultMap.put( "Result", "Unfortunately no helping nearby Chapter or Church has been found." );
						
						posP = JSONResponse.success( resultMap );
					}
				}
				
				response.setStatus( HttpServletResponse.SC_OK );
			}
			else
			{
				posP = JSONResponse.not_success(002,  "Zip Code not found" );
			}
		}
		finally
		{
			mySQL1.close();
		}
				
	    gson.toJson( posP, osw );
		
		osw.flush();
	}

	private Vector<String> lonLatByZip( MySQL mySQL1,  String zip )
	{
		String query1 = "SELECT LON, LAT FROM zip_codes WHERE ZIP_CODE=\"" + zip + "\";";

		Vector<String> resultVect = mySQL1.simpleVQuery( query1 );

		return resultVect;
	}
	
	private double calcDist( double lat1, double lon1,  double lat2, double lon2)
	{
		final double R = 6371e3; // metres
		final double φ1 = lat1 * Math.PI/180; // φ, λ in radians
		final double φ2 = lat2 * Math.PI/180;
		final double Δφ = (lat2-lat1) * Math.PI/180;
		final double Δλ = (lon2-lon1) * Math.PI/180;

		double a =	Math.sin(Δφ/2) * Math.sin(Δφ/2) +
			  		Math.cos(φ1) * Math.cos(φ2) *
			  		Math.sin(Δλ/2) * Math.sin(Δλ/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		double d = R * c; // in metres
		
		return d;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{}

}
