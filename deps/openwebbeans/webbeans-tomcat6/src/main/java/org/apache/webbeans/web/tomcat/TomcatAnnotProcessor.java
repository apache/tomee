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
package org.apache.webbeans.web.tomcat;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.apache.AnnotationProcessor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

public class TomcatAnnotProcessor implements AnnotationProcessor
{
    private final static Logger logger = WebBeansLoggerFacade.getLogger(TomcatAnnotProcessor.class);

    private AnnotationProcessor processor;

    private ClassLoader loader;
    
    private Map<Object, Object> objects = new ConcurrentHashMap<Object, Object>();
    
    public TomcatAnnotProcessor(ClassLoader loader, AnnotationProcessor processor)
    {
        this.processor = processor;
        this.loader = loader;
    }
    
    public void postConstruct(Object obj) throws IllegalAccessException, InvocationTargetException
    {
        processor.postConstruct(obj);
    }

    public void preDestroy(Object obj) throws IllegalAccessException, InvocationTargetException
    {
        Object injectorInstance = objects.get(obj);
        if(injectorInstance != null)
        {
            try
            {
                TomcatUtil.destroy(injectorInstance, loader);
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "TomcatAnnotProcessor", e);
            }
        }
        processor.preDestroy(obj);
    }

    public void processAnnotations(Object obj) throws IllegalAccessException, InvocationTargetException, NamingException
    {
        processor.processAnnotations(obj);
        try
        {
           Object injectorInstance = TomcatUtil.inject(obj, loader);
           if(injectorInstance != null)
           {
               objects.put(obj, injectorInstance);   
           }
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "TomcatAnnotProcessor", e);
        }
    }

}
