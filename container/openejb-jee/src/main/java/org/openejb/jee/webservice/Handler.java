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
import org.openejb.jee.common.InitParam;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Handler {

    private String id;
    private List<String> description = new ArrayList<String>();
    private List<String> displayName = new ArrayList<String>();
    private List<Icon> icons = new ArrayList<Icon>();
    private String handlerClass;
    private String handlerName;
    private List<InitParam> initParams = new ArrayList<InitParam>();
    private List<QName> soapHeaders = new ArrayList<QName>();
    private List<String> soapRoles = new ArrayList<String>();
    private List<String> portNames = new ArrayList<String>();

    public Handler() {
    }

    public Handler(String handlerClass) {
        this.handlerClass = handlerClass;
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

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    public List<InitParam> getInitParams() {
        return initParams;
    }

    public void setInitParams(List<InitParam> initParams) {
        this.initParams = initParams;
    }

    public List<QName> getSoapHeaders() {
        return soapHeaders;
    }

    public void setSoapHeaders(List<QName> soapHeaders) {
        this.soapHeaders = soapHeaders;
    }

    public List<String> getSoapRoles() {
        return soapRoles;
    }

    public void setSoapRoles(List<String> soapRoles) {
        this.soapRoles = soapRoles;
    }

    public List<String> getPortNames() {
        return portNames;
    }

    public void setPortNames(List<String> portNames) {
        this.portNames = portNames;
    }
}
