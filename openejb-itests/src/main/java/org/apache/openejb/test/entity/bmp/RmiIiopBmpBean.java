package org.apache.openejb.test.entity.bmp;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.EntityContext;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;

import org.apache.openejb.test.object.ObjectGraph;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class RmiIiopBmpBean implements javax.ejb.EntityBean{
    private int primaryKey;
    private String firstName;
    private String lastName;
    private EntityContext ejbContext;
    
    
    //=============================
    // Home interface methods
    //    
    
    /**
     * Maps to RmiIiopBmpHome.findEmptyCollection
     * 
     * @param primaryKey
     * @return 
     * @exception javax.ejb.FinderException
     * @see RmiIiopBmpHome#sum
     */
    public java.util.Collection ejbFindEmptyCollection()
    throws javax.ejb.FinderException, java.rmi.RemoteException {
        return new java.util.Vector();
    }

    /**
     * Maps to RmiIiopBmpHome.findByPrimaryKey
     * 
     * @param primaryKey
     * @return 
     * @exception javax.ejb.FinderException
     * @see RmiIiopBmpHome#sum
     */
    public Integer ejbFindByPrimaryKey(Integer primaryKey)
    throws javax.ejb.FinderException{
        return new Integer(-1);
    }

    /**
     * Maps to RmiIiopBmpHome.create
     * 
     * @param name
     * @return 
     * @exception javax.ejb.CreateException
     * @see RmiIiopBmpHome#create
     */
    public Integer ejbCreate(String name)
    throws javax.ejb.CreateException{
        return new Integer(-1);
    }
    
    public void ejbPostCreate(String name)
    throws javax.ejb.CreateException{
    }
    
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    /*-------------------------------------------------*/
    /*  String                                         */  
    /*-------------------------------------------------*/
    
    public String returnStringObject(String data) {
        return data;
    }
    
    public String[] returnStringObjectArray(String[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Character                                      */  
    /*-------------------------------------------------*/
    
    public Character returnCharacterObject(Character data) {
        return data;
    }
    
    public char returnCharacterPrimitive(char data) {
        return data;
    }
    
    public Character[] returnCharacterObjectArray(Character[] data) {
        return data;
    }
    
    public char[] returnCharacterPrimitiveArray(char[] data) {
        return data;
    }

    /*-------------------------------------------------*/
    /*  Boolean                                        */  
    /*-------------------------------------------------*/
    
    public Boolean returnBooleanObject(Boolean data) {
        return data;
    }
    
    public boolean returnBooleanPrimitive(boolean data) {
        return data;
    }
    
    public Boolean[] returnBooleanObjectArray(Boolean[] data) {
        return data;
    }
    
    public boolean[] returnBooleanPrimitiveArray(boolean[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Byte                                           */  
    /*-------------------------------------------------*/
    
    public Byte returnByteObject(Byte data) {
        return data;
    }
    
    public byte returnBytePrimitive(byte data) {
        return data;
    }
    
    public Byte[] returnByteObjectArray(Byte[] data) {
        return data;
    }
    
    public byte[] returnBytePrimitiveArray(byte[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Short                                          */  
    /*-------------------------------------------------*/
    
    public Short returnShortObject(Short data) {
        return data;
    }
    
    public short returnShortPrimitive(short data) {
        return data;
    }
    
    public Short[] returnShortObjectArray(Short[] data) {
        return data;
    }
    
    public short[] returnShortPrimitiveArray(short[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Integer                                        */  
    /*-------------------------------------------------*/
    
    public Integer returnIntegerObject(Integer data) {
        return data;
    }
    
    public int returnIntegerPrimitive(int data) {
        return data;
    }
    
    public Integer[] returnIntegerObjectArray(Integer[] data) {
        return data;
    }
    
    public int[] returnIntegerPrimitiveArray(int[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Long                                           */  
    /*-------------------------------------------------*/
    
    public Long returnLongObject(Long data) {
        return data;
    }
    
    public long returnLongPrimitive(long data) {
        return data;
    }
    
    public Long[] returnLongObjectArray(Long[] data) {
        return data;
    }
    
    public long[] returnLongPrimitiveArray(long[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Float                                          */  
    /*-------------------------------------------------*/
    
    public Float returnFloatObject(Float data) {
        return data;
    }
    
    public float returnFloatPrimitive(float data) {
        return data;
    }
    
    public Float[] returnFloatObjectArray(Float[] data) {
        return data;
    }
    
    public float[] returnFloatPrimitiveArray(float[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Double                                         */  
    /*-------------------------------------------------*/
    
    public Double returnDoubleObject(Double data) {
        return data;
    }
    
    public double returnDoublePrimitive(double data) {
        return data;
    }
    
    public Double[] returnDoubleObjectArray(Double[] data) {
        return data;
    }
    
    public double[] returnDoublePrimitiveArray(double[] data) {
        return data;
    }
    
    
    /*-------------------------------------------------*/
    /*  EJBHome                                         */  
    /*-------------------------------------------------*/
    
    public EJBHome returnEJBHome(EJBHome data) {
        return data;
    }
    
    public EJBHome returnEJBHome() throws javax.ejb.EJBException{
        EJBHome data = null;

        try{
        InitialContext ctx = new InitialContext();

        data = (EJBHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");

        } catch (Exception e){
            e.printStackTrace();
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedEJBHome() throws javax.ejb.EJBException{
        ObjectGraph data = null; 

        try{
        InitialContext ctx = new InitialContext();

        Object object = ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        data = new ObjectGraph(object);

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }

    public EJBHome[] returnEJBHomeArray(EJBHome[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  EJBObject                                      */  
    /*-------------------------------------------------*/
    
    public EJBObject returnEJBObject(EJBObject data) {
        return data;
    }
    
    public EJBObject returnEJBObject() throws javax.ejb.EJBException{
        EncBmpObject data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncBmpHome home = (EncBmpHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        data = home.create("Test01 BmpBean");

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedEJBObject() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncBmpHome home = (EncBmpHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        EncBmpObject object = home.create("Test02 BmpBean");
        data = new ObjectGraph(object);

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public EJBObject[] returnEJBObjectArray(EJBObject[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  EJBMetaData                                         */  
    /*-------------------------------------------------*/
    
    public EJBMetaData returnEJBMetaData(EJBMetaData data) {
        return data;
    }
    
    public EJBMetaData returnEJBMetaData() throws javax.ejb.EJBException{
        EJBMetaData data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncBmpHome home = (EncBmpHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        data = home.getEJBMetaData();

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedEJBMetaData() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncBmpHome home = (EncBmpHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        EJBMetaData object = home.getEJBMetaData();
        data = new ObjectGraph(object);

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public EJBMetaData[] returnEJBMetaDataArray(EJBMetaData[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Handle                                         */  
    /*-------------------------------------------------*/
    
    public Handle returnHandle(Handle data) {
        return data;
    }
    
    public Handle returnHandle() throws javax.ejb.EJBException{
        Handle data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncBmpHome home = (EncBmpHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        EncBmpObject object = home.create("Test03 BmpBean");
        data = object.getHandle();

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedHandle() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncBmpHome home = (EncBmpHome)ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
        EncBmpObject object = home.create("Test04 BmpBean");
        data = new ObjectGraph(object.getHandle());

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public Handle[] returnHandleArray(Handle[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  ObjectGraph                                         */  
    /*-------------------------------------------------*/
    
    public ObjectGraph returnObjectGraph(ObjectGraph data) {
        return data;
    }
    
    public ObjectGraph[] returnObjectGraphArray(ObjectGraph[] data) {
        return data;
    }
    //    
    // Remote interface methods
    //=============================


    //================================
    // EntityBean interface methods
    //    
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by loading it state from the
     * underlying database.
     */
    public void ejbLoad() throws EJBException,RemoteException {
    }
    
    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(EntityContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    
    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() throws EJBException,RemoteException {
    }
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() throws EJBException,RemoteException {
    }
    
    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() throws RemoveException,EJBException,RemoteException {
    }
    
    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() throws EJBException,RemoteException {
    }
    
    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
    }

    //    
    // EntityBean interface methods
    //================================
}
