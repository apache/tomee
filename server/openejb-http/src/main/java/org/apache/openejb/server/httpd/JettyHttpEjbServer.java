/**
 * 
 */
package org.apache.openejb.server.httpd;

import org.apache.openejb.server.SelfManaging;

/**
 * @author mahmed
 *
 */
public class JettyHttpEjbServer extends HttpEjbServer implements SelfManaging {

	/**
	 * 
	 */
	public JettyHttpEjbServer() {
		super();
		httpServer = new JettyHttpServer();
	}

}