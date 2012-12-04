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

import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.WebBeansUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Utility class to load configuration properties via a list of
 * artibrary property files by a well defined order.</p>
 * <p>User configurations should start with 'configuration.ordinal'
 * greather than 100.</p>
 *
 */
public class PropertyLoader
{
    public final static int CONFIGURATION_ORDINAL_DEFAULT_VALUE = 100;

    public final static String CONFIGURATION_ORDINAL_PROPERTY_NAME = "configuration.ordinal";


    private static final Logger logger = WebBeansLoggerFacade.getLogger(PropertyLoader.class);


    private PropertyLoader()
    {
        // utility class doesn't have a public ct
    }

    /**
     * <p>Look for all property files with the given name (e.g. 'myconfig.properties') in
     * the classpath. Then load all properties files and sort them by their ascending
     * configuration order and apply them in this order.</p>
     *
     * <p>The idea is to be able to 'override' properties by just providing
     * a new properties file with the same name but a higher 'configuration.ordinal'
     * than the old one.</p>
     *
     * <p>If a property file defines no 'configuration.ordinal' property than a default
     * value of {@link #CONFIGURATION_ORDINAL_DEFAULT_VALUE} is assumed. Any sensitive
     * default which is provided by the system parsing for the configuration should
     * have a 'configuration.ordinal' value lower than 10. In most cases a value of 1</p>
     *
     * <p>If 2 property files have the same ordinal 'configuraiton.order' the outcome
     * is not really defined. The Properties file which got found first will be
     * processed first and thus get overwritten by the one found later.</p> 
     *
     * @param propertyFileName the name of the properties file
     * @return the final property values
     */
    public static synchronized Properties getProperties(String propertyFileName)
    {
        try
        {
            List<Properties> allProperties = loadAllProperties(propertyFileName);
            if (allProperties == null)
            {
                return null;
            }
            
            List<Properties> sortedProperties = sortProperties(allProperties);
            Properties properties = mergeProperties(sortedProperties);
            return properties;
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error while loading the propertyFile " + propertyFileName, e);
            return null;
        }
    }

    private static List<Properties> loadAllProperties(String propertyFileName)
            throws IOException
    {
        ClassLoader cl = WebBeansUtil.getCurrentClassLoader();
        Enumeration<URL> propertyUrls = cl.getResources(propertyFileName);
        if (propertyUrls == null || !propertyUrls.hasMoreElements())
        {
            if(logger.isLoggable(Level.INFO))
            {
                logger.info("could not find any property files with name " + propertyFileName);   
            }
            
            return null;
        }

        List<Properties> properties = new ArrayList<Properties>();

        while (propertyUrls.hasMoreElements())
        {
            URL propertyUrl = propertyUrls.nextElement();
            InputStream is = null;
            try
            {
                is = propertyUrl.openStream();
                Properties prop = new Properties();
                prop.load(is);
                properties.add(prop);

                // a bit debugging output
                int ordinal = getConfigurationOrdinal(prop);
                if(logger.isLoggable(Level.FINE))
                {
                    logger.fine("loading properties with ordinal " + ordinal + " from file " + propertyUrl.getFile());
                }
            }
            finally
            {
                if (is != null)
                {
                    is.close();
                }
            }
        }

        return properties;
    }

    /**
     * Implement a quick and dirty sorting mechanism for the given Properties.
     * @param allProperties
     * @return the Properties list sorted by it's 'configuration.ordinal' in ascending order.
     */
    private static List<Properties> sortProperties(List<Properties> allProperties)
    {
        List<Properties> sortedProperties = new ArrayList<Properties>();
        for (Properties p : allProperties)
        {
            int configOrder = getConfigurationOrdinal(p);

            int i;
            for (i = 0; i < sortedProperties.size(); i++)
            {
                int listConfigOrder = getConfigurationOrdinal(sortedProperties.get(i));
                if (listConfigOrder > configOrder)
                {
                    // only go as far as we found a higher priority Properties file
                    break;
                }
            }
            sortedProperties.add(i, p);
        }
        return sortedProperties;
    }

    /**
     * Determine the 'configuration.ordinal' of the given properties.
     * {@link #CONFIGURATION_ORDINAL_DEFAULT_VALUE} if
     * {@link #CONFIGURATION_ORDINAL_PROPERTY_NAME} is not set in the
     * Properties file.
     *
     * @param p the Properties from the file.
     * @return the ordinal number of the given Properties file.
     */
    private static int getConfigurationOrdinal(Properties p)
    {
        int configOrder = CONFIGURATION_ORDINAL_DEFAULT_VALUE;

        String configOrderString = p.getProperty(CONFIGURATION_ORDINAL_PROPERTY_NAME);
        if (configOrderString != null && configOrderString.length() > 0)
        {
            try
            {
                configOrder = Integer.parseInt(configOrderString);
            }
            catch(NumberFormatException nfe)
            {
                logger.severe(CONFIGURATION_ORDINAL_PROPERTY_NAME + " must be an integer value!");
                throw nfe;
            }
        }

        return configOrder;
    }

    /**
     * Merge the given Properties in order of appearance.
     * @param sortedProperties
     * @return the merged Properties
     */
    private static Properties mergeProperties(List<Properties> sortedProperties)
    {
        Properties mergedProperties = new Properties();
        for (Properties p : sortedProperties)
        {
            for (Map.Entry<?,?> entry : p.entrySet())
            {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                // simply overwrite the old properties with the new ones.
                mergedProperties.setProperty(key, value);
            }
        }

        return mergedProperties;
    }

}
