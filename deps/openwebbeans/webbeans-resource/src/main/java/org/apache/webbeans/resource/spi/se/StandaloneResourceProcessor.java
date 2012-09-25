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
package org.apache.webbeans.resource.spi.se;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

public class StandaloneResourceProcessor
{ 
    private static InitialContext context = null;
    
    private static Logger logger = WebBeansLoggerFacade.getLogger(StandaloneResourceProcessor.class);
    
    private static StandaloneResourceProcessor processor = new StandaloneResourceProcessor();
    
    /**
     *  A cache for EntityManagerFactories.
     */
    private Map<String, EntityManagerFactory> factoryCache = new ConcurrentHashMap<String, EntityManagerFactory>();    
    
    static
    {
        try
        {
            context = new InitialContext();
            
        }
        catch(Exception e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static StandaloneResourceProcessor getProcessor()
    {
        return processor;
    }
    
    public <X> X getEntityManager(PersistenceContext persistenceContext, Class<X> clazz)
    {
        EntityManager obj = getPersistenceContext(persistenceContext.unitName());
        if (obj == null) 
        {
            logger.log(Level.WARNING, WebBeansLoggerFacade.constructMessage(OWBLogConst.WARN_0014, "@PersistenceContext", persistenceContext.unitName()));
        }
        
        return clazz.cast(obj);
    }
    
    public <X> X getEntityManagerFactory(PersistenceUnit persistenceUnit, Class<X> clazz)
    {
        EntityManagerFactory factory = getPersistenceUnit(persistenceUnit.unitName());
        if (factory == null) 
        {
            logger.log(Level.WARNING, WebBeansLoggerFacade.constructMessage(OWBLogConst.WARN_0014, "@PersistenceUnit", persistenceUnit.unitName()));
        }
        
        return clazz.cast(factory);
    }
    
    public <X> X getResource(Resource resource, Class<X> resourceType)
    {
        Object obj = null;
        try
        {
            obj = context.lookup("java:/comp/env/"+ resource.name()); 
            if (obj == null) 
            {
                logger.log(Level.WARNING, WebBeansLoggerFacade.constructMessage(OWBLogConst.WARN_0014, "@Resource", resource.name()));
            }

        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0001, resource));
        }   
        
        return resourceType.cast(obj);
    }    

    public <X> X getWebServiceResource(WebServiceRef resource, Class<X> resourceType)
    {
        Object obj = null;
        try
        {
            obj = context.lookup("java:/comp/env/"+ resource.name()); 
            if (obj == null) 
            {
                logger.log(Level.WARNING, WebBeansLoggerFacade.constructMessage(OWBLogConst.WARN_0014, "@WebServiceRef", resource.name()));
            }

        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0001, resource));
        }   
        
        return resourceType.cast(obj);
    }    
    
    /**
     * {@inheritDoc}
     * 
     */
    private EntityManagerFactory getPersistenceUnit(String unitName)
    {
        if(factoryCache.get(unitName) != null)
        {
            return factoryCache.get(unitName);
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(unitName);        
        factoryCache.put(unitName, emf);
            
        return emf;
    }

    /** 
     * TODO: currently this returns an extended EntityManager, so we have to wrap it
     * We have to create a Proxy for injecting entity managers. So, whenever method is called
     * on the entity managers, look at current Transaction, if exist call joinTransaction();
     */
    private EntityManager getPersistenceContext(String unitName)
    {
        EntityManagerFactory emf = getPersistenceUnit(unitName);        
        EntityManager em = emf.createEntityManager();
        
        return em;
    }
    
    public void clear()
    {
        Set<String> keys = this.factoryCache.keySet();
        for(String key : keys)
        {
            EntityManagerFactory factory = this.factoryCache.get(key);
            try
            {
                factory.close();
                
            }
            catch(Exception e)
            {
                logger.log(Level.WARNING, WebBeansLoggerFacade.constructMessage(OWBLogConst.WARN_0006, e, key));
            }
        }
    }

}
