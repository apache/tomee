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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.oejb2;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tss-linkType", namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", propOrder = {
    "ejbName",
    "tssName",
    "jndiName"
})
public class TssLinkType {

    @XmlElement(name = "ejb-name")
    protected String ejbName;

    @XmlElement(name = "tss-name")
    protected String tssName;

    @XmlElement(name = "jndi-name")
    protected List<String> jndiName;

    public TssLinkType() {
    }

    public TssLinkType(String ejbName, String tssName, List<String> jndiName) {
        this.ejbName = ejbName;
        this.tssName = tssName;
        this.jndiName = jndiName;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public String getTssName() {
        return tssName;
    }

    public void setTssName(String tssName) {
        this.tssName = tssName;
    }

    public List<String> getJndiName() {
        if (jndiName == null){
            jndiName = new ArrayList<String>();
        }
        return jndiName;
    }
}
