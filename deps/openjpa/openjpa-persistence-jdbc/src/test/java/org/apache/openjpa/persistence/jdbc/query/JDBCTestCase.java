/*
 * JDBCTestCase.java
 *
 * Created on October 6, 2006, 10:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.jdbc.query;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;

import org.apache.openjpa.persistence.jdbc.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;


public abstract class JDBCTestCase extends AbstractTestCase {
    
    /** Creates a new instance of JDBCTestCase */
    public JDBCTestCase() {
    }
    
    
    public JDBCTestCase(String name) {
        super(name);
    }
    
    public Class getDefaultInheritanceStrategy() {
        return getClassMapping(RuntimeTest2.class).getStrategy().getClass();
    }
    
    public ClassMapping getClassMapping(Class c) {
        OpenJPAConfiguration jdoConf = getConfiguration();
        return ((JDBCConfiguration) jdoConf).getMappingRepositoryInstance().
                getMapping(c, getClass().getClassLoader(), true);
    }
    
    public FieldMapping getFieldMapping(Class c, String field) {
        return getClassMapping(c).getFieldMapping(field);
    }
    
    public boolean isInheritanceStrategyVertical() {
        return VerticalClassStrategy.class.
                isAssignableFrom(getDefaultInheritanceStrategy());
    }
    
}
