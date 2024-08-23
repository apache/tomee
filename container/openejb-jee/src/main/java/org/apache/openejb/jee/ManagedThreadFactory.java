package org.apache.openejb.jee;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.jba.JndiName;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "managed-thread-factoryType", propOrder = {
        "description",
        "name",
        "contextService",
        "priority",
        "properties"
})
public class ManagedThreadFactory implements Keyable<String> {
    @XmlElement
    private Description description;
    @XmlElement
    private JndiName name;
    @XmlElement(name = "context-service-ref")
    private JndiName contextService;
    @XmlElement
    private Integer priority;
    @XmlElement(name = "property")
    private List<Property> properties;

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public JndiName getName() {
        return name;
    }

    public void setName(JndiName name) {
        this.name = name;
    }

    public JndiName getContextService() {
        return contextService;
    }

    public void setContextService(JndiName contextService) {
        this.contextService = contextService;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public String getKey() {
        return name.getvalue();
    }
}