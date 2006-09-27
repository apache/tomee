package org.apache.openejb.test.stateless;

import javax.ejb.EJBHome;

/**
 * [6] Should be run as the sixth test suite of the BasicStatelessTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessHomeHandleTests extends BasicStatelessTestClient{

    public StatelessHomeHandleTests(){
        super("HomeHandle.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbHomeHandle = ejbHome.getHomeHandle();
    }
        
    //=================================
    // Test home handle methods
    //
    public void test01_getEJBHome(){
        try{
            EJBHome home = ejbHomeHandle.getEJBHome();
            assertNotNull( "The EJBHome is null", home );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test home handle methods
    //=================================

}
