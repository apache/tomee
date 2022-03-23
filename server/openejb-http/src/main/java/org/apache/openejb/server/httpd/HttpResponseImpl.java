/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.session.SessionManager;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OpenEjbVersion;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import static java.util.Collections.singletonList;

/**
 * This class takes care of HTTP Responses.  It sends data back to the browser.
 */
public class HttpResponseImpl implements HttpResponse {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, HttpResponseImpl.class.getName());
    private static final String DEFAULT_CONTENT_TYPE = SystemInstance.get().getProperty("openejb.http.default-content-type", "text/html");

    /**
     * Response string
     */
    private String responseString = "OK";

    /**
     * Code
     */
    private int code = HttpServletResponse.SC_OK;

    /**
     * Response headers
     */
    private final Map<String, List<String>> headers = new HashMap<>();

    /**
     * the writer for the response
     */
    private transient PrintWriter writer;
    /**
     * the raw body
     */
    private transient ServletByteArrayOutputStream sosi;

    /**
     * the HTTP version
     */
    public static final String HTTP_VERSION = "HTTP/1.1";
    /**
     * a line feed character
     */
    public static final String CRLF = "\r\n";
    /**
     * a space character
     */
    public static final String SP = " ";
    /**
     * a colon and space
     */
    public static final String CSP = ": ";
    /**
     * the server to send data from
     */
    public static String server;

    private HttpRequestImpl request;
    private URLConnection content;

    private boolean commited = false;
    private String encoding = "UTF-8";
    private Locale locale = Locale.getDefault();

    protected void setRequest(final HttpRequestImpl request) {
        this.request = request;
    }

    /**
     * sets a header to be sent back to the browser
     *
     * @param name  the name of the header
     * @param value the value of the header
     */
    public void setHeader(final String name, final String value) {
        headers.put(name, new ArrayList<>(singletonList(value)));
    }

    @Override
    public void setIntHeader(final String s, final int i) {
        setHeader(s, Integer.toString(i));
    }

    @Override
    public void setStatus(final int i) {
        setCode(i);
    }

    @Override
    public void setStatus(final int i, final String s) {
        setCode(i);
        setStatusMessage(s);
    }

    @Override
    public void addCookie(final Cookie cookie) {
        setHeader(cookie.getName(), cookie.getValue());
    }

    @Override
    public void addDateHeader(final String s, final long l) {
        setHeader(s, Long.toString(l));
    }

    @Override
    public void addHeader(final String s, final String s1) {
        Collection<String> list = headers.get(s);
        if (list == null) {
            setHeader(s, s1);
        } else {
            list.add(s1);
        }
    }

    @Override
    public void addIntHeader(final String s, final int i) {
        setIntHeader(s, i);
    }

    @Override
    public boolean containsHeader(final String s) {
        return headers.containsKey(s);
    }

    @Override
    public String encodeURL(final String s) {
        return toEncoded(s);
    }

    @Override
    public String encodeRedirectURL(final String s) {
        return toEncoded(s);
    }

    @Override
    public String encodeUrl(final String s) {
        return toEncoded(s);
    }

    @Override
    public String encodeRedirectUrl(final String s) {
        return encodeRedirectURL(s);
    }

    /**
     * Gets a header based on the name passed in
     *
     * @param name The name of the header
     * @return the value of the header
     */
    public String getHeader(final String name) {
        final Collection<String> strings = headers.get(name);
        return strings == null ? null : strings.iterator().next();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public Collection<String> getHeaders(final String s) {
        return headers.get(s);
    }

    @Override
    public int getStatus() {
        return getCode();
    }

    @Override
    public void sendError(final int i) throws IOException {
        setCode(i);
    }

    @Override
    public void sendError(final int i, final String s) throws IOException {
        setCode(i);
        setStatusMessage(s);
    }

    @Override
    public void sendRedirect(final String path) throws IOException {
        if (commited) {
            throw new IllegalStateException("response already committed");
        }
        resetBuffer();

        try {
            setStatus(SC_FOUND);

            setHeader("Location", base() + toEncoded(path));
        } catch (final IllegalArgumentException e) {
            setStatus(SC_NOT_FOUND);
        }
    }

    @Override
    public void setDateHeader(final String s, final long l) {
        addDateHeader(s, l);
    }

    /**
     * gets the OutputStream to send data to the browser
     *
     * @return the OutputStream to send data to the browser
     */
    public ServletOutputStream getOutputStream() {
        return sosi;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public boolean isCommitted() {
        return commited;
    }

    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public int getBufferSize() {
        return sosi.getOutputStream().size();
    }

    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * sets the HTTP response code to be sent to the browser.  These codes are:
     *
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
     * @param code the code to be sent to the browser
     */
    public void setCode(final int code) {
        this.code = code;
        commited = true;
    }

    /**
     * gets the HTTP response code
     *
     * @return the HTTP response code
     */
    public int getCode() {
        return code;
    }

    /**
     * sets the content type to be sent back to the browser
     *
     * @param type the type to be sent to the browser (i.e. "text/html")
     */
    public void setContentType(final String type) {
        setHeader("Content-Type", type);
    }

    @Override
    public void setLocale(final Locale loc) {
        locale = loc;
    }

    /**
     * gets the content type that will be sent to the browser
     *
     * @return the content type (i.e. "text/html")
     */
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the response string to be sent to the browser
     *
     * @param responseString the response string
     */
    public void setResponseString(final String responseString) {
        this.responseString = responseString;
    }

    /**
     * resets the data to be sent to the browser
     */
    public void reset() {
        initBody();
    }

    @Override
    public void resetBuffer() {
        sosi.getOutputStream().reset();
    }

    @Override
    public void setBufferSize(final int i) {
        // no-op
    }

    @Override
    public void setCharacterEncoding(final String s) {
        encoding = s;
    }

    @Override
    public void setContentLength(final int i) {
        // no-op
    }

    @Override
    public void setContentLengthLong(final long length) {
        // no-op
    }

    /**
     * resets the data to be sent to the browser with the response code and response
     * string
     *
     * @param code           the code to be sent to the browser
     * @param responseString the response string to be sent to the browser
     */
    public void reset(final int code, final String responseString) {
        setCode(code);
        setResponseString(responseString);
        initBody();
    }

    /*------------------------------------------------------------*/
    /*  Methods for writing out a response                        */
    /*------------------------------------------------------------*/

    /**
     * creates a new instance of HttpResponseImpl with default values
     */
    protected HttpResponseImpl() {
        this(200, "OK", DEFAULT_CONTENT_TYPE);
    }

    /**
     * Creates a new HttpResponseImpl with user provided parameters
     *
     * @param code           the HTTP Response code, see <a href="http://www.ietf.org/rfc/rfc2616.txt">http://www.ietf.org/rfc/rfc2616.txt</a>
     *                       for these codes
     * @param responseString the response string to be sent back
     * @param contentType    the content type to be sent back
     */
    protected HttpResponseImpl(final int code, final String responseString, final String contentType) {
        this.responseString = responseString;
        this.code = code;

        // Default headers
        setHeader("Server", getServerName());
        setHeader("Connection", "close");
        setHeader("Content-Type", contentType);

        // create the body.
        initBody();
    }

    /**
     * Takes care of sending the response line, headers and body
     *
     * HTTP/1.1 200 OK
     * Server: Netscape-Enterprise/3.6 SP3
     * Date: Thu, 07 Jun 2001 17:30:42 GMT
     * Content-Type: text/html
     * Connection: close
     *
     * @param output the output to send the response to
     * @throws java.io.IOException if an exception is thrown
     */
    protected void writeMessage(final OutputStream output, final boolean indent) throws IOException {
        flushBuffer();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(baos);
        closeMessage();
        writeResponseLine(out);
        writeHeaders(out);
        writeBody(out, indent);
        out.flush();
        output.write(baos.toByteArray());
        output.flush();
    }

    /**
     * initalizes the body
     */
    private void initBody() {
        sosi = new ServletByteArrayOutputStream();
        writer = new PrintWriter(sosi);
    }

    /**
     * Creates a string version of the response similar to:
     *
     * HTTP/1.1 200 OK
     *
     * @return the string value of this HttpResponseImpl
     */
    public String toString() {
        return HTTP_VERSION + SP + code + SP + responseString;
    }

    /**
     * closes the message sent to the browser
     */
    private void closeMessage() {
        setContentLengthHeader();
        setCookieHeader();
    }


    private void setContentLengthHeader() {
        if (content == null) {
            writer.flush();
            writer.close();
            final int length = sosi.getOutputStream().size();
            setHeader("Content-Length", length + "");
        } else {
            setHeader("Content-Length", content.getContentLength() + "");
        }
    }

    private void setCookieHeader() {
        if (request == null) {
            return;
        }

        final HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        setHeader(HttpRequest.HEADER_SET_COOKIE, SessionManager.EJBSESSIONID + '=' + session.getId() + "; Path=/");
    }

    /**
     * Writes a response line similar to this:
     *
     * HTTP/1.1 200 OK
     *
     * to the browser
     *
     * @param out the output stream to write the response line to
     * @throws java.io.IOException if an exception is thrown
     */
    private void writeResponseLine(final DataOutput out) throws IOException {
        out.writeBytes(HTTP_VERSION);
        out.writeBytes(SP);
        out.writeBytes(code + "");
        out.writeBytes(SP);
        if (responseString != null) {
            out.writeBytes(responseString);
        }
        out.writeBytes(CRLF);
    }

    /**
     * writes the headers out to the browser
     *
     * @param out the output stream to be sent to the browser
     * @throws java.io.IOException if an exception is thrown
     */
    private void writeHeaders(final DataOutput out) throws IOException {
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getValue().size() == 1) {
                writeHeader(out, entry.getKey(), entry.getValue().get(0));
            } else if (entry.getValue().size() > 1) {
                for (final String val : entry.getValue()) {
                    writeHeader(out, entry.getKey(), val);
                }
            }
        }
    }

    private void writeHeader(final DataOutput out, final String name, final String value) throws IOException {
        out.writeBytes(name);
        out.writeBytes(CSP);
        out.writeBytes(value);
        out.writeBytes(CRLF);
    }

    /**
     * writes the body out to the browser
     *
     * @param out    the output stream that writes to the browser
     * @param indent format xml
     * @throws java.io.IOException if an exception is thrown
     */
    private void writeBody(final DataOutput out, final boolean indent) throws IOException {
        out.writeBytes(CRLF);
        if (content == null) {
            if (indent && OpenEJBHttpServer.isTextXml(headers)) {
                final String xml = new String(sosi.getOutputStream().toByteArray());
                out.write(OpenEJBHttpServer.reformat(xml).getBytes());
            } else {
                out.write(sosi.getOutputStream().toByteArray());
            }
        } else {
            final InputStream in = content.getInputStream();
            final byte[] buf = new byte[1024];

            int i;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
        }
    }

    /**
     * gets the name of the server being used
     *
     * @return the name of the server
     */
    public String getServerName() {
        if (server == null) {
            final String version = OpenEjbVersion.get().getVersion();
            final String os = System.getProperty("os.name") + "/" + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
            server = "OpenEJB/" + version + " " + os;
        }
        return server;
    }


    /**
     * This could be improved at some day in the future
     * to also include a stack trace of the exceptions
     *
     * @param message the error message to be sent
     * @return the HttpResponseImpl that this error belongs to
     */
    @SuppressWarnings("unused")
    protected static HttpResponseImpl createError(final String message) {
        return createError(message, null);
    }

    /**
     * creates an error with user defined variables
     *
     * @param message the message of the error
     * @param t       a Throwable to print a stack trace to
     * @return the HttpResponseImpl that this error belongs to
     */
    protected static HttpResponseImpl createError(String message, final Throwable t) {
        final HttpResponseImpl res = new HttpResponseImpl(500, "Internal Server Error", "text/html");
        final PrintWriter body;
        try {
            body = res.getWriter();
        } catch (final IOException e) { // impossible normally
            return res;
        }

        body.println("<html>");
        body.println("<body>");
        body.println("<h3>Internal Server Error</h3>");
        body.println("<br><br>");

        if (LOGGER.isDebugEnabled()) { // this is not an error, don't log it by default
            LOGGER.error(String.valueOf(t), t);
        }

        if (message != null) {
            final StringTokenizer msg = new StringTokenizer(message, "\n\r");

            while (msg.hasMoreTokens()) {
                body.print(msg.nextToken());
                body.println("<br>");
            }
        }

        if (t != null) {

            PrintWriter writer = null;

            try {
                body.println("<br><br>");
                body.println("Stack Trace:<br>");
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writer = new PrintWriter(baos);
                t.printStackTrace(writer);
                writer.flush();

                message = new String(baos.toByteArray());
                final StringTokenizer msg = new StringTokenizer(message, "\n\r");

                while (msg.hasMoreTokens()) {
                    body.print(msg.nextToken());
                    body.println("<br>");
                }
            } catch (final Exception e) {
                //no-op
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }

        body.println("</body>");
        body.println("</html>");

        return res;
    }

    /**
     * Creates a forbidden response to be sent to the browser using IP authentication
     *
     * @param ip the ip that is forbidden
     * @return the HttpResponseImpl that this error belongs to
     */
    @SuppressWarnings("unused")
    protected static HttpResponseImpl createForbidden(final String ip) {
        final HttpResponseImpl res = new HttpResponseImpl(403, "Forbidden", "text/html");
        final PrintWriter body;
        try {
            body = res.getWriter();
        } catch (final IOException e) { // normally impossible
            return res;
        }

        body.println("<html>");
        body.println("<body>");
        body.println("<h3>Forbidden</h3>");
        body.println("<br><br>");
        // Add more text here
        // IP not allowed, etc.
        body.println("IP address: " + ip + " is not registered on this server, please contact your system administrator.");
        body.println("</body>");
        body.println("</html>");

        return res;
    }

    /**
     * writes this object out to a file
     *
     * @param out the ObjectOutputStream to write to
     * @throws java.io.IOException if an exception is thrown
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
        /** Response string */
        out.writeObject(responseString);

        /** Code */
        out.writeInt(code);

        /** Response headers */
        out.writeObject(headers);

        /** Response body */
        writer.flush();
        final byte[] body = sosi.getOutputStream().toByteArray();
        //System.out.println("[] body "+body.length );
        out.writeObject(body);
    }

    /**
     * Reads in a serilized HttpResponseImpl object from a file
     *
     * @param in the input to read the object from
     * @throws java.io.IOException    if an exception is thrown
     * @throws ClassNotFoundException if an exception is thrown
     */
    @SuppressWarnings({"unchecked"})
    private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        /** Response string */
        this.responseString = (String) in.readObject();

        /** Code */
        this.code = in.readInt();

        /** Response headers */
        final Map headers = (Map) in.readObject();
        this.headers.clear();
        this.headers.putAll(headers);

        /** Response body */
        final byte[] body = (byte[]) in.readObject();
        //System.out.println("[] body "+body.length );
        sosi = new ServletByteArrayOutputStream();
        sosi.write(body);
        writer = new PrintWriter(sosi);

    }

    /**
     * @param content The content to set.
     */
    public void setContent(final URLConnection content) {
        this.content = content;
    }

    public void setStatusMessage(final String responseString) {
        this.setResponseString(responseString);
    }

    private String base() {
        return request == null ? "" : request.getURI().getScheme() + "://" + request.getURI().getAuthority();
    }

    private String toEncoded(final String url) {
        return url; // should add ;JSESSIONID=xxx but breaks other things and here we don't need it that much
    }
}