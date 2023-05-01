package svdp.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;

import svdp.general.Globals;


public class DebugListener extends Thread
{
	private static final  	int 			port 			= Globals.kDebugListenerPort;
	public  static 			DebugListener 	tcpListener		= null;
	
	public static 			String			lastError 		= "";
	public static 			boolean			running 		= false;
	public static 			ServerSocket 	socket			= null;
		
	private static			CopyOnWriteArrayList<DebugServer> 	serversArray = new CopyOnWriteArrayList<DebugServer>();
	
	public void run()
	{
		try
		{
			socket = new ServerSocket( port );
				
			running = true;
			
			while(true)
			{
				Socket connectionSocket = socket.accept();
					
				( new Thread( new DebugServer( socket, connectionSocket, serversArray ) ) ).start();
			}
		}
		catch (IOException e)
		{			
			lastError = "TCPListener IOException:" + e.getMessage();
		}
		
		finally
		{
			running = false;
		}
	}

	public static String doStop()
	{
		String result = "INVALID";
		
		if ( tcpListener == null )
		{
			return "tcpListener == null";
		}
		
		try
		{			
			tcpListener.close();
		} 
		catch (UnknownHostException e)
		{
			result = "TCPListener UnknownHostException " + e.getMessage();
		} 
		catch ( SocketException e )
		{
			result = "TCPListener SocketException " + e.getMessage();
		}
		catch (IOException e)
		{
			result = "TCPListener IOException " + e.getMessage();
		}
				
		finally
		{
			running = false;
			
			for ( DebugServer s : serversArray )
			{
				s.setQuit();
			}
		}
		
		return result;
	}
	
	public static String doStart()
	{		
		if ( !running )
		{
			tcpListener = new DebugListener();
			 		
			tcpListener.start();
			
			for ( int i=0; i<15 && !running; i++ )
			{
				if ( lastError != null )
				{
					return lastError;
				}
				
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{}
			}
		}
		
		return running ? "The server is executing." : "Warnning: the server is NOT executing.";
	}

	public void close() throws IOException
	{
		if ( socket != null )
		{
			socket.close();
			socket = null;
		}
		
		lastError = null;
	}
	
	public static String getLastError()
	{
		return lastError;
	}

	public static void report(PrintWriter w)
	{
		w.println( "\r\nDebugListener" );
		
		w.println( "LastError:" + lastError );
		
		w.println( "Is running:" + running );
		
		w.println( "Listenning in port:" + socket.getLocalPort() );
		
		w.println(  serversArray.size() + " debug server(s) running..." );
		
		DebugServer.report( w );
	}
}
