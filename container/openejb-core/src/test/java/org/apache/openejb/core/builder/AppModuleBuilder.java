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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.builder;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;

public class AppModuleBuilder {

    AppModule appModule;
    EjbModule ejbModule;
    EjbJar ejbJar = new EjbJar();
    OpenejbJar openEJBJar = new OpenejbJar();

    public AppModuleBuilder anAppModule() {
        ejbModule = new EjbModule(getClass().getClassLoader(), "FakeEjbJar", "fake.jar", ejbJar, null);
        ejbModule.setOpenejbJar(openEJBJar);

        return this;
    }

    public AppModuleBuilder withAnMdb(MessageDrivenBean mdb) {
        openEJBJar.addEjbDeployment(mdb);
        ejbJar.addEnterpriseBean(mdb);
        return this;

    }

    public AppModule build() {
        AppModule appModule = new AppModule(ejbModule);
        return appModule;
    }
}
