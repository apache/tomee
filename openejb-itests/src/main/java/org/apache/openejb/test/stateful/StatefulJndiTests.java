package org.apache.openejb.test.stateful;


/**
 * [1] Should be run as the first test suite of the BasicStatefulTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulJndiTests extends BasicStatefulTestClient{

    public StatefulJndiTests(){
        super("JNDI.");
    }

    public void test01_Jndi_lookupHome(){
        try{
            Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulHome");
            ejbHome = (BasicStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatefulHome.class);
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
