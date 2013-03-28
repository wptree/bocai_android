
package com.bocai.net;

import java.io.IOException;
import java.io.InputStream;


public interface AsyncHTTPResponseHandler
{

    public abstract void handleError(String errorMsg,InputStream stream, long length)throws IOException;;

    public abstract void handleResponse(AsyncHTTPRequest asyncHttpRequest,InputStream stream, long length) throws IOException;
        
}
