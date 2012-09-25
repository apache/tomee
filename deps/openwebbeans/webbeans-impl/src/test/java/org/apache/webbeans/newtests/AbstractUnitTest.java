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
package org.apache.webbeans.newtests;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.lifecycle.test.OpenWebBeansTestLifeCycle;
import org.apache.webbeans.lifecycle.test.OpenWebBeansTestMetaDataDiscoveryService;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.util.WebBeansUtil;
import org.junit.Assert;


public abstract class AbstractUnitTest
{
    private OpenWebBeansTestLifeCycle testLifecycle;
    private List<Extension>  extensions = new ArrayList<Extension>();
    private WebBeansContext webBeansContext;

    protected AbstractUnitTest()
    {

    }
    
    protected void startContainer(Collection<Class<?>> beanClasses)
    {
        startContainer(beanClasses, null);
    }
    
    protected void startContainer(Collection<Class<?>> beanClasses, Collection<String> beanXmls)
    {
        WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());
        //Creates a new container
        testLifecycle = new OpenWebBeansTestLifeCycle();
        
        webBeansContext = WebBeansContext.getInstance();
        for (Extension ext : extensions)
        {
            webBeansContext.getExtensionLoader().addExtension(ext);
        }
        
        //Deploy bean classes
        OpenWebBeansTestMetaDataDiscoveryService discoveryService = (OpenWebBeansTestMetaDataDiscoveryService)webBeansContext.getScannerService();
        discoveryService.deployClasses(beanClasses);
        if (beanXmls != null)
        {
            discoveryService.deployXMLs(beanXmls);
        }

        //Start application
        try
        {
            testLifecycle.startApplication(null);
        }
        catch (Exception e)
        {
            throw new WebBeansConfigurationException(e);
        }
        
    }

    protected ContainerLifecycle getLifecycle()
    {
        return testLifecycle;
    }
    
    protected void shutDownContainer()
    {
        //Shwtdown application
        if(this.testLifecycle != null)
        {
            this.testLifecycle.stopApplication(null);
        }        
    }
        
    protected WebBeansContext getWebBeansContext()
    {
        return this.webBeansContext;
    }
    
    protected BeanManager getBeanManager()
    {
        return this.webBeansContext.getBeanManagerImpl();
    }

    @SuppressWarnings("unchecked")
    protected <T> T getInstance(Class<T> type, Annotation... qualifiers)
    {
        Set<Bean<?>> beans = getBeanManager().getBeans(type, qualifiers);
        Assert.assertNotNull(beans);

        Bean<?> bean = getBeanManager().resolve(beans);
        
        return (T) getBeanManager().getReference(bean, type, getBeanManager().createCreationalContext(bean));
    }
    
    protected String getXmlPath(String packageName, String fileName)
    {
        StringBuilder prefix = new StringBuilder(packageName.replace('.', '/'));
        prefix.append("/");
        prefix.append(fileName);
        prefix.append(".xml");
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.getResource(prefix.toString()).toExternalForm();
    }
    
    /**
     * Add a CDI Extension which should get used in the test case.
     * Use this function instead of defining test Extensions via the usual
     * META-INF/services/javax.enterprise.inject.spi.Extension file!
     * 
     * @param ext the {@link Extension} which should get loaded
     */
    public void addExtension(Extension ext) {
        this.extensions.add(ext);
    }
}
