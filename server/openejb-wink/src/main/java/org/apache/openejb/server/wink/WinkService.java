package org.apache.openejb.server.wink;

import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.server.rest.RsHttpListener;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WinkService extends RESTService {
    @Override
    protected RsHttpListener createHttpListener() {
        return new WinkHttpListener();
    }

    @Override
    public String getName() {
        return "wink";
    }

    public static class StandaloneServletContext implements ServletContext {
        private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
        private ClassLoader classloader;

        public StandaloneServletContext(ClassLoader classloader) {
            this.classloader = classloader;
        }

        @Override
        public InputStream getResourceAsStream(final String path) {
            return classloader.getResourceAsStream(path);
        }

        @Override
        public Object getAttribute(final String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(final String name, final Object object) {
            attributes.put(name, object);
        }

        // mocks

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public ServletContext getContext(String uripath) {
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
        public Enumeration<String> getAttributeNames() {
            return null;
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
