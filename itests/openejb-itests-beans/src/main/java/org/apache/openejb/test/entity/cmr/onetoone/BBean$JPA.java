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

import org.apache.openejb.test.entity.SingleValuedCmr;
import org.apache.openejb.test.entity.CmrFactory;

public class BBean$JPA extends BBean {
    public static Object deploymentInfo;
    public Integer field1;
    public String field2;
    public Integer field3;
    public String field4;
    public ABean$JPA a;
    private SingleValuedCmr<ABean$JPA, ALocal> aCmr = CmrFactory.cmrFactory.createSingleValuedCmr(this, ABean$JPA.class, "b");

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
        return aCmr.getEjbProxy(a);
    }

    public void setA(ALocal a) {
        this.a = aCmr.updateEntityBean(a);
    }
}
