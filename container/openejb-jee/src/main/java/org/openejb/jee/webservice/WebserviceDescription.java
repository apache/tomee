/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.webservice;

import org.openejb.jee.common.Icon;
import org.openejb.jee.webservice.PortComponent;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class WebserviceDescription {
    private String id;
    private String description;
    private String displayName;
    private Icon icon;
    private String webserviceDescriptionName;
    private String wsdlFile;
    private String jaxrpcMappingFile;
    private List<PortComponent> portComponents = new ArrayList<PortComponent>();

    public WebserviceDescription() {
    }

    public WebserviceDescription(String webserviceDescriptionName) {
        this.webserviceDescriptionName = webserviceDescriptionName;
    }

    public WebserviceDescription(String webserviceDescriptionName, PortComponent portComponent) {
        this.webserviceDescriptionName = webserviceDescriptionName;
        this.portComponents.add(portComponent);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public String getWebserviceDescriptionName() {
        return webserviceDescriptionName;
    }

    public void setWebserviceDescriptionName(String webserviceDescriptionName) {
        this.webserviceDescriptionName = webserviceDescriptionName;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wsdlFile) {
        this.wsdlFile = wsdlFile;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(String jaxrpcMappingFile) {
        this.jaxrpcMappingFile = jaxrpcMappingFile;
    }

    public List<PortComponent> getPortComponents() {
        return portComponents;
    }

    public void setPortComponents(List<PortComponent> portComponents) {
        this.portComponents = portComponents;
    }
}
