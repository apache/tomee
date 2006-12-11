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
package org.apache.openejb.test.stateless;

import org.apache.openejb.test.entity.bmp.EncBmpHome;
import org.apache.openejb.test.entity.bmp.EncBmpObject;
import org.apache.openejb.test.entity.cmp.EncCmpHome;
import org.apache.openejb.test.entity.cmp.EncCmpObject;
import org.apache.openejb.test.stateful.EncStatefulHome;
import org.apache.openejb.test.stateful.EncStatefulObject;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class MiscEjbTests extends BasicStatelessTestClient{

    public MiscEjbTests(){
        super("EJBObject.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbObject = ejbHome.createObject();
    }

    protected void tearDown() throws Exception {
        try {
            //ejbObject.remove();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }

    //===============================
    // Test ejb object methods
    //
    public void test01_isIdentical_stateless(){
        try{
            String jndiName = "client/tests/stateless/EncBean";
            EncStatelessHome ejbHome2 = null;
            EncStatelessObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, EncStatelessHome.class);
            ejbObject2 = ejbHome2.create();

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            //System.out.println("-------------------------------------------------------");
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_isIdentical_stateful(){
        try{
            String jndiName = "client/tests/stateful/EncBean";
            EncStatefulHome ejbHome2 = null;
            EncStatefulObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, EncStatefulHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            //System.out.println("-------------------------------------------------------");
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_isIdentical_bmp(){
        try{
            String jndiName = "client/tests/entity/bmp/EncBean";
            EncBmpHome ejbHome2 = null;
            EncBmpObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncBmpHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * DMB: Calling this now causes an error as the "entity" table doesn't exist yet
     */ 
    public void _test04_isIdentical_cmp(){
        try{
            String jndiName = "client/tests/entity/cmp/EncBean";
            EncCmpHome ejbHome2 = null;
            EncCmpObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    //
    // Test ejb object methods
    //===============================
}
