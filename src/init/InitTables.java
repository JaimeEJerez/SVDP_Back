package init;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fast_track.APIEntry;
import fast_track.MySQL;
import fast_track.PreparedStatmentStruct;
import fast_track.APIEntry.ReferenceTable;
import fast_track.PreparedStatmentStruct.MODE;
import svdp.general.Globals;
import svdp.tcp.ContextListener;


/**
 * Iicializa las table de la base de datos
 */
@WebServlet("/InitTables")
public class InitTables extends HttpServlet 
{
	public static class ExternalReferences
	{
		public Vector<ReferenceTable> refTables = null;
		
		public ExternalReferences( Vector<ReferenceTable> 	refTables )
		{
			this.refTables = refTables;
		}
	}
	
	public static class TableData
	{
		public String				tableName	= null;
		public String 				tableCreate = null;
		public APIEntry 			apiEntry 	= null;	
		
		public TableData( 	String				tableName,
							String 				tableCreate, 
							APIEntry 			apiEntry )
		{
			this.tableName 		= tableName;
			this.tableCreate 	= tableCreate;
			this.apiEntry 		= apiEntry;
		}

		public boolean allDependeciesInVector( Vector<TableData> dependenciesVect )
		{			
			if ( apiEntry.foreingTables == null || apiEntry.foreingTables.length == 0 )
			{
				return true;
			}
			
			int count = 0;
			
			for ( ReferenceTable foreingTable : apiEntry.foreingTables )
			{				
				for ( TableData td : dependenciesVect )
				{
					if ( td.tableName.equalsIgnoreCase( foreingTable.tablenName ) )
					{
						count++;
					}
				}
			}
			
			
			return count == apiEntry.foreingTables.length;
		}

	};
	
	private static final long serialVersionUID = 1L;
    		
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InitTables() 
    {
        super();
    }

    public static boolean createTable( PrintWriter printWrtr, MySQL mysql, String tableName, String command )
    {				
		String result1 = mysql.simpleQuery( "SHOW TABLES LIKE '" + tableName + "'" );
		
		if ( result1 != null && result1.equalsIgnoreCase( tableName) )
		{
			if ( printWrtr != null )
			{
				printWrtr.append( "La tabla '" + tableName + "' ya  existe.\r\n" );
			}
			
			return true;
		}
		else
		{
			mysql.executeCommand(command);
			
			if ( mysql.getLastError() != null )
			{	
				if ( printWrtr != null )
				{
					printWrtr.append( "Error interno:" + mysql.getLastError() + "\r\n" );
				}
				
				return false;
			}
			
			if ( printWrtr != null )
			{
				printWrtr.append( "La tabla '" + tableName + "' fué creada.\r\n" );
			}
		}
		
		return true;
    }
   
    public static void doInitTables( ServletContext servletContext, PrintWriter printWrtr )
    {
    	Vector<TableData> 		tableDataVect 	= new Vector<TableData>();
    	Vector<ReferenceTable> 	refTables 		= new Vector<ReferenceTable>();
    	
		String path = servletContext.getRealPath( Globals.dbasTablesDir );
		//String path = Globals.rootDirectory + "/" + Globals.dbasTablesDir;
		
		File rootDir = new File( path );

		if (!rootDir.exists())
		{
			rootDir.mkdir();
		}

		MySQL mysql = new MySQL();

		File[] files = rootDir.listFiles();
		
		for ( File f : files )
		{
			if ( f.getName().toLowerCase().endsWith(".sql") ) 
			{
				StringBuilder 	contentBuilder 	= new StringBuilder();
				String			tableName		= null;
				String			tableCreate		= null;
				String 			apiEntryTxt 	= null;
				
		        try (BufferedReader br = new BufferedReader(new FileReader( f ))) 
		        {
		            String sCurrentLine = br.readLine();
		            
		            if ( sCurrentLine != null && sCurrentLine.toUpperCase().startsWith( "CREATE" ) )
		            {
		            	contentBuilder.append(sCurrentLine).append("\n");
		            	
			            String[] split = sCurrentLine.split(" ");
			            
			            if ( split.length >= 3 )
			            {
				            tableName = split[2];
				            
				            while ( (sCurrentLine = br.readLine()) != null ) 
				            {
				            	if ( sCurrentLine.contains( "API_DEF" ) )
				            	{
				            		tableCreate = contentBuilder.toString();	
					            						            	
					            	contentBuilder = new StringBuilder();
				            	}
				            	else
				            	if ( !sCurrentLine.contains( "/*" ) && !sCurrentLine.contains( "*/" ) )
				            	{
				            		contentBuilder.append(sCurrentLine).append("\n");
				            		
				            		//REFERENCES TableName(columnName),
				            		
				            		int indx1 = sCurrentLine.toUpperCase().indexOf("REFERENCES");
				            		int indx2 = sCurrentLine.indexOf("(");
				            		int indx3 = sCurrentLine.indexOf(")");
				            		
				            		if ( indx1 > -1 && indx2 > -1 && indx3 > -1 )
		            				{				            			
				            			String refTable = sCurrentLine.substring( indx1+"REFERENCES".length(), indx2 ).trim();
				            			String refColmn = sCurrentLine.substring( indx2+1, indx3 ).trim();
				            			
				            			refTables.add( new APIEntry.ReferenceTable( refTable, refColmn ) );
		            				}
				            	}
				            }
				            
				            if ( contentBuilder.length() > 0 )
				            {
				            	apiEntryTxt = contentBuilder.toString();	
				            }
			            	
				            if ( apiEntryTxt != null ) 
				            {
				        		final Gson 		gson 		= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
				        		
				        		if ( tableCreate != null )
				        		{
					        		APIEntry 		apiEntry 	= gson.fromJson( apiEntryTxt, APIEntry.class );
					        		
					        		apiEntry.init( refTables );
					        		
					            	TableData tableData = new TableData( tableName, tableCreate, apiEntry );
					            	
					            	tableDataVect.add( tableData );
				        		}
				        		else
				        		{
				        			mysql.executeCommand(apiEntryTxt);
				        		}
				            }
				            
				            refTables.removeAllElements();
			            }
		            }
		            
		        	br.close();
		
		        } 
		        catch (IOException e) 
		        {
		        	printWrtr.println( e.getMessage()  + "\r\nTable:" + tableName );
		        	e.printStackTrace();
		        } 
		        
		     }
		}
						 
		mysql.close();
		
		Vector<TableData> dependencyOrder = new Vector <TableData>();

    	while ( tableDataVect.size() > 0 )
    	{
    		ArrayList<TableData> toRemove = new ArrayList<TableData>();
    		
	    	for ( TableData td : tableDataVect )
	    	{	    		
	    		if ( td.allDependeciesInVector( dependencyOrder ) )
	    		{
	    			dependencyOrder.add( td );
	    			
	    			toRemove.add( td );

	    		}
	    	}
	    	
	    	tableDataVect.removeAll( toRemove );
    	}
    	
    	for ( TableData td : dependencyOrder )
    	{
    		if ( createTable( printWrtr, mysql, td.tableName, td.tableCreate ) )
    		{
	    		try
				{
	    			ExternalReferences externalReferences = new ExternalReferences( refTables );
	    			
					PreparedStatmentStruct.create( servletContext, mysql, td.tableName, td.apiEntry, MODE.INSERT, externalReferences );
		    		PreparedStatmentStruct.create( servletContext, mysql, td.tableName, td.apiEntry, MODE.UPDATE, externalReferences );
		    		PreparedStatmentStruct.create( servletContext, mysql, td.tableName, td.apiEntry, MODE.SELECT, externalReferences );
		    		PreparedStatmentStruct.create( servletContext, mysql, td.tableName, td.apiEntry, MODE.DELETE, externalReferences );
		    		PreparedStatmentStruct.create( servletContext, mysql, td.tableName, td.apiEntry, MODE.OPTIONS,externalReferences );
				} 
	    		catch (KeyException | SQLException | IOException e)
				{
	    			printWrtr.append( "Error interno:" + e.getMessage() + "\r\n" );
					e.printStackTrace();
				}
    		}
    		
    		//String entryName = td.apiEntry.apiEntryName;
    		
    		//servletContext.addServlet( entryName, APIEntryPoint.class).addMapping("/" + entryName );
    		//servletContext.addServlet( "/" + entryName, APIEntryPoint.class);
    	}

		printWrtr.flush();
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		boolean forceReinit = request.getParameter( "forceReinit" ) != null;
		
		PrintWriter printWrtr = response.getWriter();
		
		if ( forceReinit )
		{
			doInitTables( this.getServletContext(), printWrtr );
		}
		
		String txt = ContextListener.getInitTableStringWriter();
		
		printWrtr.print( txt );
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{  
		doGet(request, response);
	}

}
