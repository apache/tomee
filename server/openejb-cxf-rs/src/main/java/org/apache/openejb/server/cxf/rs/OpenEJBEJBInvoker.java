package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.message.Exchange;
import org.apache.openejb.BeanContext;

/**
 * @author Romain Manni-Bucau
 */
public class OpenEJBEJBInvoker extends JAXRSInvoker {
    private BeanContext context;

    public OpenEJBEJBInvoker(BeanContext beanContext) {
        context = beanContext;
    }

    @Override public Object invoke(Exchange exchange, Object request, Object resourceObject) {
        throw new UnsupportedOperationException("to implement...");
    }

    @Override public Object getServiceObject(Exchange exchange) {
        return null;
    }
}
