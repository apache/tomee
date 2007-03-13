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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;


/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"ejbDeployment"})
@XmlRootElement(name = "openejb-jar")
public class OpenejbJar {

    @XmlElement(name = "ejb-deployment", required = true)
    protected List<EjbDeployment> ejbDeployment;

    public List<EjbDeployment> getEjbDeployment() {
        if (ejbDeployment == null) {
            ejbDeployment = new ArrayList<EjbDeployment>();
        }
        return this.ejbDeployment;
    }

    public Map<String,EjbDeployment> getDeploymentsById(){
        Map<String,EjbDeployment> map = new LinkedHashMap();
        for (EjbDeployment deployment : getEjbDeployment()) {
            map.put(deployment.getDeploymentId(), deployment);
        }
        return map;
    }

    public Map<String,EjbDeployment> getDeploymentsByEjbName(){
        Map<String,EjbDeployment> map = new LinkedHashMap();
        for (EjbDeployment deployment : getEjbDeployment()) {
            map.put(deployment.getEjbName(), deployment);
        }
        return map;
    }

    public int getEjbDeploymentCount() {
        return getEjbDeployment().size();
    }

    public void addEjbDeployment(EjbDeployment ejbDeployment) {
        getEjbDeployment().add(ejbDeployment);
    }

    public void removeEjbDeployment(EjbDeployment ejbDeployment) {
        getEjbDeployment().remove(ejbDeployment);
    }
}
