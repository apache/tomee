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
package org.apache.webbeans.logger;

/*
 * These are for use of JDK util logging.
 */

import org.apache.webbeans.config.OWBLogConst;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class around the JUL logger class to include some checks before the
 * logs are actually written.
 * <p>
 * Actually, it is a thin layer on the JUL {@link Logger} implementation.
 * </p>
 *
 * @version $Rev$ $Date$
 */
public final class WebBeansLoggerFacade
{
    public static final String OPENWEBBEANS_LOGGING_FACTORY_PROP = "openwebbeans.logging.factory";

    private static final WebBeansLoggerFactory FACTORY;

    static final ResourceBundle WB_BUNDLE = ResourceBundle.getBundle("openwebbeans/Messages");

    static {
        final String factoryClassname = System.getProperty(OPENWEBBEANS_LOGGING_FACTORY_PROP);
        WebBeansLoggerFactory factory = null;
        Exception error = null;
        if (factoryClassname != null)
        {
            try
            {
                // don't use the org.apache.webbeans.util.WebBeansUtil.getCurrentClassLoader()
                // to avoid weird dependency and potential failing
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                if(classloader == null)
                {
                    classloader = WebBeansLoggerFacade.class.getClassLoader();
                }
                Class<?> factoryClazz = classloader.loadClass(factoryClassname);
                factory = (WebBeansLoggerFactory) factoryClazz.newInstance();
            }
            catch (Exception e)
            {
                error = e;
            }
        }
        if (factory != null)
        {
            FACTORY = factory;
        }
        else
        {
            FACTORY = new JULLoggerFactory();
        }

        final Logger logger = FACTORY.getLogger(WebBeansLoggerFacade.class);
        if (error != null && logger.isLoggable(Level.SEVERE))
        {
            logger.log(Level.SEVERE, OWBLogConst.ERROR_0028, error);
        }
    }

    /**
     * Gets the new web beans logger instance.
     * 
     * @param clazz own the return logger
     * @return new logger
     */
    public static Logger getLogger(Class<?> clazz)
    {
        return FACTORY.getLogger(clazz);
    }

    /**
     * Gets the new web beans logger instance.
     * 
     * @param clazz own the return logger
     * @param desiredLocale Locale used to select the Message resource bundle. 
     * @return new logger
     */
    public static Logger getLogger(Class<?> clazz, Locale desiredLocale)
    {
        return FACTORY.getLogger(clazz, desiredLocale);
    }

    public static String constructMessage(String messageKey, Object... args)
    {
        MessageFormat msgFrmt;
        String formattedString;

        msgFrmt = new MessageFormat(getTokenString(messageKey), Locale.getDefault());
        formattedString = msgFrmt.format(args);

        return formattedString;
    }

    public static String getTokenString(String messageKey)
    {
        String strVal;

        if (WB_BUNDLE == null)
        {
            throw new NullPointerException("ResourceBundle can not be null");
        }
        try
        {
            strVal = WB_BUNDLE.getString(messageKey);
        }
        catch (MissingResourceException mre)
        {
            strVal = null;
        }
        if (strVal == null)
        {
            return messageKey;
        }

        return strVal;
    }

    // helper method
    public static Object[] args(final Object... values)
    {
        return values;
    }
}
