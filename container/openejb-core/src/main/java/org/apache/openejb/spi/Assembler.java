/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.spi;

import org.apache.openejb.OpenEJBException;

import jakarta.transaction.TransactionManager;
import java.util.Properties;

/**
 * Instantiates and assembles a configured, runnable, instance of the
 * container system and all of its sub-components. Vendors needing extreme control
 * over the construction of the container system can get it by implementing this class.
 * Doing this comes with large amounts of responsibility and complexity and should
 * not be done without a deep understanding of OpenEJB. This class responsbilities are:
 * <ul>
 * <li>Instantiate and initialize all Container implementations</li>
 * <li>Instantiate and initialize TransactionService implementation</li>
 * <li>Instantiate and initialize SecurityService implementation</li>
 * <li>Instantiate and initialize all ResourceManagers</li>
 * <li>Load all deployed beans</li>
 * <li>Populate each deployment's JNDI ENC</li>
 * <li>Populate the IntraVM Server's global, client, JNDI namespace</li>
 * </ul>
 *
 * <p>
 *     For the concrete implementation please also inspect {@link org.apache.openejb.assembler.classic.Assembler}.
 * </p>
 */
public interface Assembler {

    /**
     * Initialize the assembler with the given properties.
     * @param props initialization properties
     * @throws OpenEJBException if the initialization phase faces an exception
     */
    void init(Properties props) throws OpenEJBException;

    /**
     * Gets the configuration from {@link org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory}
     * and builds the {@link ContainerSystem} and fire {@link org.apache.openejb.assembler.classic.event.ContainerSystemPostCreate}
     * event.
     * @throws OpenEJBException if the build phase faces an exception
     */
    void build() throws OpenEJBException;

    /**
     * Gets the configured {@link ContainerSystem} instance.
     * @return the {@link ContainerSystem} instance
     */
    ContainerSystem getContainerSystem();

    /**
     * Gets the system wide configured {@link TransactionManager} instance.
     * @return the {@link TransactionManager} instance.
     */
    TransactionManager getTransactionManager();

    /**
     * Gets the system wide configured {@link SecurityService} instance.
     * @return the {@link SecurityService} instance.
     */
    SecurityService getSecurityService();

    /**
     * Destroys the container system and all associated resources.
     */
    void destroy();
}