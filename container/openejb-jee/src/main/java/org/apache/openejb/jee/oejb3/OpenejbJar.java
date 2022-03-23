/**
 *
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

package org.apache.openejb.jee.oejb3;

import org.apache.openejb.jee.NamedModule;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.EnterpriseBean;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"moduleName", "properties", "ejbDeployment", "pojoDeployment"})
@XmlRootElement(name = "openejb-jar")
public class OpenejbJar implements NamedModule {
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;

    @XmlElement(name = "module-name")
    protected String moduleName;

    @XmlElement(name = "ejb-deployment", required = true)
    protected List<EjbDeployment> ejbDeployment;

    @XmlElement(name = "pojo-deployment")
    protected List<PojoDeployment> pojoDeployment;

    public List<EjbDeployment> getEjbDeployment() {
        if (ejbDeployment == null) {
            ejbDeployment = new ArrayList<EjbDeployment>();
        }
        return this.ejbDeployment;
    }

    public List<PojoDeployment> getPojoDeployment() {
        if (pojoDeployment == null) {
            pojoDeployment = new ArrayList<PojoDeployment>();
        }
        return pojoDeployment;
    }

    public Map<String, EjbDeployment> getDeploymentsById() {
        final Map<String, EjbDeployment> map = new LinkedHashMap<String, EjbDeployment>();
        for (final EjbDeployment deployment : getEjbDeployment()) {
            map.put(deployment.getDeploymentId(), deployment);
        }
        return map;
    }

    public Map<String, EjbDeployment> getDeploymentsByEjbName() {
        final Map<String, EjbDeployment> map = new LinkedHashMap<String, EjbDeployment>();
        for (final EjbDeployment deployment : getEjbDeployment()) {
            map.put(deployment.getEjbName(), deployment);
        }
        return map;
    }

    @Override
    public String getId() {
        return getModuleName();
    }

    @Override
    public void setId(final String id) {
        setModuleName(id);
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    public int getEjbDeploymentCount() {
        return getEjbDeployment().size();
    }

    public EjbDeployment addEjbDeployment(final EjbDeployment ejbDeployment) {
        getEjbDeployment().add(ejbDeployment);
        return ejbDeployment;
    }

    public void removeEjbDeployment(final EjbDeployment ejbDeployment) {
        getEjbDeployment().remove(ejbDeployment);
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public EjbDeployment addEjbDeployment(final EnterpriseBean bean) {
        return addEjbDeployment(new EjbDeployment(bean));
    }

    public OpenejbJar postRead() {
        if (pojoDeployment != null && properties != null) {
            for (final PojoDeployment pojo : pojoDeployment) {
                for (final String key : properties.stringPropertyNames()) {
                    if (!pojo.getProperties().containsKey(key)) {
                        pojo.getProperties().put(key, properties.get(key));
                    }
                }
            }
        }
        return this;
    }
}
