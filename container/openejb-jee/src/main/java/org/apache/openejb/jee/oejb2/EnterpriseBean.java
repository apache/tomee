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
package org.apache.openejb.jee.oejb2;

import javax.xml.bind.JAXBElement;
import java.lang.*;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public interface EnterpriseBean {
    String getEjbName();

    void setEjbName(String value);

    List<String> getJndiName();

    List<String> getLocalJndiName();

    List<Jndi> getJndi();

    List<JAXBElement<? extends AbstractNamingEntryType>> getAbstractNamingEntry();

    List<PersistenceContextRefType> getPersistenceContextRef();

    List<PersistenceUnitRefType> getPersistenceUnitRef();

    List<EjbRefType> getEjbRef();

    List<EjbLocalRefType> getEjbLocalRef();

    List<ServiceRefType> getServiceRef();

    List<ResourceRefType> getResourceRef();

    List<ResourceEnvRefType> getResourceEnvRef();



}
