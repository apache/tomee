/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.openejb.util.ArrayEnumeration;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.*;

/**
 * A class to take care of HTTP Requests.  It parses headers, content, form and url
 * parameters.
 */
public class HttpRequestImpl implements HttpRequest {
    private static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    protected static final String EJBSESSIONID = "EJBSESSIONID";

    /**
     * 5.1.1    Method
     */
    private String method;

    /**
     * 5.1.2    Request-URI
     */
    private URI uri;

    /**
     * the headers for this page
     */
    private final Map<String,String> headers = new HashMap<String,String>();

    /**
     * the form parameters for this page
     */
    private final Map<String,String> formParams = new HashMap<String,String>();

    /**
     * the URL (or query) parameters for this page
     */
    private final Map<String,String> queryParams = new HashMap<String,String>();

    /**
     * All form and query parameters.  Query parameters override form parameters.
     */
    private final Map<String,String> parameters = new HashMap<String,String>();

    private final Map<String, Part> parts = new HashMap<String, Part>();

    /**
     * Cookies sent from the client
     */
    private Map<String,String> cookies;

    /**
     * the content of the body of the request
     */
    private byte[] body;
    private ServletByteArrayIntputStream in;
    private int length;
    private String contentType;

    /**
     * the address the request came in on
     */
    private final URI socketURI;

    /**
     * Request scoped data which is set and used by application code.
     */
    private final Map<String,Object> attributes = new HashMap<String,Object>();

    private String path = "/";
    private Locale locale = Locale.getDefault();
    private HttpSession session;
    private String encoding = "UTF-8";

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
        return headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new ArrayEnumeration(new ArrayList<String>(headers.keySet()));
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return new ArrayEnumeration(Arrays.asList(headers.get(s)));
    }

    @Override
    public int getIntHeader(String s) {
        return Integer.parseInt(s);
    }

    /**
     * Gets a form parameter based on the name passed in.
     *
     * @param name The name of the form parameter to get
     * @return The value of the parameter
     */
    public String getFormParameter(String name) {
        return formParams.get(name);
    }

    public Map<String,String> getFormParameters() {
        return new HashMap<String,String>(formParams);
    }

    public Map<String,String> getQueryParameters() {
        return new HashMap<String,String>(queryParams);
    }

    /**
     * Gets a URL (or query) parameter based on the name passed in.
     *
     * @param name The name of the URL (or query) parameter
     * @return The value of the URL (or query) parameter
     */
    public String getQueryParameter(String name) {
        return queryParams.get(name);
    }

    /**
     * Gets the request method.
     * @return the request method
     */
    public String getMethod() {
        return method;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return parts.get(s);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return parts.values();
    }

    @Override
    public String getPathInfo() {
        return path;
    }

    @Override
    public String getPathTranslated() {
        return path;
    }

    @Override
    public String getQueryString() {
        StringBuilder str = new StringBuilder("");
        for (Map.Entry<String, String> q : queryParams.entrySet()) {
            str.append(q.getKey()).append("=").append(q.getValue()).append("&");
        }
        String out = str.toString();
        if (out.isEmpty()) {
            return out;
        }
        return out.substring(0, out.length() - 1);
    }

    @Override
    public String getRemoteUser() {
        return null; // TODO
    }

    @Override
    public String getRequestedSessionId() {
        return session.getId();
    }

    @Override
    public String getRequestURI() {
        return getURI().toString();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(getRequestURI());
    }

    @Override
    public String getServletPath() {
        return getPathInfo();
    }

    /**
     * Gets the URI for the current URL page.
     *
     * @return the URI
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

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

    public ServletInputStream getInputStream() throws IOException {
        return this.in;
    }

    @Override
    public String getLocalAddr() {
        return getURI().getHost();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return new ArrayEnumeration(Arrays.asList(Locale.getAvailableLocales()));
    }

    @Override
    public String getLocalName() {
        return locale.getLanguage();
    }

    @Override
    public int getLocalPort() {
        return getURI().getPort();
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

        parameters.putAll(this.getFormParameters());
        parameters.putAll(this.getQueryParameters());
    }

    public void print(boolean formatXml) {

        System.out.println("******************* REQUEST ******************");
        System.out.println(method + " "+this.uri);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            System.out.println(entry);
        }
        System.out.println();

        final String text = new String(body);
        if (formatXml && OpenEJBHttpServer.isTextXml(headers)) {
            System.out.println(OpenEJBHttpServer.reformat(text));
        } else {
            System.out.println(text);
        }
        System.out.println("**********************************************");
    }

    /**
     * reads and parses the request line
     *
     * @param in the input to be read
     * @throws java.io.IOException if an exception is thrown
     */
    private void readRequestLine(DataInput in) throws IOException {
        String line;
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
        String token;
        try {
            token = lineParts.nextToken();
        } catch (Exception e) {
            throw new IOException("Could not parse the HTTP Request Method :"
                    + e.getClass().getName()
                    + " : "
                    + e.getMessage());
        }

        if (token.equalsIgnoreCase("GET")) {
            method = Method.GET.name();
        } else if (token.equalsIgnoreCase("POST")) {
            method = Method.POST.name();
        } else if (token.equalsIgnoreCase("PUT")) {
            method = Method.PUT.name();
        } else if (token.equalsIgnoreCase("DELETE")) {
            method = Method.DELETE.name();
        } else if (token.equalsIgnoreCase("HEAD")) {
            method = Method.HEAD.name();
        }  else {
            method = Method.UNSUPPORTED.name();
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
        String token;
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
        while (true) {
            // Header Field
            String hf;

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
        String host = headers.get("Host");
        if( host!=null ) {
            String hostName;
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

    private boolean hasBody() {
        return !method.equals(Method.GET.name()) && !method.equals(Method.DELETE.name());
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

        if (hasBody() && FORM_URL_ENCODED.equals(contentType)) {
            String rawParams;

            try {
                body = readContent(in);
                rawParams = new String(body);
            } catch (Exception e) {
                throw (IOException)new IOException("Could not read the HTTP Request Body: " + e.getMessage()).initCause(e);
            }

            StringTokenizer parameters = new StringTokenizer(rawParams, "&");
            String name;
            String value;

            while (parameters.hasMoreTokens()) {
                StringTokenizer param = new StringTokenizer(parameters.nextToken(), "=");

                /* [1] Parse the Name */
                name = URLDecoder.decode(param.nextToken(), "UTF-8");
                if (name == null)
                    break;

                /* [2] Parse the Value */
                if (param.hasMoreTokens()) {
                    value = URLDecoder.decode(param.nextToken(), "UTF-8");
                } else {
                    value = ""; //if there is no token set value to blank string
                }

                if (value == null)
                    value = "";

                formParams.put(name, value);
                    //System.out.println(name + ": " + value);
            }
        } else if (hasBody() && CHUNKED.equals(headers.get(TRANSFER_ENCODING))) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    // read the size line which is in hex
                    String sizeString = line.split(";", 2)[0];
                    int size = Integer.parseInt(sizeString, 16);

                    // if size is 0 we are done
                    if (size == 0) break;

                    // read the chunk and append to byte array
                    byte[] chunk = new byte[size];
                    in.readFully(chunk);
                    out.write(chunk);

                    // read off the trailing new line characters after the chunk
                    in.readLine();
                }
                body = out.toByteArray();
                this.in = new ServletByteArrayIntputStream(body);
            } catch (Exception e) {
                throw (IOException)new IOException("Unable to read chunked body").initCause(e);
            }
        } else if (hasBody()){
            // TODO This really is terrible
            body = readContent(in);
            this.in = new ServletByteArrayIntputStream(body);
        } else {
            body = new byte[0];
            this.in = new ServletByteArrayIntputStream(body);
        }

        session = new HttpSessionImpl();

    }

    private byte[] readContent(DataInput in) throws IOException {
        if (length >= 0) {
            byte[] body = new byte[length];
            in.readFully(body);
            return body;
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            try {
                boolean atLineStart = true;
                while (true) {
                    byte b = in.readByte();

                    if (b == '\r') {
                        // read the next byte
                        out.write(b);
                        b = in.readByte();
                    }

                    if (b == '\n') {
                        if (atLineStart) {
                            // blank line signals end of data
                            break;
                        }
                        atLineStart = true;
                    } else {
                        atLineStart = false;
                    }
                    out.write(b);
                }
            } catch (EOFException e) {
                // done reading
            }
            byte[] body = out.toByteArray();
            return body;
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

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return true; // TODO?
    }

    @Override
    public String getAuthType() {
        return "BASIC"; // to manage?
    }

    @Override
    public String getContextPath() {
        return path;
    }

    @Override
    public Cookie[] getCookies() {
        if (cookies != null) return toCookies(cookies);

        cookies = new HashMap<String,String>();

        String cookieHeader = getHeader(HEADER_COOKIE);
        if (cookieHeader == null) return toCookies(cookies);

        StringTokenizer tokens = new StringTokenizer(cookieHeader, ";");
        while (tokens.hasMoreTokens()) {
            StringTokenizer token = new StringTokenizer(tokens.nextToken(), "=");
            String name = token.nextToken();
            String value = token.nextToken();
            cookies.put(name, value);
        }
        return toCookies(cookies);
    }

    protected Map<?, ?> getInternalCookies() {
        if (cookies != null) return cookies;

        cookies = new HashMap<String,String>();

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

    private Cookie[] toCookies(Map<String, String> cookies) {
        Cookie[] out = new Cookie[cookies.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            out[i++] = new Cookie(entry.getKey(), entry.getValue());
        }
        return out;
    }

    @Override
    public long getDateHeader(String s) {
        return Long.parseLong(s);
    }

    protected String getCookie(String name) {
        return (String) getInternalCookies().get(name);
    }

    public HttpSession getSession(boolean create) {
        return session;
    }

    protected URI getSocketURI() {
        return socketURI;
    }

    @Override
    public Principal getUserPrincipal() {
        return new UserPrincipal("");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isUserInRole(String s) {
        return true; // TODO?
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        // no-op
    }

    @Override
    public void logout() throws ServletException {
        // no-op
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new ArrayEnumeration(new ArrayList<String>(attributes.keySet()));
    }

    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    public void setAttribute(String name, Object value){
        attributes.put(name, value);
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        encoding = s;
    }

    @Override
    public AsyncContext startAsync() {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return null;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        for (Map.Entry<String, String> p : parameters.entrySet()) {
            params.put(p.getKey(), new String[] { p.getValue() });
        }
        return params;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new ArrayEnumeration(new ArrayList<String>(parameters.keySet()));
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[] { parameters.get(s) };
    }

    @Override
    public String getProtocol() {
        return uri.getScheme();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return path;
    }

    public Map<String,String> getParameters() {
        return new HashMap<String,String>(parameters);
    }

    public String getRemoteAddr() {
        // todo how do we get this value?
        return null;
    }

    @Override
    public String getRemoteHost() {
        return getURI().getHost();
    }

    @Override
    public int getRemotePort() {
        return getURI().getPort();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getScheme() {
        return getURI().getScheme();
    }

    @Override
    public String getServerName() {
        return getURI().getHost();
    }

    @Override
    public int getServerPort() {
        return getURI().getPort();
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false; // TODO?
    }

    @Override
    public void removeAttribute(String s) {
        attributes.remove(s);
    }
}