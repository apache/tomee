package org.apache.openejb.server.wink;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.Application;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.utils.RegistrationUtils;

// TODO: lifecycle (se cxf-rs invokers)
// TODO: see if using RestServlet is not more relevant = breaking our RsHttpListener
public class WinkHttpListener implements RsHttpListener {
    private RequestProcessor processor;
    private ServletContext servletContext;

    @Override
    public void deployEJB(final String fullContext, final BeanContext beanContext) {
        deployPojo(fullContext, beanContext.getBeanClass(), null, null, null, null); // TODO
    }

    @Override
    public void deploySingleton(final String fullContext, final Object o, final Application appInstance) {
        RegistrationUtils.registerInstances(contexts(o.getClass().getClassLoader()), o);
    }

    @Override
    public void deployPojo(final String fullContext, final Class<?> loadedClazz, final Application app, final Collection<Injection> injections, final Context context, final WebBeansContext owbCtx) {
        RegistrationUtils.registerClasses(contexts(loadedClazz.getClassLoader()), loadedClazz);
    }

    private javax.servlet.ServletContext contexts(final ClassLoader classLoader) {
        final DeploymentConfiguration conf = new DeploymentConfiguration();
        conf.setProperties(properties(classLoader));
        conf.init();

        processor = new RequestProcessor(conf);

        servletContext = new ServletContext(classLoader);
        servletContext.setAttribute(RequestProcessor.class.getName(), processor);
        return servletContext;
    }

    private static Properties properties(final ClassLoader cl) {
        final Properties prop = new Properties();
        final InputStream is = cl.getResourceAsStream("META-INF/wink-default.properties");
        if (is != null) {
            try {
                prop.load(is);
            } catch (IOException e) {
                e.printStackTrace();  // TODO
            }
        }

        final Properties prop2 = new Properties();
        final InputStream is2 = cl.getResourceAsStream("META-INF/wink.properties");
        if (is2 != null) {
            try {
                prop2.load(is2);
                prop.putAll(prop2);
            } catch (IOException e) {
                e.printStackTrace();  // TODO
            }
        }

        return prop; // TODO: cache it
    }

    @Override
    public void undeploy() {
        processor.getConfiguration().getProvidersRegistry().removeAllProviders();
        processor.getConfiguration().getResourceRegistry().removeAllResources();
        for (ObjectFactory<?> of : processor.getConfiguration().getApplicationObjectFactories()) {
            of.releaseAll(null);
        }
    }

    @Override
    public void onMessage(final HttpRequest httpRequest, final HttpResponse response) throws Exception {
        final HttpServletRequest wrapper = new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getRequestURI() {
                if (httpRequest instanceof HttpRequestImpl) {
                    return ((HttpRequestImpl) httpRequest).requestRawPath();
                }
                return super.getRequestURI();
            }

            @Override
            public String getContextPath() {
                if (httpRequest instanceof HttpRequestImpl) {
                    return ((HttpRequestImpl) httpRequest).extractContextPath();
                }
                return super.getContextPath();
            }

            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };

        RequestProcessor.getRequestProcessor(wrapper.getServletContext(), RequestProcessor.class.getName())
                .handleRequest(wrapper, response);
    }

    private static class ServletContext implements javax.servlet.ServletContext {
        private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
        private final ClassLoader classloader;

        private ServletContext(final ClassLoader classloader) {
            this.classloader = classloader;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public javax.servlet.ServletContext getContext(String uripath) {
            return null;
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public String getMimeType(String file) {
            return null;
        }

        @Override
        public Set<String> getResourcePaths(String path) {
            return null;
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            return classloader.getResourceAsStream(path);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        @Override
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        @Override
        public Enumeration<Servlet> getServlets() {
            return null;
        }

        @Override
        public Enumeration<String> getServletNames() {
            return null;
        }

        @Override
        public void log(String msg) {

        }

        @Override
        public void log(Exception exception, String msg) {

        }

        @Override
        public void log(String message, Throwable throwable) {

        }

        @Override
        public String getRealPath(String path) {
            return null;
        }

        @Override
        public String getServerInfo() {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return null;
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object object) {
            attributes.put(name, object);
        }

        @Override
        public void removeAttribute(String name) {

        }

        @Override
        public String getServletContextName() {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className) throws IllegalArgumentException, IllegalStateException {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) throws IllegalArgumentException, IllegalStateException {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> clazz) throws IllegalArgumentException, IllegalStateException {
            return null;
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
            return null;
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {
            return null;
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, String className) throws IllegalArgumentException, IllegalStateException {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) throws IllegalArgumentException, IllegalStateException {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) throws IllegalArgumentException, IllegalStateException {
            return null;
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
            return null;
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {
            return null;
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return null;
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {

        }

        @Override
        public void addListener(String className) {

        }

        @Override
        public <T extends EventListener> void addListener(T t) {

        }

        @Override
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
            return null;
        }

        @Override
        public void declareRoles(String... roleNames) {

        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            return null;
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return null;
        }

        @Override
        public int getEffectiveMajorVersion() throws UnsupportedOperationException {
            return 0;
        }

        @Override
        public int getEffectiveMinorVersion() throws UnsupportedOperationException {
            return 0;
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            return null;
        }
    }
}
