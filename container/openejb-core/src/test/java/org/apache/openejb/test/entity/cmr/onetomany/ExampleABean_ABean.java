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
import org.apache.openejb.core.cmp.cmp2.SetValuedCmr;

import java.util.HashSet;
import java.util.Set;

public class ExampleABean_ABean extends ArtistBean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer field1;
    private String field2;
    private final Set<ExampleBBean_BBean> b = new HashSet<>();
    private final SetValuedCmr bCmr = new SetValuedCmr(this, "b", ExampleBBean_BBean.class, "a");

    private final Set<ExampleBBean_BBean> bNonCascade = new HashSet<>();
    private final SetValuedCmr bNonCascadeCmr = new SetValuedCmr(this, "bNonCascade", ExampleBBean_BBean.class, "aNonCascade");

    public Integer getId() {
        return field1;
    }

    public void setId(final Integer field1) {
        this.field1 = field1;
    }

    public String getName() {
        return field2;
    }

    public void setName(final String field2) {
        this.field2 = field2;
    }

    public Set getPerformed() {
        return bCmr.get(b);
    }

    public void setPerformed(final Set b) {
        bCmr.set(this.b, b);
    }

    public Set getComposed() {
        return bNonCascadeCmr.get(bNonCascade);
    }

    public void setComposed(final Set bNonCascade) {
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

    public Object OpenEJB_addCmr(final String name, final Object bean) {
        if (deleted) return null;

        if ("b".equals(name)) {
            b.add((ExampleBBean_BBean) bean);
            return null;
        }

        if ("bNonCascade".equals(name)) {
            bNonCascade.add((ExampleBBean_BBean) bean);
            return null;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }

    public void OpenEJB_removeCmr(final String name, final Object value) {
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
