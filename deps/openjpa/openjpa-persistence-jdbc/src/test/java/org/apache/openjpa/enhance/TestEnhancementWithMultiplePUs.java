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
package org.apache.openjpa.enhance;

import java.io.IOException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.util.BytecodeWriter;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;
import serp.bytecode.BCClass;
import serp.bytecode.Project;

public class TestEnhancementWithMultiplePUs
    extends AbstractCachedEMFTestCase {

    public void testExplicitEnhancementWithClassNotInFirstPU()
        throws ClassNotFoundException {
        OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
        Configurations.populateConfiguration(conf, new Options());
        MetaDataRepository repos = conf.getMetaDataRepositoryInstance();
        ClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newTemporaryClassLoaderAction(
                getClass().getClassLoader()));
        Project project = new Project();

        String className =
            "org.apache.openjpa.enhance.UnenhancedBootstrapInstance";
        BCClass bc = assertNotPC(loader, project, className);

        PCEnhancer enhancer = new PCEnhancer(conf, bc, repos, loader);

        assertEquals(PCEnhancer.ENHANCE_PC, enhancer.run());
        assertTrue(Arrays.asList(bc.getInterfaceNames()).contains(
            PersistenceCapable.class.getName()));
    }

    private BCClass assertNotPC(ClassLoader loader, Project project,
        String className) {
        BCClass bc = project.loadClass(className, loader);
        assertFalse(className + " must not be enhanced already; it was.",
            Arrays.asList(bc.getInterfaceNames()).contains(
                PersistenceCapable.class.getName()));
        return bc;
    }

    public void testEnhancementOfSecondPUWithClassNotInFirstPU()
        throws IOException {
        OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
        Options opts = new Options();
        opts.setProperty("p",
            "META-INF/persistence.xml#second-persistence-unit");
        Configurations.populateConfiguration(conf, opts);
        MetaDataRepository repos = conf.getMetaDataRepositoryInstance();
        ClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newTemporaryClassLoaderAction(
                getClass().getClassLoader()));
        Project project = new Project();

        // make sure that the class is not already enhanced for some reason
        String className =
            "org.apache.openjpa.enhance.UnenhancedBootstrapInstance";
        BCClass bc = assertNotPC(loader, project, className);

        // build up a writer that just stores to a list so that we don't
        // mutate the disk.
        final List<String> written = new ArrayList<String>();
        BytecodeWriter writer = new BytecodeWriter() {

            public void write(BCClass type) throws IOException {
                assertTrue(Arrays.asList(type.getInterfaceNames()).contains(
                    PersistenceCapable.class.getName()));
                written.add(type.getName());
            }
        };

        PCEnhancer.run(conf, null, new PCEnhancer.Flags(), repos, writer,
            loader);

        // ensure that we don't attempt to process classes listed in other PUs
        assertEquals(1, written.size());

        // ensure that we do process the classes listed in the PU
        assertTrue(written.contains(className));
    }

    public void testEnhancementOfAllPUsWithinAResource()
        throws IOException {
        OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
        Options opts = new Options();
        opts.setProperty("p", "META-INF/persistence.xml");
        Configurations.populateConfiguration(conf, opts);
        MetaDataRepository repos = conf.getMetaDataRepositoryInstance();
        ClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newTemporaryClassLoaderAction(
                getClass().getClassLoader()));
        Project project = new Project();

        // make sure that the classes is not already enhanced for some reason
        assertNotPC(loader, project,
            "org.apache.openjpa.enhance.UnenhancedBootstrapInstance");
        assertNotPC(loader, project,
            "org.apache.openjpa.enhance.UnenhancedBootstrapInstance2");

        // build up a writer that just stores to a list so that we don't
        // mutate the disk.
        final List<String> written = new ArrayList<String>();
        BytecodeWriter writer = new BytecodeWriter() {

            public void write(BCClass type) throws IOException {
                assertTrue(Arrays.asList(type.getInterfaceNames()).contains(
                    PersistenceCapable.class.getName()));
                written.add(type.getName());
            }
        };

        opts = new Options();
        // Use a restricted mdr.  This mdr will not hand out metadata for excluded
        // types.  These are types that have known issues and should not be enhanced.
        // This test tries to enhance all persistent types in the classpath and that
        // can be problematic for tests which include entities that this test should
        // not attempt to enhance.
        opts.setProperty("MetaDataRepository", 
            "org.apache.openjpa.enhance.RestrictedMetaDataRepository(excludedTypes=" +
            "org.apache.openjpa.persistence.jdbc.annotations.UnenhancedMixedAccess)");
        opts.put(PCEnhancer.class.getName() + "#bytecodeWriter", writer);
        PCEnhancer.run(null, opts);

        // ensure that we do process the classes listed in the PUs
        assertTrue(written.contains(
            "org.apache.openjpa.enhance.UnenhancedBootstrapInstance"));
        assertTrue(written.contains(
            "org.apache.openjpa.enhance.UnenhancedBootstrapInstance2"));
    }
}
