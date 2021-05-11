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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.part.CommonsFileUploadPartFactory;
import org.apache.openejb.server.httpd.session.SessionManager;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import javax.security.auth.login.LoginException;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * A class to take care of HTTP Requests.  It parses headers, content, form and url
 * parameters.
 */
public class HttpRequestImpl implements HttpRequest {
    private static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";

    public static final Class<?>[] SERVLET_CONTEXT_INTERFACES = new Class<?>[]{ServletContext.class};
    public static final InvocationHandler SERVLET_CONTEXT_HANDLER = (proxy, method, args) -> null;

    private EndWebBeansListener end;
    private BeginWebBeansListener begin;
    private WebContext application;

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
    private final Map<String, List<String>> headers = new HashMap<>();

    /**
     * the form parameters for this page
     */
    private final Map<String, String> formParams = new HashMap<>();

    /**
     * the URL (or query) parameters for this page
     */
    private final Map<String, List<String>> queryParams = new HashMap<>();

    /**
     * All form and query parameters.  Query parameters override form parameters.
     */
    private final Map<String, List<String>> parameters = new HashMap<>();

    private volatile Collection<Part> parts;

    /**
     * Cookies sent from the client
     */
    private Map<String, String> cookies;

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
    private final Map<String, Object> attributes = new HashMap<>();

    private String path = "/";
    private Locale locale = Locale.getDefault();
    private HttpSession session;
    private String encoding = "UTF-8";
    private ServletContext context = null;
    private String contextPath = "";
    private String servletPath = null;
    private Collection<ServletRequestListener> listeners;

    private volatile boolean asyncStarted;
    private boolean noPathInfo;

    public HttpRequestImpl(URI socketURI) {
        this.socketURI = socketURI;
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    /**
     * Gets a header based the header name passed in.
     *
     * @param name The name of the header to get
     * @return The value of the header
     */
    public String getHeader(String name) {
        List<String> strings = headers.get(name);
        return strings == null || strings.isEmpty() ? null : strings.get(0);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new ArrayEnumeration<>(new ArrayList<>(headers.keySet()));
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        final List<String> list = headers.get(s);
        return new ArrayEnumeration<>(list == null ? Collections.emptyList() : list);
    }

    @Override
    public int getIntHeader(final String s) {
        final String header = getHeader(s);
        return header == null ? -1 : Integer.parseInt(header);
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

    public Map<String, String> getFormParameters() {
        return new HashMap<>(formParams);
    }

    /**
     * Gets the request method.
     *
     * @return the request method
     */
    public String getMethod() {
        return method;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        getParts(); // ensure it is initialized
        if (parts != null) {
            for (final Part p : parts) {
                if (s.equals(p.getName())) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException("upgrade not supported");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        if (parts == null) { // assume it is not init
            parts = CommonsFileUploadPartFactory.read(this);
        }
        return parts;
    }

    public void noPathInfo() { // todo: enhance it
        noPathInfo = true;
    }

    @Override
    public String getPathInfo() {
        if (noPathInfo) {
            return null;
        }
        if (servletPath != null) {
            return path.length() < servletPath.length() ? "" : path.substring(servletPath.length());
        }
        return path;
    }

    @Override
    public String getPathTranslated() {
        return path;
    }

    @Override
    public String getQueryString() {
        StringBuilder str = new StringBuilder();
        for (final Map.Entry<String, List<String>> q : queryParams.entrySet()) {
            for (final String v : q.getValue()) {
                str.append(q.getKey()).append("=").append(v).append("&");
            }
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
        if (session != null) {
            return session.getId();
        }
        return null;
    }

    @Override
    public String getRequestURI() {
        return getURI().getRawPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(uri.getScheme() + "://" + uri.getAuthority() + uri.getRawPath());
    }

    @Override
    public String getServletPath() {
        if (servletPath != null) {
            return servletPath;
        }
        if ("/".equals(path) && uri != null && "".equals(contextPath)) { // not initialized, contextpath = "" so let use it for our router (HttpListenerRegistry)
            return uri.getPath();
        }
        return path;
    }

    public void initServletPath(final String servlet) {
        servletPath = servlet;
    }

    public void addQueryParams(final String query) {
        parseQueryParams(query);
        parameters.putAll(queryParams); // a merge would be better
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

    @Override
    public long getContentLengthLong() {
        return getContentLength();
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
        return new ArrayEnumeration<>(Arrays.asList(Locale.getAvailableLocales()));
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
    protected boolean readMessage(InputStream input) throws IOException {
        final DataInput di = new DataInputStream(input);

        if (!readRequestLine(di)) {
            return false;
        }
        readHeaders(di);
        readBody(di);

        for (final Map.Entry<String, String> formParameters : getFormParameters().entrySet()) {
            parameters.put(formParameters.getKey(), singletonList(formParameters.getValue()));
        }
        parameters.putAll(queryParams);

        if (headers.containsKey("Cookie")) {
            final String cookie = getHeader("Cookie");
            if (cookie != null) {
                final String[] cookies = cookie.split(";");
                for (String c : cookies) {
                    final String current = c.trim();
                    if (current.startsWith("EJBSESSIONID=")) {
                        final SessionManager.SessionWrapper sessionWrapper =
                                SystemInstance.get().getComponent(SessionManager.class).findSession(current.substring("EJBSESSIONID=".length()));
                        session = sessionWrapper == null ? null : sessionWrapper.session;
                    } else if (current.startsWith("JSESSIONID=")) {
                        final SessionManager.SessionWrapper sessionWrapper =
                                SystemInstance.get().getComponent(SessionManager.class).findSession(current.substring("JSESSIONID=".length()));
                        session = sessionWrapper == null ? null : sessionWrapper.session;
                    }
                }
            }
        }
        return true;
    }

    public void print(final Logger log, boolean formatXml) {
        if (log.isDebugEnabled()) {
            final StringBuilder builder = new StringBuilder();
            builder.append("******************* REQUEST ******************\n");
            builder.append(method).append(" ").append(uri).append("\n");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                builder.append(entry).append("\n");
            }
            builder.append("\n");

            final String text = new String(body);
            if (formatXml && OpenEJBHttpServer.isTextXml(headers)) {
                builder.append(OpenEJBHttpServer.reformat(text)).append("\n");
            } else {
                builder.append(text).append("\n");
            }
            builder.append("**********************************************").append("\n");
            log.debug(builder.toString());
        }
    }

    /**
     * reads and parses the request line
     *
     * @param in the input to be read
     * @throws java.io.IOException if an exception is thrown
     */
    private boolean readRequestLine(DataInput in) throws IOException {
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
        if (line == null) {
            return false;
        }

        StringTokenizer lineParts = new StringTokenizer(line, " ");
        /* [1] Parse the method */
        parseMethod(lineParts);
        /* [2] Parse the URI */
        parseURI(lineParts);
        return true;
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
        } catch (final Exception e) {
            throw new IOException("Could not parse the HTTP Request Method :"
                + e.getClass().getName()
                + " : "
                + e.getMessage());
        }

        // in JAXRS you can create your own method
        try { // to control the case
            method = Method.valueOf(token.toUpperCase(Locale.ENGLISH)).name();
        } catch (final Exception e) {
            method = token;
        }
        /*
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
        } else if (token.equalsIgnoreCase("OPTIONS")) {
            method = Method.HEAD.name();
        } else if (token.equalsIgnoreCase("PATCH")) {
            method = Method.PATCH.name();
        } else {
            method = Method.UNSUPPORTED.name();
            throw new IOException("Unsupported HTTP Request Method :" + token);
        }
        */
    }

    /**
     * parses the URI into the different parts
     *
     * @param lineParts a StringTokenizer of the URI
     * @throws java.io.IOException if an exeption is thrown
     */
    public void parseURI(StringTokenizer lineParts) throws IOException {
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
            uri = new URI(socketURI.toString() + token.replace("//", "/"));
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
            if (!param.hasMoreTokens()) {
                value = "";
            } else {
                value = URLDecoder.decode(param.nextToken());
            }

            List<String> list = queryParams.get(name);
            if (list == null) {
                list = new LinkedList<>();
                queryParams.put(name, list);
            }
            list.add(value);
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
            final int colonIndex = hf.indexOf((int) ':');
            final String name = hf.substring(0, colonIndex);

            /* [2] Parse the Value */
            final String value = hf.substring(colonIndex + 1, hf.length()).trim();
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<>();
                headers.put(name, values);
            }
            values.add(value);
        }

        // Update the URI to be what the client sees the the server as.
        final String host = getHeader("Host");
        if (host != null) {
            String hostName;
            int port = uri.getPort();
            int idx = host.indexOf(":");
            if (idx >= 0) {
                hostName = host.substring(0, idx);
                try {
                    port = Integer.parseInt(host.substring(idx + 1));
                } catch (NumberFormatException ignore) {
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
        return !method.equals(Method.GET.name()) && !method.equals(Method.DELETE.name())
            && !method.equals(Method.HEAD.name()) && !method.equals(Method.OPTIONS.name());
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

        final boolean hasBody = hasBody();
        if (hasBody && contentType != null && (contentType.startsWith(FORM_URL_ENCODED) || contentType.startsWith(MULTIPART_FORM_DATA))) {
            String rawParams;

            try {
                body = readContent(in);
                this.in = new ServletByteArrayIntputStream(body);
                rawParams = new String(body);
            } catch (Exception e) {
                throw (IOException) new IOException("Could not read the HTTP Request Body: " + e.getMessage()).initCause(e);
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
            }
        } else if (hasBody && CHUNKED.equals(getHeader(TRANSFER_ENCODING))) {
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
                throw (IOException) new IOException("Unable to read chunked body").initCause(e);
            }
        } else if (hasBody) {
            // TODO This really is terrible
            body = readContent(in);
            this.in = new ServletByteArrayIntputStream(body);
        } else {
            body = new byte[0];
            this.in = new ServletByteArrayIntputStream(body);
        }

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
        return contextPath;
    }

    public String extractContextPath() {
        if (SystemInstance.get().getOptions().get("openejb.webservice.old-deployment", false)) {
            return path;
        }

        String uri = getURI().getPath();
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        int idx = uri.indexOf("/");
        if (idx < 0) {
            return uri;
        }
        return uri.substring(0, idx);
    }

    @Override
    public Cookie[] getCookies() {
        if (cookies != null) return toCookies(cookies);

        cookies = new HashMap<>();

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

        cookies = new HashMap<>();

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
        if (session == null && create) {
            long timeout = -1; // default is infinite *here* only
            if (contextPath != null) { // TODO: webapp should be contextual, would need to normalize jaxws, jaxrs, servlet, jsf...before but would be better
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                if (assembler != null) {
                    for (final AppInfo info : assembler.getDeployedApplications()) {
                        for (final WebAppInfo webApp : info.webApps) {
                            if (webApp.contextRoot.replace("/", "").equals(contextPath.replace("/", ""))) {
                                timeout = webApp.sessionTimeout;
                            }
                        }
                    }
                }
            }

            final HttpSessionImpl impl = new HttpSessionImpl(contextPath, timeout) {
                @Override
                public void invalidate() {
                    super.invalidate();
                    HttpRequestImpl.this.session = null;
                }
            };
            session = impl;
            if (begin != null) {
                begin.sessionCreated(new HttpSessionEvent(session));
                session = new SessionInvalidateListener(session, begin);
            }
            impl.callListeners(); // can call req.getSession() so do it after affectation + do it after cdi init

            final SessionManager sessionManager = SystemInstance.get().getComponent(SessionManager.class);
            final SessionManager.SessionWrapper previous = sessionManager.newSession(begin, end, session, application);
            if (previous != null) {
                session = previous.session;
            }
        }
        return session;
    }

    protected URI getSocketURI() {
        return socketURI;
    }

    @Override
    public Principal getUserPrincipal() {
        return SystemInstance.get().getComponent(SecurityService.class).getCallerPrincipal();
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
    public boolean isUserInRole(final String s) {
        return SystemInstance.get().getComponent(SecurityService.class).isCallerInRole(s);
    }

    @Override
    public void login(final String s, final String s1) throws ServletException {
        final SecurityService component = SystemInstance.get().getComponent(SecurityService.class);
        try {
            final Object uuid = component.login(s, s1);
            component.associate(uuid);
        } catch (final LoginException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void logout() throws ServletException {
        final SecurityService component = SystemInstance.get().getComponent(SecurityService.class);
        try {
            final Object disassociate = component.disassociate();
            if (disassociate != null) {
                component.logout(disassociate);
            }
        } catch (final LoginException e) {
            throw new SecurityException(e);
        }
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String changeSessionId() {
        if (session != null) {
            if (HttpSessionImpl.class.isInstance(session)) {
                HttpSessionImpl.class.cast(session).newSessionId();
            }
            return session.getId();
        }
        return null;
    }

    @Override
    public AsyncContext getAsyncContext() {
        setAttribute("openejb_async", "true");
        return new OpenEJBAsyncContext(this /* TODO */, HttpResponse.class.cast(getAttribute("openejb_response")), contextPath);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new ArrayEnumeration<>(new ArrayList<>(attributes.keySet()));
    }

    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        encoding = s;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, HttpResponse.class.cast(getAttribute("openejb_response")));
    }

    @Override // avoids the need of org.apache.openejb.server.httpd.EEFilter in embedded mode
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        setAttribute("openejb_async", "true");
        final OpenEJBAsyncContext asyncContext = new OpenEJBAsyncContext(HttpServletRequest.class.cast(servletRequest) /* TODO */, servletResponse, contextPath);
        asyncContext.internalStartAsync();
        asyncStarted = true;
        final WebBeansContext webBeansContext = AppFinder.findAppContextOrWeb(
                Thread.currentThread().getContextClassLoader(), AppFinder.WebBeansContextTransformer.INSTANCE);
        return webBeansContext != null ?
                new EEFilter.AsynContextWrapper(asyncContext, servletRequest, servletResponse, webBeansContext) : asyncContext;
    }

    public void addInternalParameter(final String key, final String val) {
        parameters.put(key, asList(val));
    }

    public String getParameter(String name) {
        final List<String> strings = parameters.get(name);
        return strings == null ? null : strings.iterator().next();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        final Map<String, String[]> params = new HashMap<>();
        for (final Map.Entry<String, List<String>> p : parameters.entrySet()) {
            final List<String> values = p.getValue();
            params.put(p.getKey(), values.toArray(new String[values.size()]));
        }
        return params;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new ArrayEnumeration<>(new ArrayList<>(parameters.keySet()));
    }

    @Override
    public String[] getParameterValues(final String s) {
        final List<String> strings = parameters.get(s);
        return strings == null ? null : strings.toArray(new String[strings.size()]);
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

    @Deprecated // TODO should be dropped, do we drop axis module as well?
    public Map<String, String> getParameters() {
        final HashMap<String, String> converted = new HashMap<>(parameters.size());
        for (final Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            converted.put(entry.getKey(), entry.getValue().iterator().next());
        }
        return converted;
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
        return new SimpleDispatcher(s);
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
    public ServletContext getServletContext() { // we need a not null value but it is not intended to be used in standalone for now
        if (context == null) {
            context = (ServletContext) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), SERVLET_CONTEXT_INTERFACES, SERVLET_CONTEXT_HANDLER);
        }
        return context;
    }

    @Override
    public boolean isAsyncStarted() {
        return asyncStarted;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false; // TODO?
    }

    @Override
    public void removeAttribute(String s) {
        attributes.remove(s);
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String requestRawPath() {
        try {
            return new URI(getRequestURI()).getRawPath();
        } catch (URISyntaxException e) {
            return getRequestURI();
        }
    }

    public void initPathFromContext(final String context) {
        if (!"/".equals(path) && !"".equals(contextPath)) { // already done
            return;
        }

        final String rawPath = requestRawPath();
        if (context != null) {
            if (context.endsWith("/")) {
                path = rawPath.substring(0, rawPath.length());
                contextPath = "";
            } else {
                path = rawPath.substring(context.length(), rawPath.length());
                contextPath = context;
            }
        }
    }

    public void setEndListener(final EndWebBeansListener end) {
        if (this.end == null) {
            this.end = end;
        }
    }

    public void setApplication(final WebContext app) {
        this.application = app;
    }

    public void setBeginListener(final BeginWebBeansListener begin) {
        if (this.begin == null) {
            this.begin = begin;
        }
    }

    public void init() {
        if (begin != null && getAttribute("openejb_requestInitialized") == null) {
            setAttribute("openejb_requestInitialized", "ok"); // if called again we loose the request scope
            begin.requestInitialized(new ServletRequestEvent(getServletContext(), this));
        }

        listeners = LightweightWebAppBuilderListenerExtractor.findByTypeForContext(contextPath, ServletRequestListener.class);
        if (!listeners.isEmpty()) {
            final ServletRequestEvent event = new ServletRequestEvent(getServletContext(), this);
            for (final ServletRequestListener listener : listeners) {
                listener.requestInitialized(event);
            }
        }
    }

    public void destroy() {
        final boolean openejbRequestDestroyed = getAttribute("openejb_requestDestroyed") == null;
        if (listeners != null && !listeners.isEmpty()) {
            if (begin != null && end != null && openejbRequestDestroyed) {
                end.requestDestroyed(new ServletRequestEvent(getServletContext(), this));
            }
            final ServletRequestEvent event = new ServletRequestEvent(getServletContext(), this);
            for (final ServletRequestListener listener : listeners) {
                listener.requestDestroyed(event);
            }
        }
        if (begin != null && openejbRequestDestroyed) {
            setAttribute("openejb_requestDestroyed", "ok");
            begin.requestDestroyed(new ServletRequestEvent(getServletContext(), this));
        }
    }

    protected class SessionInvalidateListener extends ServletSessionAdapter {
        private final BeginWebBeansListener listener;

        public SessionInvalidateListener(final javax.servlet.http.HttpSession session, final BeginWebBeansListener end) {
            super(session);
            listener = end;
        }

        @Override
        public void invalidate() {
            try {
                super.invalidate();
            } finally {
                listener.sessionDestroyed(new HttpSessionEvent(session));
            }
        }
    }

    private static class SimpleDispatcher implements RequestDispatcher {
        private final String path;

        public SimpleDispatcher(final String path) {
            this.path = path;
        }

        @Override
        public void forward(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
            if (!HttpRequestImpl.class.isInstance(request)) {
                if (HttpServletResponse.class.isInstance(response)) {
                    HttpServletResponse.class.cast(response).sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            final HttpRequestImpl cast = HttpRequestImpl.class.cast(request);
            final HttpRequestImpl httpRequest = new HttpRequestImpl(cast.socketURI);
            httpRequest.uri = cast.uri;
            httpRequest.parameters.putAll(cast.parameters);
            httpRequest.initPathFromContext(cast.contextPath);
            httpRequest.initServletPath(path);
            httpRequest.method = cast.method;

            try {
                SystemInstance.get().getComponent(HttpListenerRegistry.class).onMessage(
                        httpRequest,
                        HttpResponse.class.isInstance(response)? HttpResponse.class.cast(response) : new ServletResponseAdapter(HttpServletResponse.class.cast(response)));
            } catch (final Exception e) {
                throw new ServletException(e.getMessage(), e);
            }
        }

        @Override
        public void include(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
            // not yet supported: TODO: fake response write in ByteArrayOutputStream and then call HttpListenerRegistry and write it back
        }
    }
}