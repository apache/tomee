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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config.rules;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.runner.RunWith;

import jakarta.interceptor.AroundInvoke;

@RunWith(ValidationRunner.class)
public class CheckIncorrectPropertyNameTest {


    @Keys({@Key(value = "incorrect.property.name", type = KeyType.WARNING)})
    public AppModule testSystemPropertyNames() {
        //SystemInstance.get().setProperty("java.persistence.provider", "test");
        SystemInstance.get().setProperty("javax.naming.referral", "test");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(Green.class));
        return new AppModule(new EjbModule(ejbJar));
    }
}


class Green {

    // need to add this @AroundInvoke to cause validation to fail. Validation does not
    // fail on warnings, which causes this framework to not work properly
    @AroundInvoke
    public void sayCheese() {
    }
}