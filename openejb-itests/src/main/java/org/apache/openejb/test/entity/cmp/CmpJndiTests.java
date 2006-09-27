package org.apache.openejb.test.entity.cmp;


/**
 * [1] Should be run as the first test suite of the BasicCmpTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpJndiTests extends BasicCmpTestClient{

    public CmpJndiTests(){
        super("JNDI.");
    }

    public void test01_Jndi_lookupHome(){
        try{
            Object obj = initialContext.lookup("client/tests/entity/cmp/BasicCmpHome");
            ejbHome = (BasicCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicCmpHome.class);
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
