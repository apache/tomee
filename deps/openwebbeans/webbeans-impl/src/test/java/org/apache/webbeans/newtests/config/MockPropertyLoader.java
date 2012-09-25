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
package org.apache.webbeans.newtests.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.webbeans.config.PropertyLoader;

/**
 * 
 * For tesing pruposes. Methods are taken
 * from {@link PropertyLoader} class.
 *
 */
class MockPropertyLoader
{
    /**
     * Implement a quick and dirty sorting mechanism for the given Properties.
     * @param allProperties
     * @return the Properties list sorted by it's 'configuration.ordinal' in ascending order.
     */
    static List<Properties> sortProperties(List<Properties> allProperties)
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
    static int getConfigurationOrdinal(Properties p)
    {
        int configOrder = PropertyLoader.CONFIGURATION_ORDINAL_DEFAULT_VALUE;

        String configOrderString = p.getProperty(PropertyLoader.CONFIGURATION_ORDINAL_PROPERTY_NAME);
        if (configOrderString != null && configOrderString.length() > 0)
        {
            try
            {
                configOrder = Integer.parseInt(configOrderString);
            }
            catch(NumberFormatException nfe)
            {
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
    static Properties mergeProperties(List<Properties> sortedProperties)
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
