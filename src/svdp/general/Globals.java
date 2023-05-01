package svdp.general;

import java.io.File;


public class Globals
{
	public  static final  boolean prettyPrinting 	= true;
	
	public static String  dataBase 		= "SVDP2";
	public static String  serverURL		= null;
	public static String  quickChatURL	= null;
	public static String  mysqlUser 	= null;
	public static String  mysqlPass 	= null;
	public static String  rootDirectory = null;
	public static String  mysqlHost 	= "localhost";
	
	public static String  avatarImgsDir = "AvatarPictures";
	public static String  dbasTablesDir = "db_tables";
	
	public static String  superSystemContactKind = "S";
	public static String  superSystemContactType = "Y";
	public static String  superSystemContactID   = "0";
	public static String  superSystemContactCode = "SY00000000";
	public static String  superSystemContactName = "San Vicent de Paul.";
	public static boolean enableVisitorAssignmentMechanism = true;
	
	public static final int 		kTCPReceiveListenerPort 	= 12013;//11013;
	public static final int 		kDebugListenerPort 			= 12014;//11014;
	public static final int 		kTCPSendListenerPort		= 12015;//11015;

	enum LOCATION_MODE
	{
		LOCAL_HOST,
		SVDP_SSL_DEV,
		SVDP_SSL_PROD
	}

	static
	{
		switch ( LOCATION_MODE.SVDP_SSL_DEV )
		{
			case LOCAL_HOST:
					 serverURL 		= "http://10.201.1.66:8080/SVDP";
					 quickChatURL 	= "http://10.201.1.66:8080/QuickChat";
					 //quickChatURL 	= "https://tc.svdp-help.com/QuickChat";
					 mysqlUser 		= "root";
					 mysqlPass 		= "Guacamole77.";
					 rootDirectory 	= "c:\\data\\svdp2";
					 enableVisitorAssignmentMechanism = true;
				break;
			case SVDP_SSL_DEV:
					serverURL 		= "https://tc.svdp-help.com/SVDP";
					quickChatURL 	= "https://tc.svdp-help.com/QuickChat";
					mysqlUser 		= "tomcat";
					mysqlPass 		= "Guacamole77.";
					rootDirectory  	= "/data/svdp2";
					enableVisitorAssignmentMechanism = true;
				break;
			case SVDP_SSL_PROD:
					serverURL 		= "https://tc.svdpguadalupe.org/SVDP";
					quickChatURL 	= "https://tc.svdp-help.com/QuickChat";
					mysqlUser 		= "tomcat";
					mysqlPass 		= "Guacamole77.";
					rootDirectory  	= "/data/svdp2";
					enableVisitorAssignmentMechanism = false;
				break;
		}

		File pipDir = new File(rootDirectory);
		
		if (!pipDir.exists())
		{
			pipDir.mkdir();
		}
	}
}