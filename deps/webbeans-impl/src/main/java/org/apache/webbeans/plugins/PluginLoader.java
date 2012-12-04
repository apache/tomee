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
package org.apache.webbeans.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansWebPlugin;

/**
 * <p>OpenWebBeans plugins are used to extend 'core' functionality of the injection
 * mechanism with functionality of other frameworks.</p>
 * 
 * <p>Core functionality are all parts which are available in a standard
 * JDK-1.5 SE runtime. Extended functionality are things like JPA, JSF, EJB etc.</p>
 * 
 * <p>The plugin mechanism is based on the ServiceProvider functionality 
 * {@link http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider}.
 * A jar containing an OWB plugin has to expose itself in the file
 * <code>META-INF/services/org.apache.webbeans.plugins.OpenWebBeansPlugin</code></p> 
 */
public class PluginLoader
{
    /**Logger instance*/
    private static final Logger logger = WebBeansLoggerFacade.getLogger(PluginLoader.class);

    /** unmodifiable list with all found OWB plugins */
    private List<OpenWebBeansPlugin> plugins = null;

    private AtomicBoolean started = new AtomicBoolean(false);

    public PluginLoader()
    {
    }    

    /**
     * load and startup all registered plugins.
     * This must be called once the WebApplication is started.
     * @throws WebBeansConfigurationException
     */
    public void startUp() throws WebBeansConfigurationException
    {
        if(started.compareAndSet(false, true))
        {
            logger.fine("PluginLoader startUp called.");
            ArrayList<OpenWebBeansPlugin> ps = new ArrayList<OpenWebBeansPlugin>();

            List<OpenWebBeansPlugin> pluginList = WebBeansContext.getInstance().getLoaderService().load(OpenWebBeansPlugin.class);
            for (OpenWebBeansPlugin plugin : pluginList)
            {
                if (logger.isLoggable(Level.INFO))
                {
                    logger.log(Level.INFO, OWBLogConst.INFO_0004, plugin.getClass().getSimpleName());
                }
                try
                {
                    plugin.startUp();
                }
                catch (Exception e)
                {
                    throwsException(e);
                }
                ps.add(plugin);
            }   
            
            // just to make sure the plugins aren't modified afterwards
            plugins = Collections.unmodifiableList(ps);            
        }
        else
        {
            logger.fine("PluginLoader is already started.");
        }
    }
    
    public static void throwsException(Exception e) throws WebBeansConfigurationException
    {
        if(e instanceof WebBeansConfigurationException)
        {
            throw (WebBeansConfigurationException)e;
        }
        else
        {
            throw new WebBeansConfigurationException(e);
        }
    }
    
    /**
     * Tell all the plugins to free up all locked resources.
     * This must be called before the WebApplication gets undeployed or stopped.
     * @throws WebBeansConfigurationException
     */
    public void shutDown() throws WebBeansConfigurationException
    {
        if(started.compareAndSet(true, false))
        {
            logger.fine("PluginLoader shutDown called.");
            
            if (plugins == null)
            {
                logger.warning(OWBLogConst.WARN_0001);
                return;
            }

            ArrayList<String> failedShutdown = new ArrayList<String>();

            for (OpenWebBeansPlugin plugin : plugins)
            {
                try 
                {
                    plugin.shutDown();
                }
                catch (Exception e)
                {
                    // we catch ALL exceptions, since we like to continue shutting down all other plugins!
                    String pluginName = plugin.getClass().getSimpleName();
                    logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0009, pluginName), e);
                    failedShutdown.add(pluginName);
                }
            }
            
            if (!failedShutdown.isEmpty())
            {
                throw new WebBeansConfigurationException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0006) + failedShutdown.toString());
            }            
        }
        else
        {
            logger.fine("PluginLoader is already shut down.");
        }
    }
    
    /**
     * @return an unmodifiable list of all registered {code OpenWebBeansPlugin}s
     */
    public List<OpenWebBeansPlugin> getPlugins()
    {
        return plugins;
    }
 
    /**
     * Gets ejb plugin
     * 
     * @return ejb plugin
     */
    public OpenWebBeansEjbPlugin getEjbPlugin()
    {
        if(!pluginsExist())
        {
            return null;
        }
        
        for(OpenWebBeansPlugin plugin : plugins)
        {
            if(plugin instanceof OpenWebBeansEjbPlugin)
            {
                return (OpenWebBeansEjbPlugin)plugin;
            }
        }
        
        return null;
    }
    
    /**
     * Gets ejb lifecycle annotations plugin
     * 
     * @return ejb LCA plugin
     */
    public OpenWebBeansEjbLCAPlugin getEjbLCAPlugin()
    {
        if(!pluginsExist())
        {
            return null;
        }
        
        for(OpenWebBeansPlugin plugin : plugins)
        {
            if(plugin instanceof OpenWebBeansEjbLCAPlugin)
            {
                return (OpenWebBeansEjbLCAPlugin)plugin;
            }
        }
        
        return null;
    }   
    /**
     * Gets the JMS plugin
     * 
     * @return jms plugin
     */
    public OpenWebBeansJmsPlugin getJmsPlugin()
    {
        if(!pluginsExist())
        {
            return null;
        }
        
        for(OpenWebBeansPlugin plugin : plugins)
        {
            if(plugin instanceof OpenWebBeansJmsPlugin)
            {
                return (OpenWebBeansJmsPlugin)plugin;
            }
        }
        
        return null;
    }
    
    
    public OpenWebBeansJavaEEPlugin getJavaEEPlugin()
    {
        if(!pluginsExist())
        {
            return null;
        }
        
        for(OpenWebBeansPlugin plugin : plugins)
        {
            if(plugin instanceof OpenWebBeansJavaEEPlugin)
            {
                return (OpenWebBeansJavaEEPlugin)plugin;
            }
        }
        
        return null;        
    }
    
    public OpenWebBeansWebPlugin getWebPlugin()
    {
        if(!pluginsExist())
        {
            return null;
        }
        
        for(OpenWebBeansPlugin plugin : plugins)
        {
            if(plugin instanceof OpenWebBeansWebPlugin)
            {
                return (OpenWebBeansWebPlugin)plugin;
            }
        }
        
        return null;        
    }    
    
    private boolean pluginsExist()
    {
        if(plugins == null)
        {
            return false;
        }
        
        return true;
    }
}
