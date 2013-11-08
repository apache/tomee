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
package org.apache.openjpa.persistence.jdbc.annotations;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.strats.ValueMapDiscriminatorStrategy;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <p>Test discriminator mappings.</p>
 *
 * @author Abe White
 */
public class TestDiscriminator
    extends SingleEMFTestCase {

    public void setUp() throws Exception {
        super.setUp(AnnoTest1.class, AnnoTest2.class, Flat1.class,
            CLEAR_TABLES);
        // Commented out since OpenJPA does not have Entity1 and Entity2.
        // These tests should be ported to use classes that are available
        // in OpenJPA.
//        setUp(Entity1.class, Entity2.class, AnnoTest1.class, CLEAR_TABLES);
    }

//    public void testNoDefaultJoinedDiscriminatorWithoutColumn() {
//        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
//        ClassMapping cls = conf.getMappingRepositoryInstance().
//            getMapping(Entity1.class, null, true);
//        if (conf.getDBDictionaryInstance().joinSyntax
//            != JoinSyntaxes.SYNTAX_TRADITIONAL) {
//            assertTrue(cls.getDiscriminator().getStrategy()
//                instanceof SubclassJoinDiscriminatorStrategy);
//        } else {
//            assertEquals(NoneDiscriminatorStrategy.getInstance(),
//                cls.getDiscriminator().getStrategy());
//        }
//        assertEquals(0, cls.getDiscriminator().getColumns().length);
//    }

    public void testJoinedDiscriminatorWithColumn() {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        MappingRepository repo = conf.getMappingRepositoryInstance();
        ClassMapping cls = repo.getMapping(AnnoTest1.class, 
            null, true);
        assertTrue(cls.getDiscriminator().getStrategy()
            instanceof ValueMapDiscriminatorStrategy);
        assertEquals(1, cls.getDiscriminator().getColumns().length);
        assertEquals("ANNOCLS", cls.getDiscriminator().getColumns()[0].
            getName());
    }

//    public void testDefaultFlatDiscriminator() {
//        ClassMapping cls = ((JDBCConfiguration) emf.getConfiguration()).
//            getMappingRepositoryInstance().getMapping(Entity2.class,
//            null, true);
//        assertTrue(cls.getDiscriminator().getStrategy()
//            instanceof ValueMapDiscriminatorStrategy);
//        assertEquals(1, cls.getDiscriminator().getColumns().length);
//    }
}
