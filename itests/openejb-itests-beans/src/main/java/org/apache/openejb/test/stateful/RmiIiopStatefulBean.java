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
package org.apache.openejb.test.stateful;

import java.rmi.RemoteException;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.SessionContext;
import javax.naming.InitialContext;

import org.apache.openejb.test.object.ObjectGraph;

public class RmiIiopStatefulBean implements jakarta.ejb.SessionBean {

    private String name;
    private SessionContext ejbContext;


    //=============================
    // Home interface methods
    //    
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

            data = (EJBHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");

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

            final Object object = ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
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
        EncStatefulObject data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncStatefulHome home = (EncStatefulHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
            data = home.create("Test01 StatefulBean");

        } catch (final Exception e) {
            throw new jakarta.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedEJBObject() throws jakarta.ejb.EJBException {
        ObjectGraph data = null;

        try {
            final InitialContext ctx = new InitialContext();

            final EncStatefulHome home = (EncStatefulHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
            final EncStatefulObject object = home.create("Test02 StatefulBean");
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

            final EncStatefulHome home = (EncStatefulHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
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

            final EncStatefulHome home = (EncStatefulHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
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

            final EncStatefulHome home = (EncStatefulHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
            final EncStatefulObject object = home.create("Test03 StatefulBean");
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

            final EncStatefulHome home = (EncStatefulHome) ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
            final EncStatefulObject object = home.create("Test04 StatefulBean");
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

    public String remove(final String arg) {
        return arg;
    }

    //    
    // Remote interface methods
    //=============================


    //================================
    // SessionBean interface methods
    //    

    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(final SessionContext ctx) throws EJBException, RemoteException {
        ejbContext = ctx;
    }

    /**
     * @param name
     * @throws jakarta.ejb.CreateException
     */
    public void ejbCreate(final String name) throws jakarta.ejb.CreateException {
        this.name = name;
    }

    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException, RemoteException {
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException, RemoteException {
        // Should never called.
    }

    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
        // Should never called.
    }

    //    
    // SessionBean interface methods
    //================================
}
