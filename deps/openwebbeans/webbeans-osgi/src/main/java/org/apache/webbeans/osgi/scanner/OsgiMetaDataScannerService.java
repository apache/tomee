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
package org.apache.webbeans.osgi.scanner;

import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.ScannerService;

import org.apache.xbean.finder.BundleAssignableClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * In an OSGi environment, resources will not be delivered in
 * jars or file URLs, but as 'bundle://'.
 * This {@link org.apache.webbeans.spi.ScannerService} parses for all classes
 * in such a bundle.
 */
public class OsgiMetaDataScannerService implements ScannerService
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(OsgiMetaDataScannerService.class);

    protected ServletContext servletContext = null;
    private static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    private static final String WEB_INF_BEANS_XML = "WEB-INF/beans.xml";

    /** All classes which have to be scanned for Bean information */
    private Set<Class<?>> beanClasses = new HashSet<Class<?>>();

    /** the paths of all META-INF/beans.xml files */
    private Set<URL> beanXMLs = new HashSet<URL>();

    /**contains all the JARs we found with valid beans.xml in it */
    private Set<String> beanArchiveJarNames = new HashSet<String>();
    private Map<String, Set<String>> classAnnotations = new HashMap<String, Set<String>>();

    public void init(Object object)
    {
        if (object instanceof ServletContext)
        {
            servletContext = (ServletContext) object;
        }
    }

    public void release()
    {
        beanClasses = new HashSet<Class<?>>();
        beanXMLs = new HashSet<URL>();
        beanArchiveJarNames = new HashSet<String>();
        classAnnotations.clear();
    }

    public void scan() throws WebBeansDeploymentException
    {
        logger.info("Using OsgiMetaDataScannerService!");
        Bundle mainBundle = BundleUtils.getContextBundle(true);


        ServiceReference reference = mainBundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        try
        {
            PackageAdmin packageAdmin = (PackageAdmin) mainBundle.getBundleContext().getService(reference);

            // search for all META-INF/beans.xml files
            findBeansXml(mainBundle, packageAdmin);

            // search for all classes
            findBeanClasses(mainBundle, packageAdmin);
        }
        catch(Exception e)
        {
            throw new WebBeansDeploymentException("problem while scanning OSGi bundle", e);
        }
        finally
        {
            mainBundle.getBundleContext().ungetService(reference);
        }

    }

    private void findBeanClasses(Bundle mainBundle, PackageAdmin packageAdmin)
    {
        BundleClassFinder bundleClassFinder =
                new BundleAssignableClassFinder(packageAdmin, mainBundle,
                                                new Class<?>[]{Object.class},
                                                new ClassDiscoveryFilter()
         {

            public boolean directoryDiscoveryRequired(String directory)
            {
                return true;
            }

            public boolean jarFileDiscoveryRequired(String jarUrl)
            {
                boolean isValidBeanArchive = beanArchiveJarNames.contains(jarUrl);
                return isValidBeanArchive;
            }

            public boolean packageDiscoveryRequired(String packageName)
            {
                return true;
            }

            public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange)
            {
                return discoveryRange.equals(DiscoveryRange.BUNDLE_CLASSPATH);
            }
        });

        Set<String> acceptedClassNames = bundleClassFinder.find();
        for (String clsName : acceptedClassNames)
        {
            try
            {
                Class<?> cls = mainBundle.loadClass(clsName);

                classAnnotations.put(clsName, collectAnnotations(cls));

                beanClasses.add(cls);
            }
            catch(Exception e)
            {
                logger.info("cannot load class from bundle: " + clsName);
            }
        }
    }

    private Set<String> collectAnnotations(Class<?> cls)
    {
        Set<String> annotations = new HashSet<String>();

        addAnnotations(annotations, cls.getAnnotations());

        Constructor[] constructors = cls.getDeclaredConstructors();
        for (Constructor c : constructors)
        {
            addAnnotations(annotations, c.getAnnotations());
        }

        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields)
        {
            addAnnotations(annotations, f.getAnnotations());
        }

        Method[] methods = cls.getDeclaredMethods();
        for (Method m : methods)
        {
            addAnnotations(annotations, m.getAnnotations());

            Annotation[][] paramsAnns = m.getParameterAnnotations();
            for (Annotation[] pAnns : paramsAnns)
            {
                addAnnotations(annotations, pAnns);
            }
        }

        return annotations;
    }

    private void addAnnotations(Set<String> annStrings, Annotation[] annotations)
    {
        for (Annotation ann : annotations)
        {
            annStrings.add(ann.getClass().getSimpleName());
        }
    }

    private void findBeansXml(Bundle mainBundle, PackageAdmin packageAdmin)
            throws Exception
    {
        BundleResourceFinder brfXmlJar =  new BundleResourceFinder(packageAdmin, mainBundle, "", META_INF_BEANS_XML);

        BundleResourceFinder.ResourceFinderCallback rfCallback = new BundleResourceFinder.ResourceFinderCallback()
        {

            public void foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception
            {
                logger.info("adding the following beans.xml URL: " + url);
                beanXMLs.add(url);
            }

            public void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception
            {
                URL jarURL = bundle.getEntry(jarName);

                logger.info("adding the following beans.xml URL: " + jarURL.toExternalForm());

                beanXMLs.add(jarURL);
                beanArchiveJarNames.add(jarName);
            }

        };

        brfXmlJar.find(rfCallback);

        // TODO I found no other way to find WEB-INF/beanx.xml directly
        Enumeration<URL> urls = mainBundle.findEntries("", "beans.xml", true);
        boolean webBeansXmlFound = false;
        while(urls != null && urls.hasMoreElements())
        {
            URL webBeansXml = urls.nextElement();
            if (!webBeansXml.toExternalForm().endsWith("/" + WEB_INF_BEANS_XML))
            {
                continue;
            }

            if (webBeansXmlFound)
            {
                throw new WebBeansDeploymentException("found more than WEB-INF/beans.xml file!" + webBeansXml); 
            }

            logger.info("adding the following WEB-INF/beans.xml URL: " + webBeansXml);
            beanXMLs.add(webBeansXml);
            webBeansXmlFound = true;
        }
    }

    public Set<URL> getBeanXmls()
    {
        return beanXMLs;
    }

    public Set<Class<?>> getBeanClasses()
    {
        return beanClasses;
    }

    public Set<String> getAllAnnotations(String className)
    {
        return classAnnotations.get(className);
    }

    public BDABeansXmlScanner getBDABeansXmlScanner()
    {
        return null;
    }

    public boolean isBDABeansXmlScanningEnabled()
    {
        return false;
    }
}
