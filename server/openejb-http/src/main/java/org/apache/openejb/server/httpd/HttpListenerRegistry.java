package org.apache.openejb.server.httpd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class HttpListenerRegistry implements HttpListener {

    private final HashMap registry = new HashMap();

    public HttpListenerRegistry() {
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
        HashMap listeners;

        synchronized (registry) {
            listeners = new HashMap(registry);
        }

        String path = request.getURI().getPath();

        for (Iterator iterator = listeners.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String pattern = (String) entry.getKey();
            HttpListener listener = (HttpListener) entry.getValue();
            if (path.matches(pattern)) {
                listener.onMessage(request, response);
                break;
            }
        }
    }

    public void addHttpListener(HttpListener listener, String regex) {
        synchronized (registry) {
            registry.put(regex, listener);
        }
    }
}
