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

import java.io.IOException;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.util.OpenJPAException;

public class TestNoNoArgs
    extends AbstractTestCase {

    OpenJPAConfiguration conf;
    MetaDataRepository repos;
    ClassLoader loader;
    private PCEnhancer.Flags flags = new PCEnhancer.Flags();

    public TestNoNoArgs(String s) {
        super(s, "enhancecactusapp");
    }

    public void setUp() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        conf = ((OpenJPAEntityManagerSPI) em).getConfiguration();
        repos = conf.newMetaDataRepositoryInstance();
        loader = em.getClassLoader();
        endEm(em);
    }

    public void testNoNoArgs()
        throws IOException {
        PCEnhancer.run((OpenJPAConfiguration) conf,
            new String[]{ },
            flags, repos, null, loader);
    }

    public void testNo2NoArgs()
        throws IOException {
        flags.addDefaultConstructor = false;
        boolean caughtException = false;
        try {
            PCEnhancer.run((OpenJPAConfiguration) conf, new String[]{ },
                flags, repos, null, loader);
        } catch (OpenJPAException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
    }

    public void testNo3NoArgs()
        throws IOException {
        PCEnhancer.run((OpenJPAConfiguration) conf,
            new String[]{ "persistence.enhance.common.apps.Entity1" },
            flags, repos, null, loader);
    }
}
