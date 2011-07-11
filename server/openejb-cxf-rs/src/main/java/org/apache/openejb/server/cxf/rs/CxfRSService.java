package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.cxf.transport.HttpTransportFactory;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.server.rest.RsHttpListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * @author Romain Manni-Bucau
 */
public class CxfRSService extends RESTService {
    private static final String NAME = "cxf-rs";
    private HttpTransportFactory httpTransportFactory;


    @Override public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override public void service(Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override public String getName() {
        return NAME;
    }

    @Override public void init(Properties properties) throws Exception {
        // no-op
    }

    @Override protected void beforeStart() {
        super.beforeStart();
        httpTransportFactory = new HttpTransportFactory(CxfUtil.getBus());
    }

    @Override protected RsHttpListener createHttpListener(Object o, RsHttpListener.Scope scope) {
        return new CxfRsHttpListener(scope, httpTransportFactory);
    }
}
