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
package org.apache.openejb.test.entity.cmr.onetoone;

import org.apache.openejb.core.cmp.cmp2.SingleValuedCmr;
import org.apache.openejb.core.cmp.cmp2.Cmp2Entity;

public class ExampleABean_JPA extends ABean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer field1;
    private String field2;
    private ExampleBBean_JPA b;
    private SingleValuedCmr<ExampleBBean_JPA, BLocal> bCmr = new SingleValuedCmr<ExampleBBean_JPA, BLocal>(this, "b", ExampleBBean_JPA.class, "a");

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

    public BLocal getB() {
        return bCmr.get(b);
    }

    public void setB(BLocal b) {
        this.b = bCmr.set(this.b, b);
    }

    public Object OpenEJB_getPrimaryKey() {
        return field1;
    }

    public void OpenEJB_deleted() {
        if (deleted) {
            return;
        }
        deleted = true;

        bCmr.set(b, null);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) {
            return null;
        }

        Object oldValue;
        if ("b".equals(name)) {
            oldValue = b;
            b = (ExampleBBean_JPA) bean;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
        return oldValue;
    }

    public void OpenEJB_removeCmr(String name, Object bean) {
        if (deleted) {
            return;
        }

        if ("b".equals(name)) {
            b = null;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
    }
}
