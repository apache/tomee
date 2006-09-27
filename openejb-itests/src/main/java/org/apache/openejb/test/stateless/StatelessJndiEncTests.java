package org.apache.openejb.test.stateless;

import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.TestManager;

/**
 * [4] Should be run as the fourth test suite of the EncStatelessTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessJndiEncTests extends StatelessTestClient{

    protected EncStatelessHome   ejbHome;
    protected EncStatelessObject ejbObject;
    
    public StatelessJndiEncTests(){
        super("JNDI_ENC.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/EncBean");
        ejbHome = (EncStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, EncStatelessHome.class);
        ejbObject = ejbHome.create();
        
        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        try {
            /*[1] Drop database table */
            TestManager.getDatabase().dropEntityTable();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }
    
    public void test01_lookupStringEntry() {
        try{
            ejbObject.lookupStringEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test02_lookupDoubleEntry() { 
        try{
            ejbObject.lookupDoubleEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test03_lookupLongEntry() {   
        try{
            ejbObject.lookupLongEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test04_lookupFloatEntry() {  
        try{
            ejbObject.lookupFloatEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test05_lookupIntegerEntry() {
        try{
            ejbObject.lookupIntegerEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test06_lookupShortEntry() {  
        try{
            ejbObject.lookupShortEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test07_lookupBooleanEntry() {
        try{
            ejbObject.lookupBooleanEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test08_lookupByteEntry() {   
        try{
            ejbObject.lookupByteEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test09_lookupEntityBean() {  
        try{
            ejbObject.lookupEntityBean();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test10_lookupStatefulBean() {
        try{
            ejbObject.lookupStatefulBean();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test11_lookupStatelessBean() {
        try{
            ejbObject.lookupStatelessBean();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test12_lookupResource() {
        try{
            ejbObject.lookupResource();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
}
