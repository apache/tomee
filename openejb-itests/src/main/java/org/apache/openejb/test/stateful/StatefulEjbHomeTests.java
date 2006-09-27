package org.apache.openejb.test.stateful;

import javax.ejb.EJBMetaData;

/**
 * [3] Should be run as the third test suite of the BasicStatefulTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulEjbHomeTests extends BasicStatefulTestClient{

    public StatefulEjbHomeTests(){
        super("EJBHome.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulHome");
        ejbHome = (BasicStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatefulHome.class);
    }

    //===============================
    // Test ejb home methods
    //
    public void test01_getEJBMetaData(){
        try{
        EJBMetaData ejbMetaData = ejbHome.getEJBMetaData();
        assertNotNull( "The EJBMetaData is null", ejbMetaData );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_getHomeHandle(){
        try{
            ejbHomeHandle = ejbHome.getHomeHandle();
            assertNotNull( "The HomeHandle is null", ejbHomeHandle );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * ------------------------------------
     * 5.3.2 Removing a session object
     * A client may remove a session object using the remove() method on the javax.ejb.EJBObject
     * interface, or the remove(Handle handle) method of the javax.ejb.EJBHome interface.
     *
     * Because session objects do not have primary keys that are accessible to clients, invoking the
     * javax.ejb.EJBHome.remove(Object primaryKey) method on a session results in the
     * javax.ejb.RemoveException.
     *
     * ------------------------------------
     * 5.5 Session object identity
     *
     * Session objects are intended to be private resources used only by the
     * client that created them. For this reason, session objects, from the
     * client’s perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their
     * identity. As a result, the EJBObject.getPrimaryKey() and
     * EJBHome.remove(Object primaryKey) methods result in a java.rmi.RemoteException
     * if called on a session bean. If the EJBMetaData.getPrimaryKeyClass()
     * method is invoked on a EJBMetaData object for a Session bean, the method throws
     * the java.lang.RuntimeException.
     * ------------------------------------
     *
     * Sections 5.3.2 and 5.5 conflict.  5.3.2 says to throw javax.ejb.RemoveException, 5.5 says to
     * throw java.rmi.RemoteException.
     *
     * For now, we are going with java.rmi.RemoteException.
     */
    public void test03_removeByPrimaryKey(){
        try{
            ejbHome.remove("primaryKey");
        } catch (java.rmi.RemoteException e){
            assertTrue( true );
            return;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        assertTrue("java.rmi.RemoteException should have been thrown", false );
    }
    //
    // Test ejb home methods
    //===============================
}
