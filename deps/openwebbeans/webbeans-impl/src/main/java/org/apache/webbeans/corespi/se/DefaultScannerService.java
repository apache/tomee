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
package org.apache.webbeans.corespi.se;

import java.io.IOException;

import org.apache.webbeans.corespi.scanner.AbstractMetaDataDiscovery;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.WebBeansUtil;

public class DefaultScannerService extends AbstractMetaDataDiscovery
{

    public DefaultScannerService()
    {
        super();
    }

    protected void configure()
    {
        configureAnnotationDB();
    }

    
    private void configureAnnotationDB()
    {
        ClassLoader loader = WebBeansUtil.getCurrentClassLoader();
        //Store collection of beans.xml's before scanning archives

        String[] urlPaths = findBeansXmlBases(META_INF_BEANS_XML, loader);

        try
        {
            getAnnotationDB().scanArchives(urlPaths);
        }
        catch (IOException e)
        {
            throw new WebBeansConfigurationException("Error while scanning the JAR archives", e);
        }
    }

}
