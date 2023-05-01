package svdp.tcp;


import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import init.InitTables;


public class ContextListener implements ServletContextListener 
{	
	private static String initTablesResult = null;
	
    public void contextInitialized(ServletContextEvent event) 
    {
    	DebugListener.doStart();
    	    	    	
    	StringWriter initTableStringWriter  = new StringWriter();

    	//event.getServletContext().addServlet("hello", test.HelloServlet.class);
    		 
		InitTables.doInitTables( event.getServletContext() , new PrintWriter( initTableStringWriter ) );
		
    	initTablesResult = initTableStringWriter.toString();
    }

   
    
    public void contextDestroyed(ServletContextEvent event) 
    {
    	DebugListener.doStop();
    }
    
    public static String getInitTableStringWriter()
    {
    	return initTablesResult;
    }
}