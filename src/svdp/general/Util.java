package svdp.general;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import fast_track.MySQL;
import svdp.tcp.DebugServer;

public class Util
{

	static
	{
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("sun.jnu.encoding", "UTF-8");
	}

	private static final int BUFFER_SIZE = 4096 * 10;

	private static void extractFile(ZipInputStream zipIn, File file) throws IOException
	{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1)
		{
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException
	{
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator))
		{
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	public static void unzip(String zipFilePath, String destDirectory) throws IOException
	{
		File destDir = new File(destDirectory);

		if (!destDir.exists())
		{
			DebugServer.println("destDir.mkdir(): " + destDir.getAbsolutePath());

			destDir.mkdir();
		}

		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));

		ZipEntry entry = zipIn.getNextEntry();

		// iterates over entries in the zip file
		while (entry != null)
		{
			File newFile = newFile(destDir, entry);

			if (!entry.isDirectory())
			{
				// if the entry is a file, extracts it
				extractFile(zipIn, newFile);
			} else
			{
				// if the entry is a directory, make the directory

				if (newFile.exists())
				{
					DebugServer.println("deleteDeep( " + newFile.getAbsolutePath() + ", false )");

					deleteDeep(newFile, false);
				} else
				{
					DebugServer.println("newFile.mkdirs()( " + newFile.getAbsolutePath() + " )");

					if (!newFile.mkdirs())
					{
						throw new IOException("Failed to create directory " + newFile.getAbsolutePath());
					}
				}
			}

			zipIn.closeEntry();

			try
			{
				entry = zipIn.getNextEntry();
			} catch (java.io.IOException e)
			{
				System.err.println(e.getMessage());
			}
		}

		zipIn.close();
	}

	public static void saveBufer(byte[] buff, File folder, String name) throws IOException
	{
		File file = new File(folder, name);

		DebugServer.println(" saveBufer( " + file.getAbsolutePath() + " ) ");

		file.deleteOnExit();

		file.createNewFile();

		DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));

		fos.write(buff);

		fos.close();
	}

	public static byte[] loadBufer(File folder, String name)
	{
		File file = new File(folder, name);

		// DebugServer.println("------------------------------");
		// DebugServer.println( "LoadImage :" + file.getAbsolutePath() );
		// DebugServer.println( "file.exists :" + file.exists() );
		// DebugServer.println( "file.canRead:" + file.canRead() );
		// DebugServer.println( "file.length :" + file.length() );

		if (!file.exists())
		{
			return null;
		}

		byte[] resultBuff = null;

		DataInputStream dis = null;

		try
		{
			dis = new DataInputStream(new FileInputStream(file));

			int buffSize = (int) file.length();

			resultBuff = new byte[buffSize];

			dis.readFully(resultBuff);
		} catch (FileNotFoundException e)
		{
			DebugServer.println("FileNotFoundException:" + e.getMessage());
		} catch (IOException e)
		{
			DebugServer.println("IOException:" + e.getMessage());
		} finally
		{
			if (dis != null)
			{
				try
				{
					dis.close();
				} catch (IOException e)
				{
				}
			}
		}

		return resultBuff;
	}

	public static byte[] loadBuferFromFilePath(String filePath)
	{
		File file = new File(filePath);

		if (!file.exists())
		{
			return null;
		}

		byte[] resultBuff = null;

		DataInputStream dis = null;

		try
		{
			dis = new DataInputStream(new FileInputStream(file));

			int buffSize = (int) file.length();

			resultBuff = new byte[buffSize];

			dis.readFully(resultBuff);
		} catch (FileNotFoundException e)
		{
			DebugServer.println("FileNotFoundException:" + e.getMessage());
		} catch (IOException e)
		{
			DebugServer.println("IOException:" + e.getMessage());
		} finally
		{
			if (dis != null)
			{
				try
				{
					dis.close();
				} catch (IOException e)
				{
				}
			}
		}

		return resultBuff;
	}

	public static byte[] loadBuferFromFile(File file)
	{
		if (!file.exists())
		{
			return null;
		}

		byte[] resultBuff = null;

		DataInputStream dis = null;

		try
		{
			dis = new DataInputStream(new FileInputStream(file));

			int buffSize = (int) file.length();

			resultBuff = new byte[buffSize];

			dis.readFully(resultBuff);
		} catch (FileNotFoundException e)
		{
			DebugServer.println("FileNotFoundException:" + e.getMessage());
		} catch (IOException e)
		{
			DebugServer.println("IOException:" + e.getMessage());
		} finally
		{
			if (dis != null)
			{
				try
				{
					dis.close();
				} catch (IOException e)
				{
				}
			}
		}

		return resultBuff;
	}

	public static String add2Left(String input, int size, char add)
	{
		while (input.length() < size)
		{
			input = add + input;
		}

		return input;
	}

	public static String createDirectoryTree(String[] dirTree)
	{
		String dir = File.separator;

		File f = null;

		for (String d : dirTree)
		{
			dir = dir + d + File.separator;

			f = new File(dir);

			if (!f.exists())
			{
				if (!f.mkdir())
				{
					return null;
				}
			}
		}

		if (dir.startsWith(File.separator))
		{
			dir = dir.substring(1, dir.length());
		}

		return dir;
	}

	public static void deleteDeep(File file, boolean deleteRoot)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();

			for (File f : files)
			{
				if (f.isDirectory())
				{
					deleteDeep(f, true);
				} else
				{
					f.delete();
				}
			}
		}

		if (deleteRoot)
		{
			file.delete();
		}
	}
	
	public static void sendMail(String to, String sub, String txt ) throws MessagingException, UnsupportedEncodingException
	{
        final String username = "biz.decode.dev@gmail.com";
        final String password = "pzzfcloqxsmbnaed";//"Jizqy8-fiqruv-fakriq";
        
        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
		Session session = Session.getInstance(prop, new javax.mail.Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(username, password);
			}
		});
		
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("biz.decode.dev@gmail.com", "SVDP noreply"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress( to ));
        msg.setSubject( sub );
        msg.setText( txt );
        Transport.send(msg);
    }

	public static void sendHTMLMail( String to, String subject, String htmlContent ) throws MessagingException, UnsupportedEncodingException
	{
        final String username = "biz.decode.dev@gmail.com";
        final String password = "pzzfcloqxsmbnaed";
        
        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
		Session session = Session.getInstance(prop, new javax.mail.Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(username, password);
			}
		});
		
        Message msg = new MimeMessage(session);
        msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
        msg.setFrom(new InternetAddress("biz.decode.dev@gmail.com", "SVDP noreply"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress( to ));
        msg.setSubject( subject );
        msg.setContent( htmlContent, "text/html" );
        
        Transport.send(msg);        
	}


	static public LinkedTreeMap<String,Object>  getParamMap( InputStream is, Gson gson, boolean removeComments ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader( is, "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	int comment = removeComments ? line.lastIndexOf("//") : -1;
        	
        	if ( comment >= 0 )
        	{
        		line = line.substring( 0, comment);
        	}
        	
        	jsontxt += line;
        }

		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,Object> paramMap = (LinkedTreeMap<String, Object>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

	
	static public LinkedTreeMap<String,String>  getParamMap( HttpServletRequest request, Gson gson, boolean removeComments ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	int comment = removeComments ? line.lastIndexOf("//") : -1;
        	
        	if ( comment >= 0 )
        	{
        		line = line.substring( 0, comment);
        	}
        	
        	jsontxt += line;
        }

		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,String> paramMap = (LinkedTreeMap<String, String>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

	public static class EventInfo
	{
		public long 	id 			= 0;
		public String 	name 		= "";
		public String 	type 		= null;
		public String	date		= null;
		public String	time		= null;
		public String 	state		= "";
		public String 	city		= "";
		public String 	street		= "";
		public String 	place		= "";
	}
	
	
	public static EventInfo getEventInfo( MySQL mysql, String eventName ) throws Exception
	{
		EventInfo eventInfo = new EventInfo();
		
		String quiry0 = "SELECT EventID, EventName, EventType, EvenDate, StartTime, EventAdressID FROM Events WHERE EventName=\"" + eventName + "\""; 
		
		String[] result0 = mysql.simpleAQuery( quiry0  );

		if ( mysql.getLastError() != null )
		{
			throw new Exception( mysql.getLastError() );
		}
		else
		if ( result0.length == 0 )
		{
			throw new Exception( "Event name:" + eventName + " not found." );
		}
		else
		{
			eventInfo.id 		= Long.valueOf( result0[0] );
			eventInfo.name 		= result0[1];
			eventInfo.type 		= result0[2]==null? "" : result0[2];
			eventInfo.date 		= result0[3];
			eventInfo.time 		= result0[4];
			
			String addressID 	= result0[5];
			
			String query1 = "SELECT State, City,  Street, EventPlace FROM EventAdresses WHERE EventAdressID=" + addressID;
			
			String[] result1 = mysql.simpleAQuery( query1  );

			if ( result1 != null )
			{
				eventInfo.state 	= result1[0] == null ? "" : result1[0];
				eventInfo.city 		= result1[1] == null ? "" : result1[1];
				eventInfo.street 	= result1[2] == null ? "" : result1[2];
				eventInfo.place 	= result1[3] == null ? "" : result1[3];
			}
		}

		return eventInfo;
	}

	public static class HouseHHInfo
	{
		public long 	id 			= 0;
		public String 	name 		= "";
		public int 		age 		= 0;
		public String 	phone 		= "";
		public String 	addrssID 	= "";
		public String 	state		= "";
		public String 	city		= "";
		public String 	street		= "";
		public String 	aptoHab		= "";
	}
	
	public static HouseHHInfo getHHHeadInfo( MySQL mysql, long hhhID )
	{
		HouseHHInfo hhhInfo = new HouseHHInfo();
		
		String quiry0 = "SELECT HauseholdheadID, CONCAT( Name,\" \", LastName ) AS HHName, Age, PhoneNumber, PhisicalAddressID FROM Hauseholdheads WHERE HauseholdheadID=" + hhhID; 
		
		String[] result0 = mysql.simpleAQuery( quiry0  );

		if ( mysql.getLastError() != null )
		{
		}
		else
		{
			hhhInfo.id 			= Long.valueOf( result0[0] );
			hhhInfo.name 		= result0[1];
			hhhInfo.age 		= result0[2]==null? 0 : Integer.valueOf(result0[2]);
			hhhInfo.phone 		= result0[3];
			hhhInfo.addrssID 	= result0[4];
				
			String query1 = "SELECT State,City,Street,Apto_Hab FROM PhisicalAddresses WHERE PhisicalAddressID=" + hhhInfo.addrssID;
			
			String[] result1 = mysql.simpleAQuery( query1  );

			if ( result1 != null )
			{
				hhhInfo.state 	= result1[0] == null ? "" : result1[0];
				hhhInfo.city 	= result1[1] == null ? "" : result1[1];
				hhhInfo.street 	= result1[2] == null ? "" : result1[2];
				hhhInfo.aptoHab = result1[3] == null ? "" : result1[3];
			}
		}

		return hhhInfo;
	}

	public static LinkedTreeMap<String, Object> postObjectLikeJSON( String urlTxt, Object obj ) throws Exception
	{
		Gson gson = Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		
		String jsonStr = gson.toJson( obj );
				
		URL url = new URL( urlTxt );
		
		HttpURLConnection con = null;
		
		con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty( "SecurityToken", "602d544c-5219-42dc-8e46-883de0de7613" );
		con.setDoOutput(true);
		
		OutputStream 	os 		= con.getOutputStream();
	    byte[] 			input 	= jsonStr.getBytes("utf-8");
	    os.write(input, 0, input.length);			
							
		LinkedTreeMap<String, Object> paramMap = svdp.general.Util.getParamMap( con.getInputStream(), gson, false );
		
		return paramMap;
	}
}
