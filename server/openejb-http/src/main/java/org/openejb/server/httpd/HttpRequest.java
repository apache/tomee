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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.net.URI;
import javax.servlet.ServletInputStream;


/** An interface to take care of HTTP Requests.  It parses headers, content, form and url
 *  parameters.
 *
 */
public interface HttpRequest extends java.io.Serializable{
    
    /** the HTTP OPTIONS type */    
    public static final int OPTIONS = 0; // Section 9.2
    /** the HTTP GET type */    
    public static final int GET     = 1; // Section 9.3
    /** the HTTP HEAD type */    
    public static final int HEAD    = 2; // Section 9.4
    /** the HTTP POST type */    
    public static final int POST    = 3; // Section 9.5
    /** the HTTP PUT type */    
    public static final int PUT     = 4; // Section 9.6
    /** the HTTP DELETE type */    
    public static final int DELETE  = 5; // Section 9.7
    /** the HTTP TRACE type */    
    public static final int TRACE   = 6; // Section 9.8
    /** the HTTP CONNECT type */    
    public static final int CONNECT = 7; // Section 9.9
    /** the HTTP UNSUPPORTED type */    
    public static final int UNSUPPORTED = 8;
    
    /* 
     * Header variables 
     */
	/** the Accept header */
	public static final String HEADER_ACCEPT = "Accept";
	/** the Accept-Encoding header */
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	/** the Accept-Language header */
	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    /** the Content-Type header */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    /** the Content-Length header */
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	/** the Connection header */
	public static final String HEADER_CONNECTION = "Connection";
	/** the Cache-Control header */
	public static final String HEADER_CACHE_CONTROL = "Cache-Control";
	/** the Host header */
	public static final String HEADER_HOST = "Host";
	/** the User-Agent header */
	public static final String HEADER_USER_AGENT = "User-Agent";
    /** the Set-Cookie header */
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    /** the Cookie header */
    public static final String HEADER_COOKIE = "Cookie";

    /**
     * Gets a form or URL query parameter based on the name passed in.
     * @param name
     */
    String getParameter(String name);

    /**
     * Gets all the form and URL query parameters
     * @return All the form and URL query parameters
     */
    Map getParameters();

    /**
     * Returns the current <code>HttpSession</code> associated with this
     * request or, if there is no current session and <code>create</code> is
     * true, returns a new session.
     *
     * <p>If <code>create</code> is <code>false</code> and the request has no
     * valid <code>HttpSession</code>, this method returns <code>null</code>.
     *
     * @param create <code>true</code> to create a new session for this request
     * if necessary; <code>false</code> to return <code>null</code> if there's
     * no current session
     *
     * @return the <code>HttpSession</code> associated with this request or
     * <code>null</code> if <code>create</code> is <code>false</code> and the
     * request has no valid session
     *
     * @see #getSession()
     */
    public HttpSession getSession(boolean create);

    /**
     * Returns the current session associated with this request, or if the
     * request does not have a session, creates one.
     *
     * @return the <code>HttpSession</code> associated with this request
     *
     * @see #getSession(boolean)
     */
    public HttpSession getSession();
    
    /** Gets a header based the header name passed in.
     * @param name The name of the header to get
     * @return The value of the header
     */  
    public String getHeader(String name);

    /** Gets an integer value of the request method.  These values are:
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
     * @return The integer value of the method
     */ 
    public int getMethod();

    /** Gets the URI for the current URL page.
     * @return The URI
     */ 
    public java.net.URI getURI();

    int getContentLength();

    String getContentType();

    InputStream getInputStream() throws IOException;

    public Object getAttribute(String name);

    public void setAttribute(String name, Object value);

}
