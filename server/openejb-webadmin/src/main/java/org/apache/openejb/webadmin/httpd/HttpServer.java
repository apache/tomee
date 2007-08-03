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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
 * $Id: HttpServer.java 446024 2006-02-21 08:45:52Z dblevins $
 */
package org.apache.openejb.webadmin.httpd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.util.Logger;
import org.apache.openejb.webadmin.HttpHome;
import org.apache.openejb.webadmin.HttpObject;

/** 
 * This is the main class for the web administration.  It takes care of the
 * processing from the browser, sockets and threading.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 * @since 11/25/2001
 */
public class HttpServer implements ServerService{

    private static final Logger logger = Logger.getInstance( "OpenEJB.server", "org.apache.openejb.server.util.resources" );
    private InitialContext jndiContext;

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }
    
    public void service(Socket socket) throws ServiceException, IOException {
        /**
         * The InputStream used to receive incoming messages from the client.
         */
        InputStream in = socket.getInputStream();
        /**
         * The OutputStream used to send outgoing response messages to the client.
         */
        OutputStream out = socket.getOutputStream();


        try {
            processRequest(socket, in, out);
        } catch ( Throwable e ) {
            logger.error( "Unexpected error", e );
        } finally {
            try {
                if ( out != null ) {
                    out.flush();
                    out.close();
                }
                if (in != null)
                    in.close();
                if (socket != null)
                    socket.close();
            } catch ( Throwable t ){
                logger.error(
                        "Encountered problem while closing connection with client: "
                        + t.getMessage());
            }
        }
    }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "webadmin";
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }


    /** Initalizes this instance and takes care of starting things up
     * @param props a properties instance for system properties
     * @throws Exception if an exeption is thrown
     */
    public void init(Properties props) throws Exception{

        //props.putAll(System.getProperties());

        Properties properties = new Properties();
        properties.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.openejb.core.ivm.naming.InitContextFactory");
        jndiContext = new InitialContext(properties);

    }

    /** 
     * takes care of processing requests and creating the webadmin ejb's
     * 
     * @param in the input stream from the browser
     * @param out the output stream to the browser
     */
    private void processRequest(Socket socket, InputStream in, OutputStream out) {

        HttpRequestImpl req = new HttpRequestImpl();
        HttpResponseImpl res = new HttpResponseImpl();
        InetAddress client = socket.getInetAddress();
        

        try {
            req.readMessage(in);
            res.setRequest(req);
//            logger.info(client.getHostName()+": "+req.getRequestLine());
        } catch (Throwable t) {
            //TODO: log or something
            //t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Could not read the request.\n" + t.getClass().getName() + ":\n" + t.getMessage(),
                    t);
            try {
                logger.error(client.getHostName()+": "+res.getCode()+" "+req.getRequestLine()+ " ["+ res.getResponseString()+"]");
                res.writeMessage(out);
            } catch (Throwable t2) {
                //TODO: log or something
                //t2.printStackTrace();
            }
            return;
        }

        //System.out.println("[] read");
        URL uri = null;
        String file = null;

        try {
            uri = req.getURI();
            file = uri.getFile();
            int querry = file.indexOf("?");
            if (querry != -1) {
                file = file.substring(0, querry);
            }

            //System.out.println("[] file="+file);

        } catch (Throwable t) {
            //TODO: log or something
            //t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Could not determine the module "
                        + file
                        + "\n"
                        + t.getClass().getName()
                        + ":\n"
                        + t.getMessage());
            try {
                logger.error(client.getHostName()+": "+res.getCode()+" "+req.getRequestLine()+ " ["+ res.getResponseString()+"]");
                res.writeMessage(out);
            } catch (Throwable t2) {
                //TODO: log or something
                //t2.printStackTrace();
            }
            return;
        }

        HttpObject httpObject = null;

        try {
            httpObject = getHttpObject(file);
            //System.out.println("[] module="+httpObject);
        } catch (Throwable t) {
            //TODO: log or something
            //t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Could not load the module "
                        + file
                        + "\n"
                        + t.getClass().getName()
                        + ":\n"
                        + t.getMessage(),
                    t);
            //System.out.println("[] res="+res);
            try {
                logger.error(client.getHostName()+": "+res.getCode()+" "+req.getRequestLine()+ " ["+ res.getResponseString()+"]");
                res.writeMessage(out);
            } catch (Throwable t2) {
                //TODO: log or something
                //t2.printStackTrace();
            }
            return;
        }

        try {
            httpObject.onMessage(req, res);
        } catch (Throwable t) {
            //TODO: log or something
            //t.printStackTrace();
            res =
                HttpResponseImpl.createError(
                    "Error occurred while executing the module "
                        + file
                        + "\n"
                        + t.getClass().getName()
                        + ":\n"
                        + t.getMessage(),
                    t);
            try {
                logger.error(client.getHostName()+": "+res.getCode()+" "+req.getRequestLine()+ " ["+ res.getResponseString()+"]");
                res.writeMessage(out);
            } catch (Throwable t2) {
                //TODO: log or something
                //t2.printStackTrace();
            }

            return;
        }

        try {
            logger.info(client.getHostName()+": "+res.getCode()+" "+req.getRequestLine()+ " ["+ res.getResponseString()+"]");
            res.writeMessage(out);
        } catch (Throwable t) {
            //TODO: log or something
            //t.printStackTrace();
            return;
        }
    }

    /** gets an ejb object reference for use in <code>processRequest</code>
     * @param beanName the name of the ejb to look up
     * @throws IOException if an exception is thrown
     * @return an object reference of the ejb
     */
    private HttpObject getHttpObject(String beanName) throws IOException {
        Object obj = null;

        //check for no name, add something here later
        if (beanName.equals("/")) {
            try {
                obj = jndiContext.lookup("Webadmin/Home");
            } catch (javax.naming.NamingException ne) {
                throw new IOException(ne.getMessage());
            }
        } else {
            try {
                obj = jndiContext.lookup(beanName);
            } catch (javax.naming.NameNotFoundException e) {
                try {
                    obj = jndiContext.lookup("httpd/DefaultBean");
                } catch (javax.naming.NamingException ne) {
                    throw new IOException(ne.getMessage());
                }
            } catch (javax.naming.NamingException e) {
                throw new IOException(e.getMessage());
            }
        }

        HttpHome ejbHome = (HttpHome) obj;
        HttpObject httpObject = null;

        try {
            httpObject = ejbHome.create();

            // 
            obj = org.apache.openejb.util.proxy.ProxyManager.getInvocationHandler(httpObject);
            org.apache.openejb.core.ivm.BaseEjbProxyHandler handler = null;
            handler = (org.apache.openejb.core.ivm.BaseEjbProxyHandler) obj;
            handler.setIntraVmCopyMode(false);
        } catch (javax.ejb.CreateException cre) {
            throw new IOException(cre.getMessage());
        }

        return httpObject;
    }
}
