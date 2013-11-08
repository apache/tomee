/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.conf.test;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Properties;

import org.apache.openjpa.lib.conf.AbstractProductDerivation;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.MapConfigurationProvider;
import org.apache.openjpa.lib.conf.ProductDerivation;

/**
 * A Product Derivation to test loading of global and default configuration with
 * System settings.  Reads its global from a file specified by 
 * <code>"openjpatest.properties"</code> system property.
 *
 * @author Pinaki Poddar
 * @author Abe White
 */
public class ConfigurationTestProductDerivation 
    extends AbstractProductDerivation {
    
    public static boolean closed = false;

    public int getType() {
        return ProductDerivation.TYPE_PRODUCT;
    }
    
    public void beforeConfigurationClose(Configuration conf) {
        closed = true;
    }

    public ConfigurationProvider loadGlobals(ClassLoader loader)
        throws IOException {
        return load(null, loader);
    }

    public ConfigurationProvider load(String rsrc, ClassLoader loader)
        throws IOException {
        if (rsrc == null)
            rsrc = System.getProperty("openjpatest.properties");
        if (rsrc == null || !rsrc.endsWith(".properties"))
            return null;

        URL url = findResource(rsrc, loader);
        if (url == null)
            throw new MissingResourceException(rsrc, getClass().getName(), 
                rsrc);

        InputStream in = url.openStream();
        Properties props = new Properties();
        if (in != null) {
            try {
                props.load(in);
                return new MapConfigurationProvider(props);
            } finally {
                try { in.close(); } catch (Exception e) {}
            }
        }
        return null;
    }

    /**
     * Locate the given resource.
     */
    private URL findResource(String rsrc, ClassLoader loader)
        throws IOException {
        if (loader != null)
            return loader.getResource(rsrc);

        // in jbuilder the classloader can be null
        URL url = null;
        loader = getClass().getClassLoader();
        if (loader != null)
            url = loader.getResource(rsrc);
        if (url == null) {
            loader = Thread.currentThread().getContextClassLoader();
            if (loader != null)
                url = loader.getResource(rsrc);
        }
        if (url == null) {
            loader = ClassLoader.getSystemClassLoader();
            if (loader != null)
                url = loader.getResource(rsrc);
        }
        return url;
    }
}
