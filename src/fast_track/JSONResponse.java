package fast_track;

import svdp.tcp.DebugServer;

public class JSONResponse
{
    public static class Error
    {
        public int 		code 	= 0;
        public String 	message = null;

        public Error( int code, String message )
        {
            this.code = code;
            this.message = message;
        }
    }

    public boolean 	success = true;
    public Object 	payload = null;
    public Error 	error	= null;

    public JSONResponse( 	boolean success,
                            Object 	payload,
                            Error 	error )
    {
        this.success 	= success;
        this.payload 	= payload;
        this.error 	 	= error;
    }

    public static JSONResponse success( Object payload )
    {
    	DebugServer.println( "JSONResponse.success: " + payload.getClass().getName()  );
    	
        return new JSONResponse( true, payload, null );
    }

    public static JSONResponse not_success( int code, String error )
    {    	
    	if ( error.startsWith("Duplicate entry") )
    	{
    		code = 1771;
    	}
    	
    	DebugServer.println( "JSONResponse.not_success: " + error  );

        return new JSONResponse( false, null, new Error( code, error ) );
    }

}
