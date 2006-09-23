/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.httpd;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A class to take care of HTTP Requests.  It parses headers, content, form and url
 * parameters.
 */
public class HttpRequestImpl implements HttpRequest {
    public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String TEXT_XML = "text/xml";
    public static final String MULITPART_FORM_DATA = "multipart/form-data";
    public static final String FILENAME = "filename";
    public static final String NAME = "name";

    /**
     * 5.1   Request-Line
     */
    private String line;
    /**
     * 5.1.1    Method
     */
    private int method;
    /**
     * 5.1.2    Request-URI
     */
    private URI uri;
    /**
     * the headers for this page
     */
    private HashMap headers;
    /**
     * the form parameters for this page
     */
    private HashMap formParams = new HashMap();
    /**
     * the URL (or query) parameters for this page
     */
    private HashMap queryParams = new HashMap();
    private HashMap parameters = new HashMap();

    protected static final String EJBSESSIONID = "EJBSESSIONID";

    private HashMap cookies;


    /**
     * the content of the body of this page
     */
    private byte[] body;
    private InputStream in;
    private int length;
    private String contentType;
    /** the address the request came in on */
    private final URI socketURI;

    private HashMap attributes = new HashMap();

    public HttpRequestImpl(URI socketURI) {
        this.socketURI = socketURI;
    }

    /**
     * Gets a header based the header name passed in.
     *
     * @param name The name of the header to get
     * @return The value of the header
     */
    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    /**
     * Gets a form parameter based on the name passed in.
     *
     * @param name The name of the form parameter to get
     * @return The value of the parameter
     */
    public String getFormParameter(String name) {
        return (String) formParams.get(name);
    }

    public Map getFormParameters() {
        return (Map)formParams.clone();
    }

    public Map getQueryParameters() {
        return (Map)queryParams.clone();
    }

    /**
     * Gets a URL (or query) parameter based on the name passed in.
     *
     * @param name The name of the URL (or query) parameter
     * @return The value of the URL (or query) parameter
     */
    public String getQueryParameter(String name) {
        return (String) queryParams.get(name);
    }

    /**
     * Gets an integer value of the request method.  These values are:
     * <p/>
     * OPTIONS = 0
     * GET     = 1
     * HEAD    = 2
     * POST    = 3
     * PUT     = 4
     * DELETE  = 5
     * TRACE   = 6
     * CONNECT = 7
     * UNSUPPORTED = 8
     *
     * @return The integer value of the method
     */
    public int getMethod() {
        return method;
    }

    /**
     * Gets the URI for the current URL page.
     *
     * @return The URI
     */
    public URI getURI() {
        return uri;
    }

    public int getContentLength() {
        return length;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() throws IOException {
        return this.in;
    }

    /*------------------------------------------------------------*/
    /*  Methods for reading in and parsing a request              */
    /*------------------------------------------------------------*/
    /**
     * parses the request into the 3 different parts, request, headers, and body
     *
     * @param input the data input for this page
     * @throws java.io.IOException if an exception is thrown
     */
    protected void readMessage(InputStream input) throws IOException {
        DataInput in = new DataInputStream(input);

        readRequestLine(in);
        readHeaders(in);
        readBody(in);

        parameters = new HashMap();
        parameters.putAll(this.getFormParameters());
        parameters.putAll(this.getQueryParameters());
    }

    /**
     * reads and parses the request line
     *
     * @param in the input to be read
     * @throws java.io.IOException if an exception is thrown
     */
    private void readRequestLine(DataInput in) throws IOException {
        try {
            line = in.readLine();
//            System.out.println(line);
        } catch (Exception e) {
            throw new IOException("Could not read the HTTP Request Line :"
                    + e.getClass().getName()
                    + " : "
                    + e.getMessage());
        }

        StringTokenizer lineParts = new StringTokenizer(line, " ");
        /* [1] Parse the method */
        parseMethod(lineParts);
        /* [2] Parse the URI */
        parseURI(lineParts);
    }

    /**
     * parses the method for this page
     *
     * @param lineParts a StringTokenizer of the request line
     * @throws java.io.IOException if an exeption is thrown
     */
    private void parseMethod(StringTokenizer lineParts) throws IOException {
        String token = null;
        try {
            token = lineParts.nextToken();
        } catch (Exception e) {
            throw new IOException("Could not parse the HTTP Request Method :"
                    + e.getClass().getName()
                    + " : "
                    + e.getMessage());
        }

        if (token.equalsIgnoreCase("GET")) {
            method = GET;
        } else if (token.equalsIgnoreCase("POST")) {
            method = POST;
        } else {
            method = UNSUPPORTED;
            throw new IOException("Unsupported HTTP Request Method :" + token);
        }
    }

    /**
     * parses the URI into the different parts
     *
     * @param lineParts a StringTokenizer of the URI
     * @throws java.io.IOException if an exeption is thrown
     */
    private void parseURI(StringTokenizer lineParts) throws IOException {
        String token = null;
        try {
            token = lineParts.nextToken();
        } catch (Exception e) {
            throw new IOException("Could not parse the HTTP Request Method :"
                    + e.getClass().getName()
                    + " : "
                    + e.getMessage());
        }

        try {
            uri = new URI(socketURI.toString()+token);
        } catch (URISyntaxException e) {
            throw new IOException("Malformed URI :" + token + " Exception: " + e.getMessage());
        }

        parseQueryParams(uri.getQuery());
    }

    /**
     * parses the URL (or query) parameters
     *
     * @param query the URL (or query) parameters to be parsed
     */
    private void parseQueryParams(String query) {
        if (query == null)
            return;
        StringTokenizer parameters = new StringTokenizer(query, "&");

        while (parameters.hasMoreTokens()) {
            StringTokenizer param = new StringTokenizer(parameters.nextToken(), "=");

            /* [1] Parse the Name */
            if (!param.hasMoreTokens())
                continue;
            String name = URLDecoder.decode(param.nextToken());
            if (name == null)
                continue;

            String value;
            /* [2] Parse the Value */
            if (!param.hasMoreTokens()){
                value = "";
            } else {
                value = URLDecoder.decode(param.nextToken());
            }

            //System.out.println("[] "+name+" = "+value);
            queryParams.put(name, value);
        }
    }

    /**
     * reads the headers from the data input sent from the browser
     *
     * @param in the data input sent from the browser
     * @throws java.io.IOException if an exeption is thrown
     */
    private void readHeaders(DataInput in) throws IOException {
//        System.out.println("\nREQUEST");
        headers = new HashMap();
        while (true) {
            // Header Field
            String hf = null;

            try {
                hf = in.readLine();
                //System.out.println(hf);
            } catch (Exception e) {
                throw new IOException("Could not read the HTTP Request Header Field :"
                        + e.getClass().getName()
                        + " : "
                        + e.getMessage());
            }

            if (hf == null || hf.equals("")) {
                break;
            }

            /* [1] parse the name */
            int colonIndex = hf.indexOf((int) ':');
            String name = hf.substring(0, colonIndex);
            if (name == null)
                break;

            /* [2] Parse the Value */
            String value = hf.substring(colonIndex + 1, hf.length());
            if (value == null)
                break;
            value = value.trim();
            headers.put(name, value);
        }
        
        // Update the URI to be what the client sees the the server as.
        String host = (String) headers.get("Host");
        if( host!=null ) {
            String hostName = uri.getHost();
            int port = uri.getPort();
            int idx = host.indexOf(":");
            if( idx >= 0 ) {
                hostName = host.substring(0, idx);
                try {
                    port = Integer.parseInt(host.substring(idx+1));
                }
                catch (NumberFormatException ignore) {
                }
            } else {
                hostName = host;
            }
            
            try {
                uri = new URI(uri.getScheme(),
                        uri.getUserInfo(), hostName, port,
                        uri.getPath(), uri.getQuery(),
                        uri.getFragment());                
            } catch (URISyntaxException ignore) {
            }            
        }
        
        //temp-debug-------------------------------------------
        //java.util.Iterator myKeys = headers.keySet().iterator();
        //String temp = null;
        //while(myKeys.hasNext()) {
        //    temp = (String)myKeys.next();
        //    System.out.println("Test: " + temp + "=" + headers.get(temp));
        //}
        //end temp-debug---------------------------------------
    }

    /**
     * reads the body from the data input passed in
     *
     * @param in the data input with the body of the page
     * @throws java.io.IOException if an exception is thrown
     */
    private void readBody(DataInput in) throws IOException {
        //System.out.println("Body Length: " + body.length);
        // Content-type: application/x-www-form-urlencoded
        // or multipart/form-data
        length = parseContentLength();

        contentType = getHeader(HttpRequest.HEADER_CONTENT_TYPE);

        if (method == POST && FORM_URL_ENCODED.equals(contentType)) {
            String rawParams = null;

            try {
                byte[] body = new byte[length];
                in.readFully(body);
                rawParams = new String(body);
            } catch (Exception e) {
                throw (IOException)new IOException("Could not read the HTTP Request Body: " + e.getMessage()).initCause(e);
            }

            StringTokenizer parameters = new StringTokenizer(rawParams, "&");
            String name = null;
            String value = null;

            while (parameters.hasMoreTokens()) {
                StringTokenizer param = new StringTokenizer(parameters.nextToken(), "=");

                /* [1] Parse the Name */
                name = URLDecoder.decode(param.nextToken());
                if (name == null)
                    break;

                /* [2] Parse the Value */
                if (param.hasMoreTokens()) {
                    value = URLDecoder.decode(param.nextToken());
                } else {
                    value = ""; //if there is no token set value to blank string
                }

                if (value == null)
                    value = "";

                formParams.put(name, value);
                    //System.out.println(name + ": " + value);
            }
        } else if (method == POST){
            // TODO This really is terrible
            byte[] body = new byte[length];
            in.readFully(body);
            this.in = new ByteArrayInputStream(body);
        } else {
            this.in = new ByteArrayInputStream(new byte[0]);
        }

    }

    private int parseContentLength() {
        // Content-length: 384
        String len = getHeader(HttpRequest.HEADER_CONTENT_LENGTH);
        //System.out.println("readRequestBody Content-Length: " + len);

        int length = -1;
        if (len != null) {
            try {
                length = Integer.parseInt(len);
            } catch (Exception e) {
                //don't care
            }
        }
        return length;
    }

    protected HashMap getCookies() {
        if (cookies != null) return cookies;

        cookies = new HashMap();

        String cookieHeader = getHeader(HEADER_COOKIE);
        if (cookieHeader == null) return cookies;

        StringTokenizer tokens = new StringTokenizer(cookieHeader, ";");
        while (tokens.hasMoreTokens()) {
            StringTokenizer token = new StringTokenizer(tokens.nextToken(), "=");
            String name = token.nextToken();
            String value = token.nextToken();
            cookies.put(name, value);
        }
        return cookies;
    }

    protected String getCookie(String name) {
        return (String) getCookies().get(name);
    }

    public HttpSession getSession(boolean create) {
        return null;
    }

    public HttpSession getSession() {
        return getSession(true);
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value){
        attributes.put(name, value);
    }

    public String getParameter(String name) {
        return (String)parameters.get(name);
    }

    public Map getParameters() {
        return (Map)parameters.clone();
    }
}