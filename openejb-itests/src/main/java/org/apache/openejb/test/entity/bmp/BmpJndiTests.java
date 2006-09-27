package org.apache.openejb.test.entity.bmp;


/**
 * [1] Should be run as the first test suite of the BasicBmpTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpJndiTests extends BasicBmpTestClient{

    public BmpJndiTests(){
        super("JNDI.");
    }

    public void test01_Jndi_lookupHome(){
        try{
            Object obj = initialContext.lookup("client/tests/entity/bmp/BasicBmpHome");
            ejbHome = (BasicBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicBmpHome.class);
            assertNotNull("The EJBHome is null", ejbHome);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /* TO DO:  
     * public void test00_enterpriseBeanAccess()       
     * public void test00_jndiAccessToJavaCompEnv()
     * public void test00_resourceManagerAccess()
     */

}
