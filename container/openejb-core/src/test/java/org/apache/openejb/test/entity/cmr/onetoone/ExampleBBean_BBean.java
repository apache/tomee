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

import org.apache.openejb.core.cmp.cmp2.Cmp2Entity;
import org.apache.openejb.core.cmp.cmp2.SingleValuedCmr;

public class ExampleBBean_BBean extends LicenseBean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer field1;
    private String field2;
    private Integer field3;
    private String field4;
    private ExampleABean_ABean a;
    private final SingleValuedCmr<ExampleABean_ABean, PersonLocal> aCmr = new SingleValuedCmr<>(this, "a", ExampleABean_ABean.class, "b");

    public Integer getId() {
        return field1;
    }

    public void setId(final Integer field1) {
        this.field1 = field1;
    }

    public String getNumber() {
        return field2;
    }

    public void setNumber(final String field2) {
        this.field2 = field2;
    }

    public Integer getPoints() {
        return field3;
    }

    public void setPoints(final Integer field3) {
        this.field3 = field3;
    }

    public String getNotes() {
        return field4;
    }

    public void setNotes(final String field4) {
        this.field4 = field4;
    }

    public PersonLocal getPerson() {
        return aCmr.get(a);
    }

    public void setPerson(final PersonLocal person) {
        this.a = aCmr.set(this.a, person);
    }

    public Object OpenEJB_getPrimaryKey() {
        return field1;
    }

    public void OpenEJB_deleted() {
        if (deleted) {
            return;
        }
        deleted = true;

        aCmr.set(a, null);
    }

    public Object OpenEJB_addCmr(final String name, final Object bean) {
        if (deleted) {
            return null;
        }

        final Object oldValue;
        if ("a".equals(name)) {
            oldValue = a;
            a = (ExampleABean_ABean) bean;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
        return oldValue;
    }

    public void OpenEJB_removeCmr(final String name, final Object bean) {
        if (deleted) {
            return;
        }

        if ("a".equals(name)) {
            a = null;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
    }
}
