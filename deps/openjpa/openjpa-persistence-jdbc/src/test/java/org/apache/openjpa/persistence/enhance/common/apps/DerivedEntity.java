/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.enhance.common.apps;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * @see TestPCSubclasser
 */
@Entity
public class DerivedEntity
    extends BaseEntity {

    private int _i;
    private BasicSubclassInstance _basic;

    public int getIntField() {
        return _i;
    }

    public void setIntField(int i) {
        _i = i;
    }

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public BasicSubclassInstance getOneToOne() {
        return _basic;
    }

    public void setOneToOne(BasicSubclassInstance basic) {
        // ##### this should be throwing an exception, but clashes with
        // delete behavior.
        //#####if (basic == null)
        //#####throw new NullPointerException ();
        _basic = basic;
    }
}
