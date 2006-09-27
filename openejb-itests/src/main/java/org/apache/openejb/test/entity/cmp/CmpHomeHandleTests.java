package org.apache.openejb.test.entity.cmp;

import javax.ejb.EJBHome;

/**
 * [6] Should be run as the sixth test suite of the BasicCmpTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpHomeHandleTests extends BasicCmpTestClient{

    public CmpHomeHandleTests(){
        super("HomeHandle.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/BasicCmpHome");
        ejbHome = (BasicCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicCmpHome.class);
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
