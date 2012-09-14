package org.apache.tomee.catalina.routing;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;

public class RouterValve extends ValveBase {
    private SimpleRouter router = new SimpleRouter();

    @Override
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        final String destination = router.route(request.getRequestURI());
        if (destination == null) {
            getNext().invoke(request, response);
            return;
        }

        response.sendRedirect(destination);
    }

    public void setConfigurationPath(URL configurationPath) {
        router.readConfiguration(configurationPath);
    }

    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        router.JMXOn("Router Valve " + System.identityHashCode(this));
    }

    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        router.cleanUp();
        super.stopInternal();
    }
}
