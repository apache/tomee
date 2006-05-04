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

import org.openejb.jee.javaee.Icon;
import org.openejb.jee.webservice.WebserviceDescription;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Webservices {

    private String id;
    private List<String> description;
    private List<String> displayName;
    private List<Icon> icons;
    private List<WebserviceDescription> webserviceDescriptions = new ArrayList<WebserviceDescription>();

    public Webservices() {
    }

    public Webservices(List<WebserviceDescription> webserviceDescriptions) {
        this.webserviceDescriptions = webserviceDescriptions;
    }

    public Webservices(WebserviceDescription webserviceDescription) {
        this.webserviceDescriptions.add(webserviceDescription);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getDisplayName() {
        return displayName;
    }

    public void setDisplayName(List<String> displayName) {
        this.displayName = displayName;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public List<WebserviceDescription> getWebserviceDescriptions() {
        return webserviceDescriptions;
    }

    public void setWebserviceDescriptions(List<WebserviceDescription> webserviceDescriptions) {
        this.webserviceDescriptions = webserviceDescriptions;
    }
}
