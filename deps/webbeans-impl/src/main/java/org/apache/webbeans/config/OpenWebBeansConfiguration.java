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
package org.apache.webbeans.config;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

/**
 * Defines configuration for OpenWebBeans.
 * 
 * The algorithm is easy:
 * <ul>
 * <li>Load all properties you can find with the name (META-INF/openwebbeans/openwebbeans.properties),
 * <li>Sort them via configuration.ordinal in ascending order,
 * <li>Overload them as we do already,
 * <li>Use the sorted list of properties.
 * </ul>
 */
public class OpenWebBeansConfiguration
{
    /**Logger instance*/
    private final static Logger logger = WebBeansLoggerFacade.getLogger(OpenWebBeansConfiguration.class);

    /**Default configuration files*/
    private final static String DEFAULT_CONFIG_PROPERTIES_NAME = "META-INF/openwebbeans/openwebbeans.properties";
    
    /**Property of application*/
    private final Properties configProperties = new Properties();
        
    /**Conversation periodic delay in ms.*/
    public static final String CONVERSATION_PERIODIC_DELAY = "org.apache.webbeans.conversation.Conversation.periodicDelay";
    
    /**Timeout interval in ms*/
    public static final String CONVERSATION_TIMEOUT_INTERVAL = "org.apache.webbeans.conversation.Conversation.timeoutInterval";

    /**
     * Lifycycle methods like {@link javax.annotation.PostConstruct} and
     * {@link javax.annotation.PreDestroy} must not define a checked Exception
     * regarding to the spec. But this is often unnecessary restrictive so we
     * allow to disable this check application wide.
     */
    public static final String INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS = "org.apache.webbeans.forceNoCheckedExceptions";

    /**Use EJB Discovery or not*/
    public static final String USE_EJB_DISCOVERY = "org.apache.webbeans.spi.deployer.useEjbMetaDataDiscoveryService";
    
    /**Container lifecycle*/
    public static final String CONTAINER_LIFECYCLE = "org.apache.webbeans.spi.ContainerLifecycle";
    
    /**JNDI Service SPI*/
    public static final String JNDI_SERVICE = "org.apache.webbeans.spi.JNDIService";    
    
    /**Scanner Service*/
    public static final String SCANNER_SERVICE = "org.apache.webbeans.spi.ScannerService";

    /**Contexts Service*/
    public static final String CONTEXTS_SERVICE = "org.apache.webbeans.spi.ContextsService";
    
    /**Conversation Service*/
    public static final String CONVERSATION_SERVICE = "org.apache.webbeans.spi.ConversationService";
    
    /**Resource Injection Service*/
    public static final String RESOURCE_INJECTION_SERVICE = "org.apache.webbeans.spi.ResourceInjectionService";
    
    /**Security Service*/
    public static final String SECURITY_SERVICE = "org.apache.webbeans.spi.SecurityService";
    
    /**Validator Service*/
    public static final String VALIDATOR_SERVICE = "org.apache.webbeans.spi.ValidatorService";
    
    /**Transaction Service*/
    public static final String TRANSACTION_SERVICE = "org.apache.webbeans.spi.TransactionService";
    
    /**Application is core JSP*/
    public static final String APPLICATION_IS_JSP = "org.apache.webbeans.application.jsp";

    /**Supports conversations*/
    public static final String APPLICATION_SUPPORTS_CONVERSATION = "org.apache.webbeans.application.supportsConversation";

    /**Use of EJB interceptor to inject EJBs*/
    public static final String USE_EJBINTERCEPTOR_INJECTION = "org.apache.webbeans.application.useEJBInterceptorInjection";
    
    /**Use of EJB interceptor to activate EJB contexts*/
    public static final String USE_EJBINTERCEPTOR_ACTIVATION = "org.apache.webbeans.application.useEJBInterceptorActivation";
    
    /**EL Adaptor*/
    public static final String EL_ADAPTOR_CLASS = "org.apache.webbeans.spi.adaptor.ELAdaptor";

    /** prefix followed by the fully qualified scope name, for configuring InterceptorHandlers for our proxies.*/
    public static final String PROXY_MAPPING_PREFIX = "org.apache.webbeans.proxy.mapping.";

    /**
     * Use BDABeansXmlScanner to determine if interceptors, decorators, and
     * alternatives are enabled in the beans.xml of a given BDA. For an
     * application containing jar1 and jar2, this implies that an interceptor
     * enabled in the beans.xml of jar1 is not automatically enabled in jar2
     **/
    public static final String USE_BDA_BEANSXML_SCANNER = "org.apache.webbeans.useBDABeansXMLScanner";

    /**
     * a comma-separated list of fully qualified class names that should be ignored
     * when determining if a decorator matches its delegate.  These are typically added by
     * weaving or bytecode modification.
     */
    public static final String IGNORED_INTERFACES = "org.apache.webbeans.ignoredDecoratorInterfaces";

    private Set<String> ignoredInterfaces;

    /**
     * you can configure this externally as well.
     *
     * @param properties
     */
    public OpenWebBeansConfiguration(Properties properties)
    {
        configProperties.putAll(properties);
    }

    /**
     * Parse configuration.
     */
    public OpenWebBeansConfiguration()
    {
        parseConfiguration();
        
        logger.fine("Overriding properties from System properties");
        
        //Look for System properties
        loadFromSystemProperties();        
    }
    
    /**
     * Load from system properties
     */
    private void loadFromSystemProperties()
    {
        Properties properties;
        if(System.getSecurityManager() != null)
        {
            properties = doPrivilegedGetSystemProperties();
        }
        else
        {
            properties = System.getProperties();
        }
        
        String value = properties.getProperty(CONVERSATION_PERIODIC_DELAY);
        setPropertyFromSystemProperty(CONVERSATION_PERIODIC_DELAY, value);        
        
        value = properties.getProperty(USE_EJB_DISCOVERY);
        setPropertyFromSystemProperty(USE_EJB_DISCOVERY, value);
        
        value = properties.getProperty(USE_EJBINTERCEPTOR_INJECTION);
        setPropertyFromSystemProperty(USE_EJBINTERCEPTOR_INJECTION, value);
        
        value = properties.getProperty(USE_EJBINTERCEPTOR_ACTIVATION);
        setPropertyFromSystemProperty(USE_EJBINTERCEPTOR_ACTIVATION, value);
        
        value = properties.getProperty(CONTAINER_LIFECYCLE);
        setPropertyFromSystemProperty(CONTAINER_LIFECYCLE, value);

        value = properties.getProperty(APPLICATION_IS_JSP);
        setPropertyFromSystemProperty(APPLICATION_IS_JSP, value);

        value = properties.getProperty(TRANSACTION_SERVICE);
        setPropertyFromSystemProperty(TRANSACTION_SERVICE, value);

        value = properties.getProperty(VALIDATOR_SERVICE);
        setPropertyFromSystemProperty(VALIDATOR_SERVICE, value);

        value = properties.getProperty(SECURITY_SERVICE);
        setPropertyFromSystemProperty(SECURITY_SERVICE, value);

        value = properties.getProperty(RESOURCE_INJECTION_SERVICE);
        setPropertyFromSystemProperty(RESOURCE_INJECTION_SERVICE, value);

        value = properties.getProperty(CONVERSATION_SERVICE);
        setPropertyFromSystemProperty(CONVERSATION_SERVICE, value);

        value = properties.getProperty(CONTEXTS_SERVICE);
        setPropertyFromSystemProperty(CONTEXTS_SERVICE, value);

        value = properties.getProperty(SCANNER_SERVICE);
        setPropertyFromSystemProperty(SCANNER_SERVICE, value);

        value = properties.getProperty(JNDI_SERVICE);
        setPropertyFromSystemProperty(JNDI_SERVICE, value);
        
        value = properties.getProperty(EL_ADAPTOR_CLASS);
        setPropertyFromSystemProperty(EL_ADAPTOR_CLASS, value);

        value = properties.getProperty(USE_BDA_BEANSXML_SCANNER);
        setPropertyFromSystemProperty(USE_BDA_BEANSXML_SCANNER, value);

    }

    private Properties doPrivilegedGetSystemProperties()
    {
        return AccessController.doPrivileged(
                new PrivilegedAction<Properties>()
                    {
                        public Properties run()
                        {
                            return System.getProperties();
                        }

                    }
                );
    }

     
    private void setPropertyFromSystemProperty(String key, String value)
    {
        if(value != null)
        {
            setProperty(key, value);
        }
    }
    
    /**
     * (re)read the configuration from the resources in the classpath.
     * @see #DEFAULT_CONFIG_PROPERTIES_NAME
     * @see #DEFAULT_CONFIG_PROPERTIES_NAME
     */
    public synchronized void parseConfiguration() throws WebBeansConfigurationException
    {
        Properties newConfigProperties = PropertyLoader.getProperties(DEFAULT_CONFIG_PROPERTIES_NAME);
        configProperties.clear();

        // set the new one as perfect fit.
        if(newConfigProperties != null)
        {
            configProperties.putAll(newConfigProperties);
        }
    }
    

    /**
     * Gets property.
     * @param key
     * @return String with the property value or <code>null</code>
     */
    public String getProperty(String key)
    {
        return configProperties.getProperty(key);
    }
    
    /**
     * Gets property value.
     * @param key
     * @param defaultValue
     * @return String with the property value or <code>null</code>
     */
    public String getProperty(String key,String defaultValue)
    {
        return configProperties.getProperty(key, defaultValue);
    }
    
    
    /**
     * Sets given property.
     * @param key property name
     * @param value property value
     */
    public synchronized void setProperty(String key, Object value)
    {
        configProperties.put(key, value);
    }
    

    /**
     * Gets jsp property.
     * @return true if jsp
     */
    public boolean isJspApplication()
    {
        String value = getProperty(APPLICATION_IS_JSP);
        
        return Boolean.valueOf(value);
    }
    
    /**
     * Gets conversation supports property.
     * @return true if supports
     */
    public boolean supportsConversation()
    {
        String value = getProperty(APPLICATION_SUPPORTS_CONVERSATION);
        
        return Boolean.valueOf(value);
    }
    
    /**
     * Gets EJB injection property.
     * @return true if EJB interceptor should do injection
     */
    public boolean isUseEJBInterceptorInjection()
    {
        String value = getProperty(USE_EJBINTERCEPTOR_INJECTION);
        
        return Boolean.valueOf(value);
    }
    
    /**
     * Gets EJB context activation property.
     * @return true if EJB interceptor should do activation of EJB contexts
     */
    public boolean isUseEJBInterceptorActivation()
    {
        String value = getProperty(USE_EJBINTERCEPTOR_ACTIVATION);
        
        return Boolean.valueOf(value);
    }

    public synchronized Set<String> getIgnoredInterfaces()
    {
        if (ignoredInterfaces == null)
        {
            String ignoredInterfacesString = getProperty(IGNORED_INTERFACES);
            if (ignoredInterfacesString != null)
            {
                ignoredInterfaces = new HashSet<String>(Arrays.asList(ignoredInterfacesString.split("[,\\p{javaWhitespace}]")));
            }
            else
            {
                ignoredInterfaces = Collections.emptySet();
            }
        }
        return ignoredInterfaces;
    }

}
