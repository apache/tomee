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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"ejbLink", "resourceLink", "query"})
@XmlRootElement(name = "ejb-deployment")
public class EjbDeployment {

    @XmlElement(name = "ejb-link", required = true)
    protected List<EjbLink> ejbLink;

    @XmlElement(name = "resource-link", required = true)
    protected List<ResourceLink> resourceLink;

    @XmlElement(required = true)
    protected List<Query> query;

    @XmlAttribute(name = "container-id")
    protected String containerId;

    @XmlAttribute(name = "deployment-id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String deploymentId;

    @XmlAttribute(name = "ejb-name")
    protected String ejbName;

    public EjbDeployment() {
    }

    public EjbDeployment(String containerId, String deploymentId, String ejbName) {
        this.containerId = containerId;
        this.deploymentId = deploymentId;
        this.ejbName = ejbName;
    }

    public List<EjbLink> getEjbLink() {
        if (ejbLink == null) {
            ejbLink = new ArrayList<EjbLink>();
        }
        return this.ejbLink;
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

    public ResourceLink getResourceLink(String refName) {
        return getResourceLinksMap().get(refName);
    }

    public Map<String,ResourceLink> getResourceLinksMap(){
        Map<String,ResourceLink> map = new LinkedHashMap<String,ResourceLink>();
        for (ResourceLink link : getResourceLink()) {
            map.put(link.getResRefName(), link);
        }
        return map;
    }

    public EjbLink getEjbLink(String refName) {
        return getEjbLinksMap().get(refName);
    }

    public Map<String,EjbLink> getEjbLinksMap(){
        Map<String,EjbLink> map = new LinkedHashMap<String,EjbLink>();
        for (EjbLink link : getEjbLink()) {
            map.put(link.getEjbRefName(), link);
        }
        return map;
    }


    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String value) {
        this.containerId = value;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String value) {
        this.deploymentId = value;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String value) {
        this.ejbName = value;
    }

    public void addResourceLink(ResourceLink resourceLink) {
        getResourceLink().add(resourceLink);
    }

    public void removeResourceLink(String resRefName) {
        for (Iterator<ResourceLink> iterator = resourceLink.iterator(); iterator.hasNext();) {
            ResourceLink link =  iterator.next();
            if (resRefName.equals(link.getResRefName())) {
                iterator.remove();
            }
        }
    }

    public void addEjbLink(EjbLink ejbLink) {
        getEjbLink().add(ejbLink);
    }

    public void addQuery(Query query) {
        getQuery().add(query);
    }
}
