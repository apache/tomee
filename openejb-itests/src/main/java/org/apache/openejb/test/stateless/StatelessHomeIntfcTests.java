package org.apache.openejb.test.stateless;


/**
 * [2] Should be run as the second test suite of the BasicStatelessTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessHomeIntfcTests extends BasicStatelessTestClient{

    public StatelessHomeIntfcTests(){
        super("HomeIntfc.");
    }
    
    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
    }
    
    //===============================
    // Test home interface methods
    //
    public void test01_create(){
        try{
            ejbObject = ejbHome.create();
            assertNotNull( "The EJBObject is null", ejbObject );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test home interface methods
    //===============================

}
