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

public class ExampleBBean_BBean extends SongBean implements Cmp2Entity {
    public static Object deploymentInfo;
    private transient boolean deleted;
    private Integer field1;
    private String field2;
    private Integer field3;
    private String field4;
    private ExampleABean_ABean a;
    private SingleValuedCmr aCmr = new SingleValuedCmr(this, "a", ExampleABean_ABean.class, "b");

    private ExampleABean_ABean aNonCascade;
    private SingleValuedCmr aNonCascadeCmr = new SingleValuedCmr(this, "aNonCascade", ExampleABean_ABean.class, "bNonCascade");

    public Integer getId() {
        return field1;
    }

    public void setId(Integer field1) {
        this.field1 = field1;
    }

    public String getName() {
        return field2;
    }

    public void setName(String field2) {
        this.field2 = field2;
    }

    public Integer getBpm() {
        return field3;
    }

    public void setBpm(Integer field3) {
        this.field3 = field3;
    }

    public String getDescription() {
        return field4;
    }

    public void setDescription(String field4) {
        this.field4 = field4;
    }

    public ArtistLocal getPerformer() {
        return (ArtistLocal) aCmr.get(a);
    }

    public void setPerformer(ArtistLocal artist) {
        this.a = (ExampleABean_ABean) aCmr.set(this.a, artist);
    }

    public ArtistLocal getComposer() {
        return (ArtistLocal) aNonCascadeCmr.get(aNonCascade);
    }

    public void setComposer(ArtistLocal artistNonCascade) {
        this.aNonCascade = (ExampleABean_ABean) aNonCascadeCmr.set(this.aNonCascade, artistNonCascade);
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
            a = (ExampleABean_ABean) bean;
            return oldValue;
        }

        if ("aNonCascade".equals(name)) {
            Object oldValue = aNonCascade;
            aNonCascade = (ExampleABean_ABean) bean;
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
