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

import org.apache.openejb.core.cmp.cmp2.Cmp2Entity;
import org.apache.openejb.core.cmp.cmp2.SingleValuedCmr;

public class ExampleBBean_JPA extends BBean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer field1;
    private String field2;
    private Integer field3;
    private String field4;
    private ExampleABean_JPA a;
    private SingleValuedCmr aCmr = new SingleValuedCmr(this, "a", ExampleABean_JPA.class, "b");

    private ExampleABean_JPA aNonCascade;
    private SingleValuedCmr aNonCascadeCmr = new SingleValuedCmr(this, "aNonCascade", ExampleABean_JPA.class, "bNonCascade");

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
        return (ALocal) aCmr.get(a);
    }

    public void setA(ALocal a) {
        this.a = (ExampleABean_JPA) aCmr.set(this.a, a);
    }

    public ALocal getANonCascade() {
        return (ALocal) aNonCascadeCmr.get(aNonCascade);
    }

    public void setANonCascade(ALocal aNonCascade) {
        this.aNonCascade = (ExampleABean_JPA) aNonCascadeCmr.set(this.aNonCascade, aNonCascade);
    }

    public Object OpenEJB_getPrimaryKey() {
        return field1;
    }

    public void OpenEJB_deleted() {
        if (deleted) return;
        deleted = true;

        aCmr.deleted(a);
        aNonCascadeCmr.deleted(aNonCascade);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) return null;

        if ("a".equals(name)) {
            Object oldValue = a;
            a = (ExampleABean_JPA) bean;
            return oldValue;
        }

        if ("aNonCascade".equals(name)) {
            Object oldValue = aNonCascade;
            aNonCascade = (ExampleABean_JPA) bean;
            return oldValue;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }

    public void OpenEJB_removeCmr(String name, Object bean) {
        if (deleted) return;

        if ("a".equals(name)) {
            a = null;
            return;
        }

        if ("aNonCascade".equals(name)) {
            aNonCascade = null;
            return;
        }
        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }
}
