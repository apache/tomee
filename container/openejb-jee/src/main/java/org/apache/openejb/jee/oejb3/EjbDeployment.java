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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.oejb3;

import org.apache.openejb.jee.EnterpriseBean;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Properties;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"jndi", "ejbLink", "resourceLink", "query", "roleMapping", "properties"})
@XmlRootElement(name = "ejb-deployment")
public class EjbDeployment {

    @XmlElement(name = "jndi", required = true)
    protected List<Jndi> jndi;

    @XmlElement(name = "ejb-link", required = true)
    protected List<EjbLink> ejbLink;

    @XmlElement(name = "resource-link", required = true)
    protected List<ResourceLink> resourceLink;

    @XmlElement(required = true)
    protected List<Query> query;

    @XmlElement(name = "role-mapping")
    protected List<RoleMapping> roleMapping;

    @XmlAttribute(name = "container-id")
    protected String containerId;

    @XmlAttribute(name = "deployment-id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String deploymentId;

    @XmlAttribute(name = "ejb-name")
    protected String ejbName;

    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;

    public EjbDeployment() {
    }

    public EjbDeployment(final String containerId, final String deploymentId, final String ejbName) {
        this.containerId = containerId;
        this.deploymentId = deploymentId;
        this.ejbName = ejbName;
    }

    public EjbDeployment(final EnterpriseBean bean) {
        this.deploymentId = bean.getEjbName();
        this.ejbName = bean.getEjbName();
    }

    public List<EjbLink> getEjbLink() {
        if (ejbLink == null) {
            ejbLink = new ArrayList<EjbLink>();
        }
        return this.ejbLink;
    }

    public List<Jndi> getJndi() {
        if (jndi == null) {
            jndi = new ArrayList<Jndi>();
        }
        return jndi;
    }

    public List<ResourceLink> getResourceLink() {
        if (resourceLink == null) {
            resourceLink = new ArrayList<ResourceLink>();
        }
        return this.resourceLink;
    }

    public List<Query> getQuery() {
        if (query == null) {
            query = new ArrayList<Query>();
        }
        return this.query;
    }

    public ResourceLink getResourceLink(final String refName) {
        return getResourceLinksMap().get(refName);
    }

    public Map<String, ResourceLink> getResourceLinksMap() {
        final Map<String, ResourceLink> map = new LinkedHashMap<String, ResourceLink>();
        for (final ResourceLink link : getResourceLink()) {
            map.put(link.getResRefName(), link);
        }
        return map;
    }

    public EjbLink getEjbLink(final String refName) {
        return getEjbLinksMap().get(refName);
    }

    public Map<String, EjbLink> getEjbLinksMap() {
        final Map<String, EjbLink> map = new LinkedHashMap<String, EjbLink>();
        for (final EjbLink link : getEjbLink()) {
            map.put(link.getEjbRefName(), link);
        }
        return map;
    }


    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(final String value) {
        this.containerId = value;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(final String value) {
        this.deploymentId = value;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(final String value) {
        this.ejbName = value;
    }

    public void addResourceLink(final ResourceLink resourceLink) {
        getResourceLink().add(resourceLink);
    }

    public void removeResourceLink(final String resRefName) {
        resourceLink.removeIf(link -> resRefName.equals(link.getResRefName()));
    }

    public void addEjbLink(final EjbLink ejbLink) {
        getEjbLink().add(ejbLink);
    }

    public void addQuery(final Query query) {
        getQuery().add(query);
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public void addProperty(final String key, final String value) {
        getProperties().setProperty(key, value);
    }

    public List<RoleMapping> getRoleMapping() {
        if (roleMapping == null) {
            roleMapping = new ArrayList<RoleMapping>();
        }
        return roleMapping;
    }
}
