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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

import org.apache.openejb.webadmin.HttpBean;
import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.HttpHome;
import org.apache.openejb.loader.SystemInstance;

/** This is a webadmin bean which has default functionality such as genderating
 * error pages and setting page content.
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
@Stateless(name = "httpd/DefaultBean")
@RemoteHome(HttpHome.class)
public class DefaultHttpBean implements HttpBean {

    /** The path in which to look for files. */
    private static final URL[] PATH = getSearchPath();
    
    /** the ejb session context */
	private SessionContext context;

    
    private static URL[] getSearchPath(){
        ArrayList path = new ArrayList();

        try {
            //OpenEJB Home and Base folders
            URL base = SystemInstance.get().getBase().getDirectory().toURL();
            URL home = SystemInstance.get().getHome().getDirectory().toURL();

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (!base.sameFile(home)) {
                path.add(new URL(base, "htdocs/"));
            }
            path.add(new URL(home, "htdocs/"));
            path.add(classLoader.getResource("/htdocs/"));
            path.add(classLoader.getResource("/openejb/webadmin/"));
        } catch (Exception e) {
            // TODO: 1: We should never get an exception here
            e.printStackTrace();
        }

        return (URL[]) path.toArray(new URL[0]);
    }

	/** Creates a new instance */
	public void ejbCreate() {}

	/** the main processing part of the this bean
	 * @param request the http request object
	 * @param response the http response object
	 * @throws IOException if an exception is thrown
	 */
	public void onMessage(HttpRequest request, HttpResponse response) throws java.io.IOException {
        InputStream in = null;  
        OutputStream out = null;
		// Internationalize this
		try {
			String file = request.getURI().getFile();
			String ext = (file.indexOf('.') == -1) ? null : file.substring(file.indexOf('.'));

			if (ext != null) {
				//resolve the content type
				if (ext.equalsIgnoreCase(".gif")) {
					response.setContentType("image/gif");
				} else if (ext.equalsIgnoreCase(".jpeg") || ext.equalsIgnoreCase(".jpg")) {
					response.setContentType("image/jpeg");
				} else if (ext.equalsIgnoreCase(".png")) {
					response.setContentType("image/png");
				} else if (ext.equalsIgnoreCase(".css")) {
					response.setContentType("text/css");
				} else if (ext.equalsIgnoreCase(".js")) {
					response.setContentType("text/javascript");
				} else if (ext.equalsIgnoreCase(".txt")) {
					response.setContentType("text/plain");
				} else if (ext.equalsIgnoreCase(".java")) {
					response.setContentType("text/plain");
				} else if (ext.equalsIgnoreCase(".xml")) {
					response.setContentType("text/plain");
				} else if (ext.equalsIgnoreCase(".zip")) {
					response.setContentType("application/zip");
				}
			}

            
            
			URLConnection resource = findResource(request.getURI().getFile());  
            HttpResponseImpl res = (HttpResponseImpl)response;
            res.setContent(resource);

        } catch (java.io.FileNotFoundException e) {
			do404(request, response);

		} catch (java.io.IOException e) {
			do500(request, response, e.getMessage());
		} finally {
            if (in != null) in.close();
        }
	}

    private URLConnection findResource(String fileName) throws FileNotFoundException, IOException{
        if (fileName.startsWith("/")){
            fileName = fileName.substring(1);
        }
        
        for (int i = 0; i < PATH.length; i++) {
            try {
                URL base = PATH[i];
                URL resource = new URL(base, fileName);
                URLConnection conn = resource.openConnection();
                if (resource.openConnection().getContentLength() > 0){
                    return conn;
                }
            } catch (MalformedURLException e) {
            } catch (FileNotFoundException e) {
            }
        }
        throw new FileNotFoundException("Cannot locate resource: "+fileName);
    }
	/** Creates a "Page not found" error screen
	 * @param request the HTTP request object
	 * @param response the HTTP response object
	 */
	public void do404(HttpRequest request, HttpResponse response) {
		response.reset(404, "Object not found.");
		java.io.PrintWriter body = response.getPrintWriter();

		body.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
		body.println("<HTML><HEAD>");
		body.println("<TITLE>404 Not Found</TITLE>");
		body.println("</HEAD><BODY>");
		body.println("<H1>Not Found</H1>");
		body.println(
			"The requested URL <font color=\"red\">"
				+ request.getURI().getFile()
				+ "</font> was not found on this server.<P>");
		body.println("<HR>");
		body.println("<ADDRESS>" + response.getServerName() + "</ADDRESS>");
		body.println("</BODY></HTML>");
	}

	/** Creates and "Internal Server Error" page
	 * @param request the HTTP request object
	 * @param response the HTTP response object
	 * @param message the message to be sent back to the browser
	 */
	public void do500(HttpRequest request, HttpResponse response, String message) {
		response.reset(500, "Internal Server Error.");
		java.io.PrintWriter body = response.getPrintWriter();
		body.println("<html>");
		body.println("<body>");
		body.println("<h3>Internal Server Error</h3>");
		body.println("<br><br>");

		if (message != null) {
			StringTokenizer msg = new StringTokenizer(message, "\n\r");
			while (msg.hasMoreTokens()) {
				body.print(msg.nextToken());
				body.println("<br>");
			}
		}

		body.println("</body>");
		body.println("</html>");
	}

	/** called on a stateful sessionbean after the bean is
	 * deserialized from storage and put back into use.      
	 * @throws javax.ejb.EJBException if an exeption is thrown
	 * @throws java.rmi.RemoteException if an exception is thrown
	 */
	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	/** called on a stateful sessionbean before the bean is 
	 * removed from memory and serialized to a temporary store.  
	 * This method is never called on a stateless sessionbean
	 * @throws javax.ejb.EJBException if an exception is thrown
	 * @throws java.rmi.RemoteException if an exception is thrown
	 */
	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	/** called by the ejb container when this bean is about to be garbage collected
	 * @throws javax.ejb.EJBException if an exception is thrown
	 * @throws java.rmi.RemoteException if an exception is thrown
	 */
	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	/** sets the session context for this bean
	 * @param sessionContext the session context to be set
	 * @throws javax.ejb.EJBException if an exception is thrown
	 * @throws java.rmi.RemoteException if an exception is thrown
	 */
	public void setSessionContext(javax.ejb.SessionContext sessionContext)
		throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.context = sessionContext;
	}
}
