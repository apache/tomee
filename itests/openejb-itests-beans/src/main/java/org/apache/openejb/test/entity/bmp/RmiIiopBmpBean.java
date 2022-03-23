/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.bmp;

import java.rmi.RemoteException;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EntityContext;
import jakarta.ejb.Handle;
import jakarta.ejb.RemoveException;
import javax.naming.InitialContext;

import org.apache.openejb.test.object.ObjectGraph;

public class RmiIiopBmpBean implements jakarta.ejb.EntityBean {
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
     * @return
     * @throws jakarta.ejb.FinderException
     * @see RmiIiopBmpHome#sum
     */
    public java.util.Collection ejbFindEmptyCollection()
        throws jakarta.ejb.FinderException, java.rmi.RemoteException {
        return new java.util.Vector();
    }

    /**
     * Maps to RmiIiopBmpHome.findByPrimaryKey
     *
     * @param primaryKey
     * @return
     * @throws jakarta.ejb.FinderException
     * @see RmiIiopBmpHome#sum
     */
    public Integer ejbFindByPrimaryKey(final Integer primaryKey)
        throws jakarta.ejb.FinderException {
        return new Integer(-1);
    }

    /**
     * Maps to RmiIiopBmpHome.create
     *
     * @param name
     * @return
     * @throws jakarta.ejb.CreateException
     * @see RmiIiopBmpHome#create
     */
    public Integer ejbCreate(final String name)
        throws jakarta.ejb.CreateException {
        return new Integer(-1);
    }

    public void ejbPostCreate(final String name)
        throws jakarta.ejb.CreateException {
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

    public String returnStringObject(final String data) {
        return data;
    }

    public String[] returnStringObjectArray(final String[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Character                                      */  
    /*-------------------------------------------------*/

    public Character returnCharacterObject(final Character data) {
        return data;
    }

    public char returnCharacterPrimitive(final char data) {
        return data;
    }

    public Character[] returnCharacterObjectArray(final Character[] data) {
        return data;
    }

    public char[] returnCharacterPrimitiveArray(final char[] data) {
        return data;
    }

    /*-------------------------------------------------*/
    /*  Boolean                                        */  
    /*-------------------------------------------------*/

    public Boolean returnBooleanObject(final Boolean data) {
        return data;
    }

    public boolean returnBooleanPrimitive(final boolean data) {
        return data;
    }

    public Boolean[] returnBooleanObjectArray(final Boolean[] data) {
        return data;
    }

    public boolean[] returnBooleanPrimitiveArray(final boolean[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Byte                                           */  
    /*-------------------------------------------------*/

    public Byte returnByteObject(final Byte data) {
        return data;
    }

    public byte returnBytePrimitive(final byte data) {
        return data;
    }

    public Byte[] returnByteObjectArray(final Byte[] data) {
        return data;
    }

    public byte[] returnBytePrimitiveArray(final byte[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Short                                          */  
    /*-------------------------------------------------*/

    public Short returnShortObject(final Short data) {
        return data;
    }

    public short returnShortPrimitive(final short data) {
        return data;
    }

    public Short[] returnShortObjectArray(final Short[] data) {
        return data;
    }

    public short[] returnShortPrimitiveArray(final short[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Integer                                        */  
    /*-------------------------------------------------*/

    public Integer returnIntegerObject(final Integer data) {
        return data;
    }

    public int returnIntegerPrimitive(final int data) {
        return data;
    }

    public Integer[] returnIntegerObjectArray(final Integer[] data) {
        return data;
    }

    public int[] returnIntegerPrimitiveArray(final int[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Long                                           */  
    /*-------------------------------------------------*/

    public Long returnLongObject(final Long data) {
        return data;
    }

    public long returnLongPrimitive(final long data) {
        return data;
    }

    public Long[] returnLongObjectArray(final Long[] data) {
        return data;
    }

    public long[] returnLongPrimitiveArray(final long[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Float                                          */  
    /*-------------------------------------------------*/

    public Float returnFloatObject(final Float data) {
        return data;
    }

    public float returnFloatPrimitive(final float data) {
        return data;
    }

    public Float[] returnFloatObjectArray(final Float[] data) {
        return data;
    }

    public float[] returnFloatPrimitiveArray(final float[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Double                                         */  
    /*-------------------------------------------------*/

    public Double returnDoubleObject(final Double data) {
        return data;
    }

    public double returnDoublePrimitive(final double data) {
        return data;
    }

    public Double[] returnDoubleObjectArray(final Double[] data) {
        return data;
    }

    public double[] returnDoublePrimitiveArray(final double[] data) {
        return data;
    }
    
    
    /*-------------------------------------------------*/
    /*  EJBHome                                         */  
    /*-------------------------------------------------*/

    public EJBHome returnEJBHome(final EJBHome data) {
        return data;
    }

    public EJBHome returnEJBHome() throws jakarta.ejb.EJBException {
        EJBHome data = null;

        try {
            final InitialContext ctx = new InitialContext();

            data = (EJBHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");

        } catch (final Exception e) {
            e.printStackTrace();
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedEJBHome() throws jakarta.ejb.EJBException {
        ObjectGraph data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final Object object = ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            data = new ObjectGraph(object);

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public EJBHome[] returnEJBHomeArray(final EJBHome[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  EJBObject                                      */  
    /*-------------------------------------------------*/

    public EJBObject returnEJBObject(final EJBObject data) {
        return data;
    }

    public EJBObject returnEJBObject() throws jakarta.ejb.EJBException {
        EncBmpObject data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncBmpHome home = (EncBmpHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            data = home.create("Test01 BmpBean");

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedEJBObject() throws jakarta.ejb.EJBException {
        ObjectGraph data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncBmpHome home = (EncBmpHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            final EncBmpObject object = home.create("Test02 BmpBean");
            data = new ObjectGraph(object);

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public EJBObject[] returnEJBObjectArray(final EJBObject[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  EJBMetaData                                         */  
    /*-------------------------------------------------*/

    public EJBMetaData returnEJBMetaData(final EJBMetaData data) {
        return data;
    }

    public EJBMetaData returnEJBMetaData() throws jakarta.ejb.EJBException {
        EJBMetaData data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncBmpHome home = (EncBmpHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            data = home.getEJBMetaData();

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedEJBMetaData() throws jakarta.ejb.EJBException {
        ObjectGraph data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncBmpHome home = (EncBmpHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            final EJBMetaData object = home.getEJBMetaData();
            data = new ObjectGraph(object);

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public EJBMetaData[] returnEJBMetaDataArray(final EJBMetaData[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Handle                                         */  
    /*-------------------------------------------------*/

    public Handle returnHandle(final Handle data) {
        return data;
    }

    public Handle returnHandle() throws jakarta.ejb.EJBException {
        Handle data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncBmpHome home = (EncBmpHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            final EncBmpObject object = home.create("Test03 BmpBean");
            data = object.getHandle();

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedHandle() throws jakarta.ejb.EJBException {
        ObjectGraph data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncBmpHome home = (EncBmpHome) ctx.lookup("java:comp/env/bmp/rmi-iiop/home");
            final EncBmpObject object = home.create("Test04 BmpBean");
            data = new ObjectGraph(object.getHandle());

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public Handle[] returnHandleArray(final Handle[] data) {
        return data;
    }
    

    /*-------------------------------------------------*/
    /*  Class                                         */
    /*-------------------------------------------------*/

    public Class returnClass(final Class data) {
        return data;
    }

    public Class[] returnClassArray(final Class[] data) {
        return data;
    }

    /*-------------------------------------------------*/
    /*  ObjectGraph                                         */  
    /*-------------------------------------------------*/

    public ObjectGraph returnObjectGraph(final ObjectGraph data) {
        return data;
    }

    public ObjectGraph[] returnObjectGraphArray(final ObjectGraph[] data) {
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
    public void ejbLoad() throws EJBException, RemoteException {
    }

    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(final EntityContext ctx) throws EJBException, RemoteException {
        ejbContext = ctx;
    }

    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() throws EJBException, RemoteException {
    }

    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() throws EJBException, RemoteException {
    }

    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() throws RemoveException, EJBException, RemoteException {
    }

    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() throws EJBException, RemoteException {
    }

    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
    }

    //    
    // EntityBean interface methods
    //================================
}
