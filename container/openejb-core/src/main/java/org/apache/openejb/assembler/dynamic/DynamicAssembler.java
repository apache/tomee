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
package org.apache.openejb.assembler.dynamic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.InfoObject;
import org.apache.openejb.assembler.classic.Assembler;

import javax.transaction.TransactionManager;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class DynamicAssembler {
    private final CoreContainerSystem system;

    public DynamicAssembler() {
        system = new CoreContainerSystem();
    }

    public void add(InfoObject info) throws OpenEJBException {
    }

    public void addContainer(ContainerInfo info) throws OpenEJBException {
    }

    public void addConnector(ConnectorInfo info) throws OpenEJBException {
    }

    public void addConnectionManager(ConnectionManagerInfo info) throws OpenEJBException {
    }

    public void addEjbJar(EjbJarInfo info) throws OpenEJBException {
    }

    public void addApplication(AppInfo info) throws OpenEJBException {
    }

    public void addClient(ClientInfo info) throws OpenEJBException {
    }

    public void add(InfoObject info, ClassLoader classLoader) throws OpenEJBException {
    }

    public void addContainer(ContainerInfo info, ClassLoader classLoader) throws OpenEJBException {
    }

    public void addConnector(ConnectorInfo info, ClassLoader classLoader) throws OpenEJBException {
    }

    public void addConnectionManager(ConnectionManagerInfo info, ClassLoader classLoader) throws OpenEJBException {
    }

    public void addEjbJar(EjbJarInfo info, ClassLoader classLoader) throws OpenEJBException {
    }

    public void addApplication(AppInfo info, ClassLoader classLoader) throws OpenEJBException {
    }

    public void addClient(ClientInfo info, ClassLoader classLoader) throws OpenEJBException {
    }

}
