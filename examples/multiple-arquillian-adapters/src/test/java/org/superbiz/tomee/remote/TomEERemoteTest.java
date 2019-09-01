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
package org.superbiz.tomee.remote;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.superbiz.SomeEJB;

import javax.ejb.EJB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(TomEERemote.class)
@RunWith(Arquillian.class)
public class TomEERemoteTest {

    @EJB
    private SomeEJB ejb;

    @Deployment
    public static WebArchive war() {
        // use test name for the war otherwise arquillian ejb enricher doesn't work
        // don't forget the category otherwise it will fail since the runner parse annotations
        // in embedded mode it is not so important
        return ShrinkWrap.create(WebArchive.class, "test.war").addClasses(SomeEJB.class, TomEERemote.class);
    }

    @Test
    public void check() {
        assertNotNull(ejb);
        assertEquals("ejb", ejb.ok());
    }
}
