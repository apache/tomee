package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.MessageContentsList;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.rest.ThreadLocalContextManager;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public class OpenEJBEJBInvoker extends JAXRSInvoker {
    private BeanContext context;

    public OpenEJBEJBInvoker(BeanContext beanContext) {
        context = beanContext;
    }

    @Override public Object invoke(Exchange exchange, Object request, Object resourceObject) {
        final OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);
        final ClassResourceInfo cri = ori.getClassResourceInfo();
        final Method method = cri.getMethodDispatcher().getMethod(ori);
        final RpcContainer container = RpcContainer.class.cast(context.getContainer());

        Object[] parameters;
        if (request instanceof List) {
            List<Object> params = CastUtils.cast((List<?>) request);
            parameters = params.toArray(new Object[params.size()]);
        } else if (request != null) {
            List<Object> params = new MessageContentsList(request);
            parameters = params.toArray(new Object[params.size()]);
        } else {
            parameters = new Object[0];
        }

        // injecting context parameters
        super.insertExchange(method, parameters, exchange);

        // binding context fields
        for (Field field : cri.getContextFields()) {
            Class<?> type = field.getType();
            if (Request.class.equals(type)) {
                Request binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Request.class);
                ThreadLocalContextManager.REQUEST.set(binding);
            } else if (UriInfo.class.equals(type)) {
                UriInfo binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, UriInfo.class);
                ThreadLocalContextManager.URI_INFO.set(binding);
            } else if (HttpHeaders.class.equals(type)) {
                HttpHeaders binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpHeaders.class);
                ThreadLocalContextManager.HTTP_HEADERS.set(binding);
            } else if (SecurityContext.class.equals(type)) {
                SecurityContext binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, SecurityContext.class);
                ThreadLocalContextManager.SECURITY_CONTEXT.set(binding);
            } else if (ContextResolver.class.equals(type)) {
                ContextResolver<?> binding = JAXRSUtils.createContextValue(exchange.getInMessage(), type, ContextResolver.class);
                ThreadLocalContextManager.CONTEXT_RESOLVER.set(binding);
            }
        }

        // invoking the EJB
        try {
            Object result = container.invoke(context.getDeploymentID(),
                context.getInterfaceType(method.getDeclaringClass()),
                method.getDeclaringClass(), method, parameters, null);
            return new MessageContentsList(result);
        } catch (OpenEJBException e) {
            Response excResponse = JAXRSUtils.convertFaultToResponse(e, exchange.getInMessage());
            return new MessageContentsList(excResponse);
        } finally {
            ThreadLocalContextManager.reset();
        }
    }

    @Override public Object getServiceObject(Exchange exchange) {
        return null;
    }
}
