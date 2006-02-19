package org.openejb.server.httpd;

/**
 * @version $Rev$ $Date: 2005-12-11 17:29:03 -0800 (Sun, 11 Dec 2005) $
 */
public interface HttpListener {

    void onMessage(HttpRequest request, HttpResponse response) throws Exception;

}
