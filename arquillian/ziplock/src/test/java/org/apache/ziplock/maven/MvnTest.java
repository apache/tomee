/*
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
package org.apache.ziplock.maven;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MvnTest {
    @Test
    public void main() {
        final Archive<?> war = Mvn.war();
        System.out.println(war.toString(true));
        assertTrue(war.get("/WEB-INF/classes/org/apache/ziplock/maven/Mvn.class") != null);
        assertTrue(war.getContent(new Filter<ArchivePath>() {
            @Override
            public boolean include(final ArchivePath archivePath) {
                return archivePath.get().startsWith("/WEB-INF/lib") && archivePath.get().contains("jakartaee-api");
            }
        }).size() == 1);
        assertTrue(war.getContent(new Filter<ArchivePath>() {
            @Override
            public boolean include(final ArchivePath archivePath) {
                return archivePath.get().startsWith("/WEB-INF/lib") && archivePath.get().contains("junit");
            }
        }).isEmpty());
    }

    @Test
    public void test() {
        final Archive<?> war = Mvn.testWar();
        System.out.println(war.toString(true));
        assertTrue(war.get("/WEB-INF/classes/org/apache/ziplock/maven/Mvn.class") != null);
        assertTrue(war.getContent(new Filter<ArchivePath>() {
            @Override
            public boolean include(final ArchivePath archivePath) {
                return archivePath.get().startsWith("/WEB-INF/lib") && archivePath.get().contains("jakartaee-api");
            }
        }).size() == 1);
        assertTrue(war.getContent(new Filter<ArchivePath>() {
            @Override
            public boolean include(final ArchivePath archivePath) {
                return archivePath.get().startsWith("/WEB-INF/lib") && archivePath.get().contains("junit");
            }
        }).size() == 1);
    }
}
