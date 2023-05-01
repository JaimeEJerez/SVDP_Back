package fast_track;


import java.util.Vector;

import fast_track.PreparedStatmentStruct.MODE;

public class APIEntry
{
	public static class ReferenceTable
	{
		public 	boolean visible			= true;
		public 	String	tablenName 		= null;
		public 	String 	columnName 		= null;
		
		public ReferenceTable( String tablenName, String columnName )
		{
			this.tablenName = tablenName;
			this.columnName = columnName;
		}
		
	}
	public class Insert
	{
		public Boolean	doNotUseToken		= null;
		public String[] insert_fields 		= null;
		public String 	generete_uuid 		= null;
		public String[] returns 			= null;
						
		public boolean useToken()
		{
			return doNotUseToken == null || !doNotUseToken;
		}
	}
	
	public class Update
	{
		public Boolean	doNotUseToken		= null;
		public String[] update_fields 		= null;
		public String   update_key			= null;
		
		public boolean useToken()
		{
			return doNotUseToken == null || !doNotUseToken;
		}
	}
	
	public class Select
	{
		public Boolean	doNotUseToken		= null;
		public String[] select_fields 		= null;
		public String[] select_keys 		= null;
		public String	comparator			= "=";
		public String	order				= null;
		
		public boolean useToken()
		{
			return doNotUseToken == null || !doNotUseToken;
		}
	}
	
	public class Delete
	{
		public Boolean	doNotUseToken		= null;
		public String 	delete_key			= null;
		
		public boolean useToken()
		{
			return doNotUseToken == null || !doNotUseToken;
		}
	}
	
	public class Options
	{
		public Boolean	doNotUseToken		= null;
		public String[] options_fields		= null;
		
		public boolean useToken()
		{
			return doNotUseToken == null || !doNotUseToken;
		}
	}

	public String 				apiEntryName			= null;
	public String 				apiUserName				= null;
	public int					neestDeep				= 1;
	public ReferenceTable[] 	foreingTables			= null;
	public String[]				hidenForeingTables		= null;
	
	public Insert 				insert 					= new Insert();
	public Update 				update 					= new Update();
	public Select 				select 					= new Select();
	public Delete 				delete 					= new Delete();
	public Options 				options 				= new Options();
	
	public APIEntry()
	{
		
	}
	
    private static boolean stringInArray( String str, String[] array )
    {
    	if ( array == null )
    	{
    		return false;
    	}
    	
    	for ( String s : array )
    	{
    		if ( s.equalsIgnoreCase( str  ) )
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
	public boolean isForeingKey( String searchString, boolean onlyVisibles ) 
	{		
		if ( foreingTables != null )
		{
			for ( ReferenceTable rt : foreingTables )
			{
				if ( searchString.equalsIgnoreCase( rt.columnName ) )
				{
					return onlyVisibles ? !rt.visible : true;
				}
			}
		}
		
		return false;
	}
	
	public boolean contains( MODE mode, String searchString, boolean onlyVisibles )
	{
		switch ( mode )
		{
			case SELECT:

				if ( isForeingKey( searchString, onlyVisibles ) )
				{
					return false;
				}
				
				return contains( select.select_fields, searchString );
		
			case INSERT:
				
				if ( this.neestDeep > 0 && isForeingKey( searchString, onlyVisibles ) )
				{
					return false;
				}
				
				return contains( insert.insert_fields, searchString );


			case UPDATE:

				if ( isForeingKey( searchString, onlyVisibles ) )
				{
					return false;
				}

				return contains( update.update_fields, searchString );
				
			case OPTIONS:
				if ( isForeingKey( searchString, onlyVisibles ) )
				{
					return false;
				}

				return contains( options.options_fields, searchString );
			case DELETE:
				break;
				
			default:
				break;
		}
		
		return false;
	}
	
	boolean contains( String[] strings, String searchString ) 
	{
		searchString = searchString.toUpperCase();
		
		if ( strings == null || strings.length == 0 )
		{
			return true;
		}
		
		if ( strings[0].trim().toUpperCase().contains( "$ALL" ) )
		{
			for ( String string : strings ) 
		    {
				string = string.trim().toUpperCase();
				
		        if ( string.startsWith( "-" ) && string.substring(1).equals(searchString) )
		        {
		        	return false;
		        }
		    }
		    
			return true;
		}
		else
		{
			for (String string : strings ) 
		    {
		        if (string.trim().toUpperCase().equals(searchString))
		        {
		        	return true;
		        }
		    }
		    
		    return false;		
		}
	}
	
	public ReferenceTable[] getForeingTables()
	{
		return foreingTables;
	}

	public boolean isForeing(String tableName)
	{		
		for ( ReferenceTable s : foreingTables )
		{
			if ( s.tablenName.equalsIgnoreCase(tableName))
			{
				return true;
			}
		}
		
		return false;
	}

	public void init( Vector<ReferenceTable> refTables )
	{		
		int size = refTables == null ? 0 : refTables.size();
		
		if ( size > 0 )
		{
			foreingTables = new ReferenceTable[size];
			
			for ( int i=0; i<size; i++ )
			{
				ReferenceTable foreingTable = refTables.get( i );
				
				Boolean isVisible = !stringInArray( foreingTable.tablenName, hidenForeingTables );

				foreingTables[i] 			= refTables.get(i);
				foreingTables[i].visible 	= isVisible;
			}			
		}
	}

	public boolean isHiddenForeingTable(String tablenName)
	{
		if ( hidenForeingTables != null )
		{
			for( String tableName : hidenForeingTables )
			{
				if ( tableName.equalsIgnoreCase(tablenName) )
				{
					return false;
				}
			}
			
		}
		return false;
	}
		
}
