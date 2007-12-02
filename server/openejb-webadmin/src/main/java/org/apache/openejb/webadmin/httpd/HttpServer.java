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
import org.apache.openejb.util.LogCategory;
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

    private static final Logger logger = Logger.getInstance( LogCategory.OPENEJB_SERVER, HttpServer.class );
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
