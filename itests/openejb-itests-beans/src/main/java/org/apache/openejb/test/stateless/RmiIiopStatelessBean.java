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
package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.apache.openejb.test.object.ObjectGraph;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class RmiIiopStatelessBean implements javax.ejb.SessionBean{
    
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

        data = (EJBHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");

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

        Object object = ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
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
        EncStatelessObject data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatelessHome home = (EncStatelessHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
        data = home.create();

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedEJBObject() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatelessHome home = (EncStatelessHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
        EncStatelessObject object = home.create();
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

        EncStatelessHome home = (EncStatelessHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
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

        EncStatelessHome home = (EncStatelessHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
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

        EncStatelessHome home = (EncStatelessHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
        EncStatelessObject object = home.create();
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

        EncStatelessHome home = (EncStatelessHome)ctx.lookup("java:comp/env/stateless/rmi-iiop/home");
        EncStatelessObject object = home.create();
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
    
    public String remove(String arg) {
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
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * 
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate() throws javax.ejb.CreateException{
        this.name = "nameless automaton";
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        // Should never called.
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        // Should never called.
    }

    //    
    // SessionBean interface methods
    //================================
}
