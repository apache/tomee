package org.apache.openejb.test.stateful;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.apache.openejb.test.object.ObjectGraph;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface RmiIiopStatefulObject extends javax.ejb.EJBObject{
    
    public String returnStringObject(String data) throws RemoteException; 

    public String[] returnStringObjectArray(String[] data) throws RemoteException; 

    public Character returnCharacterObject(Character data) throws RemoteException; 

    public char returnCharacterPrimitive(char data) throws RemoteException; 

    public Character[] returnCharacterObjectArray(Character[] data) throws RemoteException; 

    public char[] returnCharacterPrimitiveArray(char[] data) throws RemoteException; 

    public Boolean returnBooleanObject(Boolean data) throws RemoteException; 

    public boolean returnBooleanPrimitive(boolean data) throws RemoteException; 

    public Boolean[] returnBooleanObjectArray(Boolean[] data) throws RemoteException; 

    public boolean[] returnBooleanPrimitiveArray(boolean[] data) throws RemoteException; 

    public Byte returnByteObject(Byte data) throws RemoteException; 

    public byte returnBytePrimitive(byte data) throws RemoteException; 

    public Byte[] returnByteObjectArray(Byte[] data) throws RemoteException; 

    public byte[] returnBytePrimitiveArray(byte[] data) throws RemoteException; 

    public Short returnShortObject(Short data) throws RemoteException; 

    public short returnShortPrimitive(short data) throws RemoteException; 

    public Short[] returnShortObjectArray(Short[] data) throws RemoteException; 

    public short[] returnShortPrimitiveArray(short[] data) throws RemoteException; 

    public Integer returnIntegerObject(Integer data) throws RemoteException; 

    public int returnIntegerPrimitive(int data) throws RemoteException; 

    public Integer[] returnIntegerObjectArray(Integer[] data) throws RemoteException; 

    public int[] returnIntegerPrimitiveArray(int[] data) throws RemoteException; 

    public Long returnLongObject(Long data) throws RemoteException; 

    public long returnLongPrimitive(long data) throws RemoteException; 

    public Long[] returnLongObjectArray(Long[] data) throws RemoteException; 

    public long[] returnLongPrimitiveArray(long[] data) throws RemoteException; 

    public Float returnFloatObject(Float data) throws RemoteException; 

    public float returnFloatPrimitive(float data) throws RemoteException; 

    public Float[] returnFloatObjectArray(Float[] data) throws RemoteException; 

    public float[] returnFloatPrimitiveArray(float[] data) throws RemoteException; 

    public Double returnDoubleObject(Double data) throws RemoteException; 

    public double returnDoublePrimitive(double data) throws RemoteException; 

    public Double[] returnDoubleObjectArray(Double[] data) throws RemoteException; 

    public double[] returnDoublePrimitiveArray(double[] data) throws RemoteException; 

    public EJBHome returnEJBHome(EJBHome data) throws RemoteException; 

    public EJBHome returnEJBHome() throws RemoteException; 

    public ObjectGraph returnNestedEJBHome() throws RemoteException; 

    public EJBHome[] returnEJBHomeArray(EJBHome[] data) throws RemoteException; 

    public EJBObject returnEJBObject(EJBObject data) throws RemoteException; 

    public EJBObject returnEJBObject() throws RemoteException; 

    public ObjectGraph returnNestedEJBObject() throws RemoteException; 

    public EJBObject[] returnEJBObjectArray(EJBObject[] data) throws RemoteException; 

    public EJBMetaData returnEJBMetaData(EJBMetaData data) throws RemoteException; 

    public EJBMetaData returnEJBMetaData() throws RemoteException; 

    public ObjectGraph returnNestedEJBMetaData() throws RemoteException; 

    public EJBMetaData[] returnEJBMetaDataArray(EJBMetaData[] data) throws RemoteException; 

    public Handle returnHandle(Handle data) throws RemoteException; 

    public Handle returnHandle() throws RemoteException; 

    public ObjectGraph returnNestedHandle() throws RemoteException; 

    public Handle[] returnHandleArray(Handle[] data) throws RemoteException; 

    public ObjectGraph returnObjectGraph(ObjectGraph data) throws RemoteException; 

    public ObjectGraph[] returnObjectGraphArray(ObjectGraph[] data) throws RemoteException; 

}
