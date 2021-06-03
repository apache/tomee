package org.apache.openejb.arquillian.tests.jaxws;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans11.BeansDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.logging.Logger;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class WebServiceContextTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final String beansXml = Descriptors.create(BeansDescriptor.class)
                .getOrCreateInterceptors()
                .clazz(LogInterceptor.class.getName())
                .up().exportAsString();

        return ShrinkWrap.create(WebArchive.class, "ROOT.war")
                .addClasses(Echo.class, EchoWS.class, SimpleRequestContext.class, Log.class, LogInterceptor.class)
                .addAsWebInfResource(new StringAsset(beansXml), "beans.xml");
    }

    @Test
    public void invoke() throws Exception {
        final Service service = Service.create(new URL(url.toExternalForm() + "/EchoWSService?wsdl"), new QName("http://jaxws.tests.arquillian.openejb.apache.org/", "EchoWSService"));
        final Echo echo = service.getPort(Echo.class);
        assertEquals("foo", echo.echo("foo"));
        final String remote = echo.remote();
        System.out.println(remote);
        assertEquals("127.0.0.1", remote);
    }

    @WebService(portName = "EchoWSPort")
    public static interface Echo {
        public String echo(final String input);
        public String remote();
    }

    @WebService
    public static class EchoWS implements Echo {
        @Resource
        private WebServiceContext wsc;

        @Inject
        private SimpleRequestContext ctx;

        @Override
        @Log
        public String remote() {
            final HttpServletRequest request = (HttpServletRequest) wsc.getMessageContext().get(MessageContext.SERVLET_REQUEST);
            return request.getRemoteAddr();
        }

        @Override
        @Log
        public String echo(String input) {
            return input;
        }
    }

    @RequestScoped
    public static class SimpleRequestContext {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @InterceptorBinding
    @Priority(4020)
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    public @interface Log {
    }

    @Interceptor
    @Log
    public static class LogInterceptor {

        private static final Logger LOGGER = Logger.getLogger(LogInterceptor.class.getName());

        @AroundInvoke
        protected Object businessMethodInterceptor(final InvocationContext ic) throws Throwable {
            final Object result;
            try {
                LOGGER.info("Entering " + ic.getMethod().toString());
                result = ic.proceed();
            } catch (Throwable t) {
                throw t;
            } finally {
                LOGGER.info("Exiting " + ic.getMethod().toString());
            }

            return result;
        }
    }

}
