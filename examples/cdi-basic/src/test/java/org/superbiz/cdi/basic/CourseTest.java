/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cdi.basic;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.embeddable.EJBContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CourseTest {

    private static EJBContainer container;

    @EJB
    private Course course;

    @BeforeClass
    public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before
    public void setUp() throws Exception {
        container.getContext().bind("inject", this);
    }

    @Test
    public void test() {

        // Was the EJB injected?
        assertTrue(course != null);

        // Was the Course @PostConstruct called?
        assertNotNull(course.getCourseName());
        assertTrue(course.getCapacity() > 0);

        // Was a Faculty instance injected into Course?
        final Faculty faculty = course.getFaculty();
        assertTrue(faculty != null);

        // Was the @PostConstruct called on Faculty?
        assertEquals(faculty.getFacultyName(), "Computer Science");
        assertEquals(faculty.getFacultyMembers().size(), 2);
    }

    @AfterClass
    public static void stop() {
        container.close();
    }
}
