package org.apache.openejb.server.httpd;

/**
 * @version $Rev$ $Date$
 */
public interface HttpListener {

    void onMessage(HttpRequest request, HttpResponse response) throws Exception;

}
