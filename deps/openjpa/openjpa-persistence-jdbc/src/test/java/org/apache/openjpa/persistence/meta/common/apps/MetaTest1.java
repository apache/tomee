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
package org.apache.openjpa.persistence.meta.common.apps;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class MetaTest1 {

    // these should not be persistent
    private static int staticField;
    private final String finalField = null;
    private transient char transientField;
    private MetaTest4 metaTest4Field;
    //@OneToMany(mappedBy="MetaTest4", fetch=FetchType.LAZY)
    @PersistentCollection
    private java.util.Set<MetaTest4> metaTest4ArrayField;
    //private MetaTest4[] metaTest4ArrayField;
    private Object objectField;
    private Long longWrapperField;
    private double doubleField;

    // persistent fields -- see metadata
    private String stringField;
    private Integer intWrapperField;
    private int intField;
    private MetaTest2 metaTest2Field;
    //@OneToMany(mappedBy="MetaTest2", fetch=FetchType.LAZY)
    @PersistentCollection
    private java.util.Set<MetaTest2> metaTest2ArrayField;
    //private MetaTest2[] metaTest2ArrayField;
    @PersistentCollection
    private int[] intArrayField;

    @Entity
    @Table(name="MetaTest1_Inner")
    public static class Inner {

        private long longField;
    }
}
