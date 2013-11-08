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
package org.apache.openjpa.persistence.enhance;

import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import javax.persistence.EntityManager;
import java.io.IOException;

public class TestEmbeddedEnumSqlGeneration
    extends AbstractTestCase {

    private PCEnhancer.Flags flags = new PCEnhancer.Flags();

    public TestEmbeddedEnumSqlGeneration(String s) {
        super(s, "org.apache.openjpa.persistence.enhance.EntityWithEnum");
    }

    public void setUp() {
    }

    public void testEnumEnhancement()
    throws IOException {
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        MappingTool tool = new MappingTool((JDBCConfiguration)
                ((OpenJPAEntityManagerSPI) kem).getConfiguration(),
                MappingTool.ACTION_REFRESH, false);
        tool.run(EntityWithEnum.class);
        tool.record();
    }
}
