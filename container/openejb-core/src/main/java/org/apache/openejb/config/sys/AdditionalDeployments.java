package org.apache.openejb.config.sys;

import org.apache.openejb.config.sys.Deployments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "additional-deployments")
public class AdditionalDeployments {
    @XmlElement(name = "deployments")
    protected List<Deployments> deployments = new ArrayList<Deployments>();

    public List<Deployments> getDeployments() {
        return deployments;
    }
}
