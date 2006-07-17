package org.openejb.alt.config.ejb;

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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"resourceLink", "query"})
@XmlRootElement(name = "ejb-deployment")
public class EjbDeployment {

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

    public void addQuery(Query query) {
        getQuery().add(query);
    }
}
