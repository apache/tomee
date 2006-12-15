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
package org.apache.openejb.test.entity.cmr.onetomany;

import org.apache.openejb.test.entity.CmrFactory;
import org.apache.openejb.test.entity.MultiValuedCmr;

import java.util.HashSet;
import java.util.Set;

public class ABean_JPA extends ABean {
    public static Object deploymentInfo;
    public transient boolean deleted;
    public Integer field1;
    private String field2;
    private Set<BBean_JPA> b = new HashSet<BBean_JPA>();
    private MultiValuedCmr<BBean_JPA, BLocal> bCmr = CmrFactory.cmrFactory.createMultiValuedCmr(this, "b", BBean_JPA.class, "a");
    private Set<BBean_JPA> bNonCascade = new HashSet<BBean_JPA>();
    private MultiValuedCmr<BBean_JPA, BLocal> bNonCascadeCmr = CmrFactory.cmrFactory.createMultiValuedCmr(this, "bNonCascade", BBean_JPA.class, "aNonCascade");

    public Integer getField1() {
        return field1;
    }

    public void setField1(Integer field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public static Object getDeploymentInfo() {
        return deploymentInfo;
    }

    public static void setDeploymentInfo(Object deploymentInfo) {
        ABean_JPA.deploymentInfo = deploymentInfo;
    }

    public Set<BLocal> getB() {
        return bCmr.get(b);
    }

    public void setB(Set<BLocal> b) {
        bCmr.set(this.b, b);
    }

    public Set<BLocal> getBNonCascade() {
        return bNonCascadeCmr.get(bNonCascade);
    }

    public void setBNonCascade(Set<BLocal> bNonCascade) {
        bNonCascadeCmr.set(this.bNonCascade, bNonCascade);
    }

    public void OpenEJB_deleted() {
        if (deleted) return;
        deleted = true;

        bCmr.deleted(b);
        bNonCascadeCmr.deleted(bNonCascade);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) return null;

        Object oldValue = null;
        if ("b".equals(name)) {
            b.add((BBean_JPA) bean);
        } else if ("bNonCascade".equals(name)) {
            bNonCascade.add((BBean_JPA) bean);
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
        return oldValue;
    }

    public void OpenEJB_removeCmr(String name, Object value) {
        if (deleted) return;

        if ("b".equals(name)) {
            b.remove(value);
        } else if ("bNonCascade".equals(name)) {
            bNonCascade.remove(value);
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
    }
}
