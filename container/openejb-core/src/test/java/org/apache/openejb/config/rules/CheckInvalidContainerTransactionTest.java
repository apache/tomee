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

import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.TransAttribute;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckInvalidContainerTransactionTest {
    @Keys( { @Key("containerTransaction.ejbNameRequired"), @Key("containerTransaction.noSuchEjbName") })
    public EjbJar test() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ContainerTransaction tx = new ContainerTransaction(TransAttribute.REQUIRED, new Method((String) null, (String) null));
        ejbJar.getAssemblyDescriptor().getContainerTransaction().add(tx);
        ContainerTransaction tx1 = new ContainerTransaction(TransAttribute.REQUIRED, new Method("wrongEjbName", "wrongMethodName"));
        ejbJar.getAssemblyDescriptor().getContainerTransaction().add(tx1);
        return ejbJar;
    }
}