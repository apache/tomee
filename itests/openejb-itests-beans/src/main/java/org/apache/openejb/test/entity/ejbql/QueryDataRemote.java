/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.ejbql;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface QueryDataRemote extends EJBObject {
    public Integer getId() throws RemoteException;
    public void setId(Integer id) throws RemoteException;

    public boolean getBooleanField() throws RemoteException;
    public void setBooleanField(boolean value) throws RemoteException;

    public char getCharField() throws RemoteException;
    public void setCharField(char value) throws RemoteException;

    public byte getByteField() throws RemoteException;
    public void setByteField(byte value) throws RemoteException;

    public short getShortField() throws RemoteException;
    public void setShortField(short value) throws RemoteException;

    public int getIntField() throws RemoteException;
    public void setIntField(int value) throws RemoteException;

    public long getLongField() throws RemoteException;
    public void setLongField(long value) throws RemoteException;

    public float getFloatField() throws RemoteException;
    public void setFloatField(float value) throws RemoteException;

    public double getDoubleField() throws RemoteException;
    public void setDoubleField(double value) throws RemoteException;

    public String getStringField() throws RemoteException;
    public void setStringField(String value) throws RemoteException;
}
