package svdp.tcp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.simple.JSONObject;

import svdp.general.Globals;
import svdp.general.Util;


public class DebugServer implements Runnable
{
	private static final String[] 				dirTree 	= { Globals.rootDirectory, Globals.dataBase, "logs" };
	private	static String						actualDate	= null;
	private static SimpleDateFormat 			sdf 		= new SimpleDateFormat( "yyyy-MM-dd");
	private static Logger 						logger		= null;
	public  static LinkedBlockingQueue<String> 	queue 		= new LinkedBlockingQueue<String>();
	public  static PrintWriter 					pr			= null;
	
	private CopyOnWriteArrayList<DebugServer> 	serversArray = null;
	
	private Socket 			connectionSocket	= null;
	@SuppressWarnings("unused")
	private ServerSocket 	listenerSocket		= null;

	private static String directPath = Util.createDirectoryTree( dirTree );
	
	private static String lastError = "";
	
	private boolean quit = false;
	
	public void setQuit()
	{
		quit = true;
	}
	
	public DebugServer()
	{
	}
	
	private static Logger initLogger() 
	{
		Logger logger = Logger.getLogger( DebugServer.class.getName() );
  
		try 
		{
			actualDate = sdf.format(new Date());
			
			FileHandler fh = new FileHandler( directPath + File.separator + actualDate + ".txt");
			
			logger.addHandler(fh);

			SimpleFormatter formatter = new SimpleFormatter();

			fh.setFormatter(formatter);
		} 
		catch (SecurityException e1) 
		{
			e1.printStackTrace();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}

		return logger;
	}

	synchronized public static void printJSON( String title, JSONObject json )
	{
		String str = json.toJSONString();
		
		str = str.replace("{", "{\r\n  ").replace("}", "\r\n}").replace("\",\"", "\"\r\n  \"");

		synchronized ( queue )
		{
			queue.add( "\r\n" + title + "\r\n" + str + "\r\n" );
		}
	}
	
	public static void printException( String text, Exception e ) 
	{
		e.printStackTrace();
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		String[] trace = (sw.toString()).split("at ");
		
		for ( String s : trace )
		{
			println( s.trim() );
		}
	}

	public static void println( String txt )
	{
		System.out.println( txt );
		
		if ( logger == null )
		{
			//logger = initLogger();
		}
		else
		{
			if ( !actualDate.equalsIgnoreCase( sdf.format(new Date()) ) )
			{
				logger = initLogger();
			}
		}
		
		if ( logger != null )
		{
			logger.log( Level.INFO, txt );
		}
		
		if ( pr != null )
		{
			synchronized ( queue )
			{
				queue.add( txt + "\r\n" );
			}
		}
	}
	
	public DebugServer(ServerSocket listenerSocket, Socket connectionSocket, CopyOnWriteArrayList<DebugServer> 	serversArray )
	{
		this.listenerSocket   	= listenerSocket;
		this.connectionSocket 	= connectionSocket;
		this.serversArray 		= serversArray;
	}

	@Override
	public void run()
	{		
		serversArray.add( this );
		
		try
		{
			pr = new PrintWriter( connectionSocket.getOutputStream() );
			
			pr.print("\r\nWellcome to PIP DebugServer V1.07\r\n");
			
			pr.flush();
			
			while ( !quit )
			{
				String txt = queue.poll();
				
				if ( txt == null )
				{
					try
					{
						Thread.sleep(500);
					} catch (InterruptedException e)
					{}
				}
				else
				{
					pr.print(txt);
					pr.flush();
				}
			}
		} 
		catch (IOException e)
		{
			lastError = "IOException:" + e.getMessage(); 
		} 
		
		finally
		{
			serversArray.remove( this );
			
			pr = null;
			
			try
			{
				connectionSocket.close();
			} catch (IOException e)
			{}
		}
	}

	public static void report(PrintWriter w)
	{
		w.println(  "Last Error:" + lastError );
	}


}
