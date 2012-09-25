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
package org.apache.webbeans.web.scanner;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.corespi.scanner.AbstractMetaDataDiscovery;
import org.apache.webbeans.corespi.scanner.AnnotationDB;
import org.apache.webbeans.corespi.se.BeansXmlAnnotationDB;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.WebBeansUtil;
import org.scannotation.WarUrlFinder;

/**
 * Configures the web application to find beans.
 */
public class WebScannerService extends AbstractMetaDataDiscovery
{
    private final static Logger logger = WebBeansLoggerFacade.getLogger(WebScannerService.class);

    private boolean configure = false;

    protected ServletContext servletContext = null;

    public WebScannerService()
    {
        
    }

    public void init(Object context)
    {
        super.init(context);
        this.servletContext = (ServletContext) context;        
    }
    
    protected void configure()
    {
        try
        {
            if (!configure)
            {
                Set<String> arcs = getArchives();
                String[] urls = new String[arcs.size()];
                urls = arcs.toArray(urls);

                getAnnotationDB().scanArchives(urls);
                
                configure = true;
            }

        }
        catch (Exception e)
        {
            throw new WebBeansConfigurationException(WebBeansLoggerFacade.getTokenString(OWBLogConst.ERROR_0002), e);
        }

    }

    /**
     *  @return all beans.xml paths
     */
    private Set<String> getArchives() throws Exception
    {
        Set<String> lists = createURLFromMarkerFile();
        String warUrlPath = createURLFromWARFile();

        if (warUrlPath != null)
        {
            lists.add(warUrlPath);
        }

        return lists;
    }

    /* Creates URLs from the marker file */
    protected Set<String> createURLFromMarkerFile() throws Exception
    {
        Set<String> listURL = new HashSet<String>();

        // Root with beans.xml marker.
        String[] urls = findBeansXmlBases(META_INF_BEANS_XML, WebBeansUtil.getCurrentClassLoader());

        if (urls != null)
        {
            String addPath;
            for (String url : urls)
            {
                String fileDir = new URL(url).getFile();
                if (fileDir.endsWith(".jar!/"))
                {
                    fileDir = fileDir.substring(0, fileDir.lastIndexOf("/")) + "/" + META_INF_BEANS_XML;

                    //fix for weblogic
                    if (!fileDir.startsWith("file:/"))
                    {
                        fileDir = "file:/" + fileDir;
                    }

                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.log(Level.FINE, "OpenWebBeans found the following url while doing web scanning: " + fileDir);
                    }

                    addPath = "jar:" + fileDir;

                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.log(Level.FINE, "OpenWebBeans added the following jar based path while doing web scanning: " +
                                addPath);
                    }
                }
                else
                {
                    //X TODO check!
                    addPath = "file:" + url + "META-INF/beans.xml";

                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.log(Level.FINE, "OpenWebBeans added the following file based path while doing web scanning: " +
                                addPath);
                    }

                }

                listURL.add(url);
            }
        }

        return listURL;
    }

    /**
     * Returns the web application class path if it contains
     * a beans.xml marker file.
     * 
     * @return the web application class path
     * @throws Exception if any exception occurs
     */
    protected String createURLFromWARFile() throws Exception
    {
        if (servletContext == null)
        {
            // this may happen if we are running in a test container, in IDE development, etc
            return null;
        }
        
        URL url = servletContext.getResource("/WEB-INF/beans.xml");

        if (url != null)
        {
            addWebBeansXmlLocation(url);
            URL resourceUrl = WarUrlFinder.findWebInfClassesPath(this.servletContext);

            if (resourceUrl == null)
            {
                return null;
            }

            //set resource to beans.xml mapping
            AnnotationDB annotationDB = getAnnotationDB();

            if(annotationDB instanceof BeansXmlAnnotationDB)
            {
                ((BeansXmlAnnotationDB)annotationDB).setResourceBeansXml(resourceUrl.toExternalForm(), url.toExternalForm());
            }
            return resourceUrl.toExternalForm();
        }

        return null;
    }

}
