/**
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
package org.apache.openejb.config.rules;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import jakarta.ejb.Local;
import jakarta.ejb.Remote;

@RunWith(ValidationRunner.class)
public class CheckInvalidAnnotatedInterfacesTest {
    @BeforeClass
    public static void beforeClass() {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
    }

    @Keys({@Key("ann.local.noAttributes"), @Key("ann.remote.noAttributes")})
    public EjbJar noAttributes() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooBeanLocal.class));
        ejbJar.addEnterpriseBean(new StatelessBean(FooBeanRemote.class));
        return ejbJar;
    }

    @Keys({@Key("ann.localRemote.ambiguous"), @Key("ann.localRemote.conflict")})
    public EjbJar ambiguous() {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooBeanLocalRemote.class));
        ejbJar.addEnterpriseBean(new StatelessBean(FooBeanLocalOne.class));
        return ejbJar;
    }

    @After
    public void after() {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "false");
    }

    @Local
    public static class FooBeanLocal implements FooLocalOne, FooLocalTwo {
    }

    @Remote
    public static class FooBeanRemote implements FooRemoteOne, FooRemoteTwo {
    }

    @Local
    @Remote
    public static class FooBeanLocalRemote implements FooLocalOne {
    }

    @Local(FooRemoteOne.class)
    @Remote(FooRemoteOne.class)
    public static class FooBeanLocalOne {
    }

    @Remote
    public static interface FooRemoteOne {
    }

    @Remote
    public static interface FooRemoteTwo {
    }

    @Local
    public static interface FooLocalOne {
    }

    @Local
    public static interface FooLocalTwo {
    }
}
