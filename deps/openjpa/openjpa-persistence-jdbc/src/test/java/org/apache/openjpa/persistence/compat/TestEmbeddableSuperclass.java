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
package org.apache.openjpa.persistence.compat;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.RelationFieldStrategy;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <p>Test embeddable superclasses</p>
 *
 *  <B>This is an anti-test because it validates or masks root cause of a possibly serious error.
 *
 * <b>Compatible testcases</b> are used to test various backwards compatibility scenarios between JPA 2.0 and JPA 1.2
 * 
 * <p>The following scenarios are tested:
 * <ol>
 * <li>RelationFieldStrategy
 * </ol>
 * <p> 
 * <b>Note(s):</b>
 * <ul>
 * <li>The proper openjpa.Compatibility value(s) must be provided in order for the testcase(s) to succeed
 * </ul>
 * 
 */
public class TestEmbeddableSuperclass
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(EmbeddableSuper.class, EmbeddableSuperSub.class, CLEAR_TABLES);
    }

    public void testRelationMappings() {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(EmbeddableSuperSub.class, null, true);
        FieldMapping fm = cls.getFieldMapping("sub");
        assertTrue(fm.getStrategy() instanceof RelationFieldStrategy);

        fm = cls.getFieldMapping("sup");
        assertEquals(RelationFieldStrategy.class, fm.getStrategy().getClass());
        // This was an anti-test because it legitimizes/masks the root cause of a serious error.
        // The strategy for a field should not change without a valid reason.
        
//        if (OpenJPAVersion.MAJOR_RELEASE >= 2) {
            // OPENJPA-1214 - OpenJPA 2 returns a EmbedFieldStrategy instead of
            // a RelationFieldStrategy as in prior releases.
//            assertEquals(EmbedFieldStrategy.class, fm.getStrategy().getClass());
//        } else {
            // Prior OpenJPA 1.2/1.3 behavior
//            assertEquals(RelationFieldStrategy.class, fm.getStrategy().getClass());
//            
//        }
        
        // 
    } 
}

