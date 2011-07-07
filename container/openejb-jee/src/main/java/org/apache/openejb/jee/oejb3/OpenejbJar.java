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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;


/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"moduleName", "properties","ejbDeployment"})
@XmlRootElement(name = "openejb-jar")
public class OpenejbJar implements NamedModule {
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;

    @XmlElement(name = "module-name")
    protected String moduleName;

    @XmlElement(name = "ejb-deployment", required = true)
    protected List<EjbDeployment> ejbDeployment;

    public List<EjbDeployment> getEjbDeployment() {
        if (ejbDeployment == null) {
            ejbDeployment = new ArrayList<EjbDeployment>();
        }
        return this.ejbDeployment;
    }

    public Map<String,EjbDeployment> getDeploymentsById(){
        Map<String,EjbDeployment> map = new LinkedHashMap<String,EjbDeployment>();
        for (EjbDeployment deployment : getEjbDeployment()) {
            map.put(deployment.getDeploymentId(), deployment);
        }
        return map;
    }

    public Map<String,EjbDeployment> getDeploymentsByEjbName(){
        Map<String,EjbDeployment> map = new LinkedHashMap<String,EjbDeployment>();
        for (EjbDeployment deployment : getEjbDeployment()) {
            map.put(deployment.getEjbName(), deployment);
        }
        return map;
    }

    @Override
    public String getId() {
        return getModuleName();
    }

    @Override
    public void setId(String id) {
        setModuleName(id);
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getEjbDeploymentCount() {
        return getEjbDeployment().size();
    }

    public EjbDeployment addEjbDeployment(EjbDeployment ejbDeployment) {
        getEjbDeployment().add(ejbDeployment);
        return ejbDeployment;
    }

    public void removeEjbDeployment(EjbDeployment ejbDeployment) {
        getEjbDeployment().remove(ejbDeployment);
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public EjbDeployment addEjbDeployment(EnterpriseBean bean) {
        return addEjbDeployment(new EjbDeployment(bean));
    }
}
