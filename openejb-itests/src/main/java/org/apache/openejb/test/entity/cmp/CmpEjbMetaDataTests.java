package org.apache.openejb.test.entity.cmp;

import javax.ejb.EJBHome;

/**
 * [8] Should be run as the eigth test suite of the BasicCmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpEjbMetaDataTests extends BasicCmpTestClient{

    public CmpEjbMetaDataTests(){
        super("EJBMetaData.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/BasicCmpHome");
        ejbHome = (BasicCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicCmpHome.class);
        ejbMetaData = ejbHome.getEJBMetaData();
    }

    //=================================
    // Test meta data methods
    //
    public void test01_getEJBHome(){
        try{
        EJBHome home = ejbMetaData.getEJBHome();
        assertNotNull( "The EJBHome is null", home );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_getHomeInterfaceClass(){
        try{
        Class clazz = ejbMetaData.getHomeInterfaceClass();
        assertNotNull( "The Home Interface class is null", clazz );
        assertEquals(clazz , BasicCmpHome.class);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_getPrimaryKeyClass(){
        try{
        Class clazz = ejbMetaData.getPrimaryKeyClass();
        assertNotNull( "The EJBMetaData is null", clazz );
        assertEquals(clazz , Integer.class);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_getRemoteInterfaceClass(){
        try{
        Class clazz = ejbMetaData.getRemoteInterfaceClass();
        assertNotNull( "The Remote Interface class is null", clazz );
        assertEquals(clazz , BasicCmpObject.class);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_isSession(){
        try{
        assertTrue( "EJBMetaData says this is a session bean", !ejbMetaData.isSession() );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test06_isStatelessSession(){
        try{
        assertTrue( "EJBMetaData says this is a stateless session bean", !ejbMetaData.isStatelessSession() );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test meta data methods
    //=================================
}
