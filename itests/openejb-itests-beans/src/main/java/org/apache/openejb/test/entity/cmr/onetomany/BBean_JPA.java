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

import org.apache.openejb.test.entity.SingleValuedCmr;
import org.apache.openejb.test.entity.CmrFactory;

public class BBean_JPA extends BBean {
    public static Object deploymentInfo;
    public Integer field1;
    private String field2;
    private Integer field3;
    private String field4;
    private ABean_JPA a;
    private SingleValuedCmr<ABean_JPA, ALocal> aCmr = CmrFactory.cmrFactory.createSingleValuedCmr(this, "a", ABean_JPA.class, "b");

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

    public Integer getField3() {
        return field3;
    }

    public void setField3(Integer field3) {
        this.field3 = field3;
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public ALocal getA() {
        return aCmr.get(a);
    }

    public void OpenEJB_deleted() {
        a = aCmr.set(a, null);
    }

    public void setA(ALocal a) {
        this.a = aCmr.set(this.a, a);
    }

    public Object OpenEJB_addCmr(String name, Object pk, Object bean) {
        Object oldValue;
        if ("a".equals(name)) {
            oldValue = a;
            a = (ABean_JPA) bean;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
        return oldValue;
    }

    public void OpenEJB_removeCmr(String name, Object pk, Object bean) {
        if ("a".equals(name)) {
            a = null;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
    }
}
