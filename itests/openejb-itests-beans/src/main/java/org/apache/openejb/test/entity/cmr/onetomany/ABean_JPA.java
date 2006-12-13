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

import org.apache.openejb.test.entity.MultiValuedCmr;
import org.apache.openejb.test.entity.CmrFactory;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class ABean_JPA extends ABean {
    public static Object deploymentInfo;
    public Integer field1;
    private String field2;
    private Map<Integer,BBean_JPA> b = new HashMap<Integer, BBean_JPA>();
    private MultiValuedCmr<BBean_JPA, BLocal, Integer> bCmr = CmrFactory.cmrFactory.createMultiValuedCmr(this, "b", BBean_JPA.class, "a");

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

    public void OpenEJB_deleted() {
        bCmr.set(b, Collections.<BLocal>emptySet());
    }

    public Object OpenEJB_addCmr(String name, Object pk, Object bean) {
        Object oldValue = null;
        if ("b".equals(name)) {
            b.put((Integer)pk, (BBean_JPA) bean);
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
        return oldValue;
    }

    public void OpenEJB_removeCmr(String name, Object pk, Object value) {
        if ("b".equals(name)) {
            b.remove(pk);
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
    }
}
