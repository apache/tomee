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
package org.apache.webbeans.spi;

import java.util.Properties;

import javax.enterprise.inject.spi.BeanManager;

/**
 * <h3>JSR-299 Container lifecycle.</h3>
 * <p>
 * Implement this interface to provide own container initialization logic.
 * </p>
 * <p>
 * From the application point of view this interface can be used to start
 * and stop OpenWebBeans.
 * </p>
 *
 */
public interface ContainerLifecycle
{
    /**
     * Initialize lifecycle.
     * <p>
     * Implementors can configure their
     * initialization specific actions here.
     * </p>
     * @param properties any properties
     */
    public void initApplication(Properties properties);
    
    /**
     * Starts container. It discovers all beans
     * in the deployed application classpath.
     * <p>
     * For Java EE artifact deployment, it scans all classes
     * and libraries in all deployment archives in the classpath.
     * There are several types of deployment archives;
     * <ul>
     *  <li>EAR archive</li>
     *  <li>EJB archive</li>
     *  <li>WAR archive</li>
     *  <li>RAR archive</li>
     *  <li>Application client archive. <b>OPTIONAL</b></li> 
     * </ul>
     * </p>
     *
     * <p>
     * Container uses {@link ScannerService} SPI for scanning archives
     * and act accordingly. If there is an exception while starting,
     * it must abort the deployment and provides information to the
     * developer.
     * </p>
     *
     * @param startupObject any startup object.
     */
    public void startApplication(Object startupObject);
        
    /**
     * <p>
     * Stopping the Application means that the container destroys all bean instances
     * it stores, cleans and removes all contexts and does other necessary
     * cleanup actions.
     * </p>
     * <p>
     * <b>Attention:</b> Accessing the BeanManager or any bean proxy after the shutdown
     * will result in non-portable behaviour!
     * </p>
     * @param endObject any object provided by application implementor. This can be a ServletContext, etc
     */
    public void stopApplication(Object endObject);
    
    /**
     * Get the underlying {@link BeanManager} instance for the current application.
     * There is 1-1 correspondence between a bean manager and a deployed (web-) application.
     * @return deployment {@link BeanManager} instance
     */
    public BeanManager getBeanManager();
    
    /**
     * Gets container's context service implementation.
     * This allows to manually start and end specific contexts.
     *
     * @return container contexts service
     */
    public ContextsService getContextService();
}
