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

public class ExampleABean_ABean extends PersonBean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer id;
    private String name;
    private ExampleBBean_BBean License;
    private SingleValuedCmr<ExampleBBean_BBean, LicenseLocal> bCmr = new SingleValuedCmr<ExampleBBean_BBean, LicenseLocal>(this, "b", ExampleBBean_BBean.class, "a");

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LicenseLocal getLicense() {
        return bCmr.get(License);
    }

    public void setLicense(LicenseLocal license) {
        this.License = bCmr.set(this.License, license);
    }

    public Object OpenEJB_getPrimaryKey() {
        return id;
    }

    public void OpenEJB_deleted() {
        if (deleted) {
            return;
        }
        deleted = true;

        bCmr.set(License, null);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) {
            return null;
        }

        Object oldValue;
        if ("b".equals(name)) {
            oldValue = License;
            License = (ExampleBBean_BBean) bean;
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
            License = null;
        } else {
            throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        }
    }
}
