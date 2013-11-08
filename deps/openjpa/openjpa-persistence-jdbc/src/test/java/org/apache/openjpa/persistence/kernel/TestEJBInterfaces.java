/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.kernel;

import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.InterfaceHolder;
import org.apache.openjpa.persistence.kernel.common.apps.InterfaceTestImpl1;
import org.apache.openjpa.persistence.kernel.common.apps.InterfaceTestImpl2;
import org.apache.openjpa.persistence.kernel.common.apps.InterfaceTestImpl3;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

/**
 * Test for persistent interfaces.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
public class TestEJBInterfaces extends AbstractTestCase {

    public TestEJBInterfaces(String name) {
        super(name, "kernelcactusapp");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp(InterfaceTestImpl1.class, InterfaceTestImpl2.class, InterfaceTestImpl3.class,
            InterfaceHolder.class);
    }

    @SuppressWarnings("unchecked")
    public void testInterfaceField() {
        EntityManager em2 = currentEntityManager();
        startTx(em2);

        em2.flush();
        endTx(em2);
        endEm(em2);

        EntityManager em = currentEntityManager();
        startTx(em);
        InterfaceHolder holder = new InterfaceHolder(1);
        em.persist(holder);
        holder.setIntf(new InterfaceTestImpl1("intf-1-field"));
        assertEquals("intf-1-field", holder.getIntf().getStringField());
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        startTx(em);
        InterfaceHolder hold = em.find(InterfaceHolder.class, 1);
        assertNotNull(hold.getIntf());
        assertEquals("intf-1-field", hold.getIntf().getStringField());
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        startTx(em);
        hold = em.find(InterfaceHolder.class, 1);
        hold.setIntf(null);
        assertNull(hold.getIntf());
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        startTx(em);
        hold = em.find(InterfaceHolder.class, 1);
        assertNull(hold.getIntf());
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        startTx(em);
        hold = em.find(InterfaceHolder.class, 1);
        hold.setIntf(new InterfaceTestImpl2("intf-2-field"));
        assertEquals("intf-2-field", hold.getIntf().getStringField());
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        startTx(em);
        hold = em.find(InterfaceHolder.class, 1);
        assertNotNull(hold.getIntf());
        assertEquals("intf-2-field", hold.getIntf().getStringField());
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        startTx(em);
        hold = em.find(InterfaceHolder.class, 1);
        hold.getIntfs().add(new InterfaceTestImpl1("intf-1-set"));
        endTx(em);
        endEm(em);

//		em = currentEntityManager();
//		startTx(em);
//		hold = (InterfaceHolder)em.find(InterfaceHolder.class, 1);
//		assertEquals (1, hold.getIntfs ().size ());
//      assertEquals ("intf-1-set", ((InterfaceTest) hold.getIntfs().iterator().
//              next()).getStringField());
//		endTx(em);
//		endEm(em);
    }
}
