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
package org.apache.openejb.arquillian.tests.cdi.ejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;

/**
 * OWB-743 - Overloaded EJB Observer methods fail to deploy
 *
 * @version $Revision$ $Date$
 */
@RunWith(Arquillian.class)
public class OverloadedEjbObserverMethodsTest {

    @EJB
    private Painter painter;

    @Resource
    private BeanManager beanManager;


    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap.create(WebArchive.class, "OverloadedEjbObserverMethodsTest.war")
                .addClasses(Painter.class, Orange.class, Green.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void test() throws Exception {
        final Orange orange = new Orange();
        beanManager.fireEvent(orange);

        final Green green = new Green();
        beanManager.fireEvent(green);

        Assert.assertEquals(2, painter.getObserved().size());
        Assert.assertSame(orange, painter.getObserved().get(0));
        Assert.assertSame(green, painter.getObserved().get(1));
    }

    @Singleton
    public static class Painter {

        private List<Object> observed = new ArrayList<Object>();

        public void observe(@Observes Orange orange) {
            observed.add(orange);
        }

        public void observe(@Observes Green green) {
            observed.add(green);
        }

        public List<Object> getObserved() {
            return observed;
        }
    }

    public static class Orange {
    }

    public static class Green {
    }

}
