package org.apache.openejb.test.stateful;


/**
 * [2] Should be run as the second test suite of the BasicStatefulTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulHomeIntfcTests extends BasicStatefulTestClient{

    public StatefulHomeIntfcTests(){
        super("HomeIntfc.");
    }
    
    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulHome");
        ejbHome = (BasicStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatefulHome.class);
    }
    
    //===============================
    // Test home interface methods
    //
    public void test01_create(){
        try{
            ejbObject = ejbHome.create("First Bean");
            assertNotNull("The EJBObject is null", ejbObject);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test home interface methods
    //===============================

}
