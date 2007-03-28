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

import java.util.Properties;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.TestManager;

/**
 * [1] Should be run as the first test suite of the StatelessTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessContainerTxTests extends org.apache.openejb.test.NamedTestCase{

    public final static String jndiEJBHomeEntry = "client/tests/stateless/ContainerManagedTransactionTests/EJBHome";

    protected ContainerTxStatelessHome   ejbHome;
    protected ContainerTxStatelessObject ejbObject;

    protected EJBMetaData       ejbMetaData;
    protected HomeHandle        ejbHomeHandle;
    protected Handle            ejbHandle;
    protected Integer           ejbPrimaryKey;

    protected InitialContext initialContext;

    public StatelessContainerTxTests(){
        super("Stateless.ContainerManagedTransaction.");
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {

        Properties properties = TestManager.getServer().getContextEnvironment();
        //properties.put(Context.SECURITY_PRINCIPAL, "STATELESS_test00_CLIENT");
        //properties.put(Context.SECURITY_CREDENTIALS, "STATELESS_test00_CLIENT");

        initialContext = new InitialContext(properties);

        /*[1] Get bean */
        Object obj = initialContext.lookup(jndiEJBHomeEntry);
        ejbHome = (ContainerTxStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, ContainerTxStatelessHome.class);
        ejbObject = ejbHome.create();

        /*[2] Create database table */
        TestManager.getDatabase().createAccountTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropAccountTable();
    }

    public void test01_txMandatory_withoutTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txMandatoryMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_txNever_withoutTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNeverMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_txNotSupported_withoutTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNotSupportedMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_txRequired_withoutTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiredMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_txRequiresNew_withoutTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiresNewMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test06_txSupports_withoutTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txSupportsMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test07_txMandatory_withTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txMandatoryMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test08_txNever_withTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNeverMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test09_txNotSupported_withTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNotSupportedMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test10_txRequired_withTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiredMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test11_txRequiresNew_withTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiresNewMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test12_txSupports_withTx(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txSupportsMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test01_txMandatory_withoutTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txMandatoryMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_txNever_withoutTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNeverMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_txNotSupported_withoutTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNotSupportedMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_txRequired_withoutTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiredMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_txRequiresNew_withoutTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiresNewMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test06_txSupports_withoutTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txSupportsMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test07_txMandatory_withTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txMandatoryMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test08_txNever_withTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNeverMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test09_txNotSupported_withTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNotSupportedMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test10_txRequired_withTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiredMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test11_txRequiresNew_withTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiresNewMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test12_txSupports_withTx_appException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txSupportsMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test01_txMandatory_withoutTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txMandatoryMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_txNever_withoutTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNeverMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_txNotSupported_withoutTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNotSupportedMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_txRequired_withoutTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiredMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_txRequiresNew_withoutTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiresNewMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test06_txSupports_withoutTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txSupportsMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test07_txMandatory_withTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txMandatoryMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test08_txNever_withTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNeverMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test09_txNotSupported_withTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txNotSupportedMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test10_txRequired_withTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiredMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test11_txRequiresNew_withTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txRequiresNewMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test12_txSupports_withTx_sysException(){
        try{
            String expected = "ping";
            String actual   = ejbObject.txSupportsMethod( expected );
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
}

