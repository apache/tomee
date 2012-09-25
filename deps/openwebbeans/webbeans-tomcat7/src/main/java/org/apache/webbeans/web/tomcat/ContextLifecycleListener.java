/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.web.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.naming.ContextAccessController;
import org.apache.tomcat.InstanceManager;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.servlet.WebBeansConfigurationListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Context lifecycle listener. Adapted from
 * OpenEJB Tomcat and updated.
 * 
 * @version $Rev$ $Date$
 *
 */
public class ContextLifecycleListener implements PropertyChangeListener, LifecycleListener, ContainerListener, ServletContextAttributeListener
{

    private StandardServer standardServer;

    public ContextLifecycleListener()
    {
    }

    public void lifecycleEvent(LifecycleEvent event)
    {
        try
        {
            if (event.getSource() instanceof StandardServer)
            {
                if (event.getType().equals(Lifecycle.START_EVENT))
                {
                    this.standardServer = (StandardServer) event.getSource();
                    start();
                }
            }
            else if (event.getSource() instanceof StandardContext)
            {
                StandardContext context = (StandardContext) event.getSource();

                if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT))
                {
                    ServletContext scontext = context.getServletContext();
                    URL url = getBeansXml(scontext);
                    if (url != null)
                    {
                        //Registering ELResolver with JSP container
                        System.setProperty("org.apache.webbeans.application.jsp", "true");

                        String[] oldListeners = context.findApplicationListeners();
                        LinkedList<String> listeners = new LinkedList<String>();

                        listeners.addFirst(WebBeansConfigurationListener.class.getName());

                        for(String listener : oldListeners)
                        {
                            listeners.add(listener);
                            context.removeApplicationListener(listener);
                        }
                        
                        for(String listener : listeners)
                        {
                            context.addApplicationListener(listener);
                        }                        
                        
                        context.addApplicationListener(TomcatSecurityListener.class.getName());
                        context.addApplicationEventListener(this);
                        //context.addInstanceListener(TomcatInstanceListener.class.getName());             
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }        
    }

    private URL getBeansXml(ServletContext scontext) throws MalformedURLException
    {
        URL url = scontext.getResource("/WEB-INF/beans.xml");
        if (url == null)
        {
            url = scontext.getResource("/WEB-INF/classes/META-INF/beans.xml");
        }
        return url;
    }

    public void containerEvent(ContainerEvent event)
    {
        StandardContext context;
        try
        {
            if(event.getSource() instanceof StandardContext)
            {               
                context = (StandardContext)event.getSource();

                if(event.getType().equals("beforeContextInitialized"))
                {
                    ClassLoader loader = context.getLoader().getClassLoader();
                    Object listener = event.getData();
                    
                    if(listener.getClass().getName().equals(WebBeansConfigurationListener.class.getName()))
                    {
                       ContextAccessController.setWritable(context.getNamingContextListener().getName(), context);                       
                       return;
                    }
                    else
                    {
                        URL url = getBeansXml(context.getServletContext());
                        if(url != null)
                        {
                            TomcatUtil.inject(listener, loader);   
                        }
                    }
                }
                else if(event.getType().equals("afterContextInitialized"))
                {
                    ClassLoader loader = context.getLoader().getClassLoader();
                    Object listener = event.getData();
                    if(listener.getClass().getName().equals(WebBeansConfigurationListener.class.getName()))
                    {
                        setInstanceManager(context);
                        
                        ContextAccessController.setReadOnly(context.getNamingContextListener().getName());

                        URL url = getBeansXml(context.getServletContext());
                        if(url != null)
                        {
                            Object[] listeners = context.getApplicationEventListeners();
                            for(Object instance : listeners)
                            {
                                if(!instance.getClass().getName().equals(WebBeansConfigurationListener.class.getName()))
                                {                                
                                    TomcatUtil.inject(instance, loader);   
                                }
                            }                                                                                    
                        }                        
                    }                                
                }
                else if(event.getType().equals("beforeContextDestroyed"))
                {
                    Object listener = event.getData();
                    if(listener.getClass().getName().equals(WebBeansConfigurationListener.class.getName()))
                    {
                        ContextAccessController.setWritable(context.getNamingContextListener().getName(),context);   
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setInstanceManager(final StandardContext context)
    {
        if (context.getInstanceManager() instanceof TomcatInstanceManager)
        {
            return;
        }

        InstanceManager processor = context.getInstanceManager();
        InstanceManager custom = new TomcatInstanceManager(context.getLoader().getClassLoader(), processor);
        context.setInstanceManager(custom);

        context.getServletContext().setAttribute(InstanceManager.class.getName(), custom);
    }

    public void start()
    {
        // hook the hosts so we get notified before contexts are started
        standardServer.addPropertyChangeListener(this);
        standardServer.addLifecycleListener(this);
        for (Service service : standardServer.findServices())
        {
            serviceAdded(service);
        }
    }

    public void stop()
    {
        standardServer.removePropertyChangeListener(this);
    }

    private void serviceAdded(Service service)
    {
        Container container = service.getContainer();
        if (container instanceof StandardEngine)
        {
            StandardEngine engine = (StandardEngine) container;
            engineAdded(engine);
        }
    }

    private void engineAdded(StandardEngine engine)
    {
        addContextListener(engine);
        for (Container child : engine.findChildren())
        {
            if (child instanceof StandardHost)
            {
                StandardHost host = (StandardHost) child;
                hostAdded(host);
            }
        }
    }

    private void hostAdded(StandardHost host)
    {
        addContextListener(host);
        host.addLifecycleListener(this);
        for (Container child : host.findChildren())
        {
            if (child instanceof StandardContext)
            {
                StandardContext context = (StandardContext) child;
                contextAdded(context);
            }
        }
    }

    private void contextAdded(StandardContext context)
    {
        // put this class as the first listener so we can process the
        // application before any classes are loaded
        forceFirstLifecycleListener(context);
    }

    private void forceFirstLifecycleListener(StandardContext context)
    {
        LifecycleListener[] listeners = context.findLifecycleListeners();

        // if we are already first return
        if (listeners.length > 0 && listeners[0] == this)
        {
            return;
        }

        // remove all of the current listeners
        for (LifecycleListener listener : listeners)
        {
            context.removeLifecycleListener(listener);
        }

        // add this class (as first)
        context.addLifecycleListener(this);
        context.addContainerListener(this);

        // add back all listeners
        for (LifecycleListener listener : listeners)
        {
            if (listener != this)
            {
                context.addLifecycleListener(listener);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        if ("service".equals(event.getPropertyName()))
        {
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            if (oldValue == null && newValue instanceof Service)
            {
                serviceAdded((Service) newValue);
            }
        }
        if ("children".equals(event.getPropertyName()))
        {
            Object source = event.getSource();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            if (source instanceof StandardEngine)
            {
                if (oldValue == null && newValue instanceof StandardHost)
                {
                    hostAdded((StandardHost) newValue);
                }
            }
            if (source instanceof StandardHost)
            {
                if (oldValue == null && newValue instanceof StandardContext)
                {
                    contextAdded((StandardContext) newValue);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addContextListener(ContainerBase containerBase)
    {
        try
        {
            Field field = (Field)AccessController.doPrivileged(new PrivilegedActionForClass(ContainerBase.class, "children"));
            AccessController.doPrivileged(new PrivilegedActionForAccessibleObject(field, true));
            Map<Object,Object> children = (Map<Object,Object>) field.get(containerBase);
            if (children instanceof ContextLifecycleListener.MoniterableHashMap)
            {
                return;
            }
            children = new ContextLifecycleListener.MoniterableHashMap(children, containerBase, "children", this);
            field.set(containerBase, children);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public void attributeAdded(ServletContextAttributeEvent servletContextAttributeEvent)
    {
        if (InstanceManager.class.getName().equals(servletContextAttributeEvent.getName()))
        { // used as a hook to know we can override eagerly the InstanceManager
            try
            {
                final StandardContext context = (StandardContext) getContext(
                                            getContext(servletContextAttributeEvent.getServletContext()));
                setInstanceManager(context);
            }
            catch (NoSuchFieldException e)
            {
                throw new WebBeansException(e.getMessage(), e);
            }
            catch (IllegalAccessException e)
            {
                throw new WebBeansException(e.getMessage(), e);
            }
        }
    }

    private static Object getContext(final Object o) throws NoSuchFieldException, IllegalAccessException
    {
        final Field getContext = o.getClass().getDeclaredField("context");
        final boolean acc = getContext.isAccessible();
        getContext.setAccessible(true);
        try
        {
            return getContext.get(o);
        }
        finally
        {
            getContext.setAccessible(acc);
        }
    }

    public void attributeRemoved(ServletContextAttributeEvent servletContextAttributeEvent)
    {
        // no-op
    }

    public void attributeReplaced(ServletContextAttributeEvent servletContextAttributeEvent)
    {
        // no-op
    }

    public static class MoniterableHashMap extends HashMap<Object,Object>
    {
        private static final long serialVersionUID = 1L;

        private final Object source;
        private final String propertyName;
        private final PropertyChangeListener listener;

        public MoniterableHashMap(Map<Object,Object> m, Object source, String propertyName, PropertyChangeListener listener)
        {
            super(m);
            this.source = source;
            this.propertyName = propertyName;
            this.listener = listener;
        }

        public Object put(Object key, Object value)
        {
            Object oldValue = super.put(key, value);
            PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, null, value);
            listener.propertyChange(event);
            return oldValue;
        }

        public Object remove(Object key)
        {
            Object value = super.remove(key);
            PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, value, null);
            listener.propertyChange(event);
            return value;
        }
    }

    protected static class PrivilegedActionForAccessibleObject implements PrivilegedAction<Object>
    {

        AccessibleObject object;

        boolean flag;

        protected PrivilegedActionForAccessibleObject(AccessibleObject object, boolean flag)
        {
            this.object = object;
            this.flag = flag;
        }

        public Object run()
        {
            object.setAccessible(flag);
            return null;
        }
    }

    protected static class PrivilegedActionForClass implements PrivilegedAction<Object>
    {
        Class<?> clazz;

        Object parameters;

        protected PrivilegedActionForClass(Class<?> clazz, Object parameters)
        {
            this.clazz = clazz;
            this.parameters = parameters;
        }

        public Object run()
        {
            try
            {
                return clazz.getDeclaredField((String)parameters);
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    
}
