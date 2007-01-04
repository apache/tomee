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

import java.util.HashSet;
import java.util.Set;

import org.apache.openejb.core.cmp.cmp2.Cmp2Entity;
import org.apache.openejb.core.cmp.cmp2.SetValuedCmr;

public class ExampleABean_JPA extends ABean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer field1;
    private String field2;
    private Set<ExampleBBean_JPA> b = new HashSet<ExampleBBean_JPA>();
    private SetValuedCmr bCmr = new SetValuedCmr(this, "b", ExampleBBean_JPA.class, "a");

    private Set<ExampleBBean_JPA> bNonCascade = new HashSet<ExampleBBean_JPA>();
    private SetValuedCmr bNonCascadeCmr = new SetValuedCmr(this, "bNonCascade", ExampleBBean_JPA.class, "aNonCascade");

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

    public Set getB() {
        return bCmr.get(b);
    }

    public void setB(Set b) {
        bCmr.set(this.b, b);
    }

    public Set getBNonCascade() {
        return bNonCascadeCmr.get(bNonCascade);
    }

    public void setBNonCascade(Set bNonCascade) {
        bNonCascadeCmr.set(this.bNonCascade, bNonCascade);
    }

    public Object OpenEJB_getPrimaryKey() {
        return field1;
    }

    public void OpenEJB_deleted() {
        if (deleted) return;
        deleted = true;

        bCmr.deleted(b);
        bNonCascadeCmr.deleted(bNonCascade);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) return null;

        if ("b".equals(name)) {
            b.add((ExampleBBean_JPA) bean);
            return null;
        }

        if ("bNonCascade".equals(name)) {
            bNonCascade.add((ExampleBBean_JPA) bean);
            return null;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }

    public void OpenEJB_removeCmr(String name, Object value) {
        if (deleted) return;

        if ("b".equals(name)) {
            b.remove(value);
            return;
        }

        if ("bNonCascade".equals(name)) {
            bNonCascade.remove(value);
            return;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }
}
