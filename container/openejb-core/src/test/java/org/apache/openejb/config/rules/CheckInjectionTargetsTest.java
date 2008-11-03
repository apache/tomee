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

import junit.framework.TestCase;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ValidationError;
import org.apache.openejb.config.ValidationWarning;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class CheckInjectionTargetsTest extends TestCase {

    private EjbModule module;
    private StatelessBean bean;
    
    private CheckInjectionTargets rule;
    
    @Before
    public void initialize() {
        module = new EjbModule(new EjbJar());
        bean = module.getEjbJar().addEnterpriseBean(new StatelessBean("CheeseEjb", "org.acme.CheeseEjb"));        

        rule = new CheckInjectionTargets();
        rule.module = module;
    }
    
    public void test() {

        // Valid
        EnvEntry envEntry = new EnvEntry("count", Integer.class.getName(), "10");
        envEntry.getInjectionTarget().add(new InjectionTarget("org.acme.CheeseEjb", "org.acme.CheeseEjb/count"));
        bean.getEnvEntry().add(envEntry);

        // Invalid - can't specify setColor, just color as a target and its setter will be calculated
        EnvEntry envEntry2 = new EnvEntry("color", String.class.getName(), "yellow");
        envEntry2.getInjectionTarget().add(new InjectionTarget("org.acme.CheeseEjb", "org.acme.CheeseEjb/setColor"));
        bean.getEnvEntry().add(envEntry2);

        // Invalid - see the comment above
        EnvEntry envEntry3 = new EnvEntry("age", Integer.class.getName(), "5");
        envEntry3.getInjectionTarget().add(new InjectionTarget("org.acme.CheeseEjb", "setAge"));
        bean.getEnvEntry().add(envEntry3);

        rule.validate(module);

        ValidationWarning[] warnings = module.getValidation().getWarnings();
        assertEquals(warnings.length, 2);

    }
    
    @Test
    public void testUserTransaction() {
        // 16.12 UserTransaction Interface
        // only session and message-driven beans with bean-managed transaction demarcation are allowed to use this interface
        ResourceEnvRef resEnvEntry = new ResourceEnvRef("jta/UserTransaction", "javax.transaction.UserTransaction");
        bean.getResourceEnvRef().add(resEnvEntry);
        
        rule.validate(module);

        ValidationError[] errors = module.getValidation().getErrors();
        assertEquals(errors.length, 1);

        // The authenticationType and shareable elements of the Resource annotation must not be specified
    }

}
