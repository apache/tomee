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
package org.apache.openjpa.persistence.test;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.PersistenceProductDerivation;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.apache.openjpa.persistence.PersistenceUnitInfoImpl;

/*
 * This abstract test framework class provides scaffolding for using
 * PersistenceProvider.createContainerEntityManagerFactory from within
 * a JSE environment.  Tests which extend this class can specify a non-default
 * persistence file name by overriding the getPersistenceResourceName() 
 * method.
 * 
 */
public abstract class ContainerEMFTest extends SingleEMFTestCase {

    // Use this constant as a config map key to specify a list of persistent 
    // classes.  For example:
    // List<Class<?>> clist = new ArrayList<Class<?>>();
    // clist.add(com.my.Cls.class);
    // clist.add(com.my.OtherCls.class);
    // cfgMap.put(PERSISTENT_CLASS_LIST, clist);
    // emf = createContainerEMF("pu","persistence.xml", cfgMap);
    public static final String PERSISTENT_CLASS_LIST = "PERSISTENT_CLASS_LIST";
    
    // The persistence file name to parse.  This file gets parsed
    // and converted into one or more PersistenceUnitInfo's
    public String getPersistenceResourceName() {
        return PersistenceProductDerivation.RSRC_DEFAULT;
    }
    
    // Creates an EMF through the typical container path.  This EMF may be
    // a bit quirky, but provides some level of testing from within a JSE
    // environment.
    @Override
    public void setUp(Object...props) {
        Map<String, Object> map = getPropertiesMap(props);
        emf = createContainerEMF(getPersistenceUnitName(),
            getPersistenceResourceName(), map);
    }

    // Create an EMF through the path a container normally takes - calling
    // createContainerEntityManagerFactory directly on the persistence impl.
    @SuppressWarnings("unchecked")
    protected OpenJPAEntityManagerFactorySPI createContainerEMF(final String pu,
        final String persistenceFile, Map<String, Object> map) {
        List<Class<?>> clist = null;
        OpenJPAEntityManagerFactorySPI oemf = null;
        Map<String, Object> config = new HashMap(System.getProperties());
        if (map != null) {
            config.putAll(map);
            // Get the persistent class list
            clist = (List<Class<?>>)map.remove(PERSISTENT_CLASS_LIST);
        }
        PersistenceProductDerivation.ConfigurationParser cfgParser = 
            new PersistenceProductDerivation.ConfigurationParser(config);
        try {
            URL url = getResourceURL(persistenceFile);
            cfgParser.parse(url);
            List<PersistenceUnitInfoImpl> units = cfgParser.getResults();
            PersistenceUnitInfo puinf = null;
            // Find the pu info that matches the pu name
            for (PersistenceUnitInfo pui : units) {
                if (pu.equals(pui.getPersistenceUnitName())) {
                    puinf = pui;
                    break;
                }
            }
            
            // If there is a persistent class list, add each class to the puinfo
            if (clist != null) {
                for (Class<?> cl : clist) {
                    ((PersistenceUnitInfoImpl)puinf).addManagedClassName(cl.getName());
                }
            }
            
            oemf = createContainerEMF(pu, puinf, config);
        }
        catch (IOException ioe) {
            throw new RuntimeException("Failed to parse: " + getPersistenceResourceName(), ioe);
        }
        if (oemf == null) {
            throw new NullPointerException("Expected an entity manager factory " + "for the persistence unit named: \""
                + pu + "\"");
        }
        return oemf;
    }

    // Creates an instance of the OpenJPA PersistenceProviderImpl and
    // returns an EMF via createContainerEntityManagerFactory
    private OpenJPAEntityManagerFactorySPI createContainerEMF(String puName, 
        PersistenceUnitInfo pui, Map<String, Object> map) {
        
        PersistenceProviderImpl  ppi = new PersistenceProviderImpl();
        
        return (OpenJPAEntityManagerFactorySPI)ppi.createContainerEntityManagerFactory(pui, map);
    }

    // Build a resource URL for the given resource
    private static URL getResourceURL(String rsrc) throws IOException {
        Enumeration<URL> urls = null;
        try {
            ClassLoader cl = ContainerEMFTest.class.getClassLoader();
            urls = AccessController.doPrivileged(
                J2DoPrivHelper.getResourcesAction(cl, rsrc));
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
        return Collections.list(urls).get(0);
    }

    
}
