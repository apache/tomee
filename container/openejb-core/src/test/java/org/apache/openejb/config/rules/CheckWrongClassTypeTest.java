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
package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.PersistenceType;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckWrongClassTypeTest {
    @Keys( { @Key("wrong.class.type"), @Key("noInterfaceDeclared.entity") })
    public EjbJar wrongClassType() throws OpenEJBException {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
        EjbJar ejbJar = new EjbJar();
        EntityBean entityBean = new EntityBean();
        entityBean.setEjbClass(FooEntity.class);
        entityBean.setEjbName("fooEntity");
        entityBean.setPersistenceType(PersistenceType.BEAN);
        ejbJar.addEnterpriseBean(entityBean);
        return ejbJar;
    }

    private static class FooEntity {}
}
