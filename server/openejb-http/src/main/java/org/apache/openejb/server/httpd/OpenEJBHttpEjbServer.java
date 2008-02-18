/**
 * 
 */
package org.apache.openejb.server.httpd;

/**
 * @author mahmed
 *
 */
public class OpenEJBHttpEjbServer extends HttpEjbServer {

	/**
	 * 
	 */
	public OpenEJBHttpEjbServer() {
		super();
		httpServer = new OpenEJBHttpServer();
	}

}
