/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.myfaces;

import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.apache.myfaces.spi.impl.DefaultFacesConfigurationProviderFactory;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// fork to be able to not fail on empty faces-config.xml
public class TomEEFacesConfigurationProviderFactory extends DefaultFacesConfigurationProviderFactory {
    public static final String FACES_CONFIGURATION_PROVIDER = FacesConfigurationProvider.class.getName();
    public static final String FACES_CONFIGURATION_PROVIDER_LIST = FacesConfigurationProvider.class.getName()+".LIST";
    public static final String FACES_CONFIGURATION_PROVIDER_INSTANCE_KEY = FacesConfigurationProvider.class.getName() + ".INSTANCE";

    private Logger getLogger() {
        return Logger.getLogger(DefaultFacesConfigurationProviderFactory.class.getName());
    }

    @Override
    public FacesConfigurationProvider getFacesConfigurationProvider(final ExternalContext externalContext) {
        FacesConfigurationProvider returnValue = (FacesConfigurationProvider) externalContext.getApplicationMap().get(FACES_CONFIGURATION_PROVIDER_INSTANCE_KEY);
        if (returnValue == null) {
            final ExternalContext extContext = externalContext;
            try {
                returnValue = resolveFacesConfigurationProviderFromService(extContext);
                externalContext.getApplicationMap().put(FACES_CONFIGURATION_PROVIDER_INSTANCE_KEY, returnValue);
            } catch (final ClassNotFoundException | NoClassDefFoundError e) {
                // ignore
            } catch (final InstantiationException | InvocationTargetException | IllegalAccessException e) {
                getLogger().log(Level.SEVERE, "", e);
            } catch (final PrivilegedActionException e) {
                throw new FacesException(e);
            }
        }


        return returnValue;
    }

    private FacesConfigurationProvider resolveFacesConfigurationProviderFromService(final ExternalContext externalContext)
            throws ClassNotFoundException, NoClassDefFoundError, InstantiationException, IllegalAccessException, InvocationTargetException, PrivilegedActionException {
        List<String> classList = (List<String>) externalContext.getApplicationMap().get(FACES_CONFIGURATION_PROVIDER_LIST);
        if (classList == null) {
            classList = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext).getServiceProviderList(FACES_CONFIGURATION_PROVIDER);
            externalContext.getApplicationMap().put(FACES_CONFIGURATION_PROVIDER_LIST, classList);
        }
        return ClassUtils.buildApplicationObject(FacesConfigurationProvider.class, classList, new TomEEFacesConfigurationProvider());
    }
}
