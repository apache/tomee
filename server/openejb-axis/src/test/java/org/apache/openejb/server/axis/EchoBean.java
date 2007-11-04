/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis;

public class EchoBean implements Echo {
    public void ejbCreate() {
    }

    public void echoVoid() {
    }

    public int echoInt(int in) {
        return in;
    }

    public double echoDouble(double in) {
        return in;
    }

    public float echoFloat(float in) {
        return in;
    }

    public boolean echoBoolean(boolean in) {
        return in;
    }

    public String echoString(String in) {
        return in;
    }

    public short echoShort(short in) {
        return in;
    }

    public long echoLong(long in) {
        return in;
    }

    public char echoChar(char in) {
        return in;
    }

    public byte[] echoBytes(byte[] in) {
        return in;
    }

    public void echoEvoid() {
    }

    public EchoStruct echoStruct(EchoStruct in) {
        return in;
    }
    
//	public EchoStruct[] echoAStruct(EchoStruct[] in){
//		return in;
//	}

    public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void setSessionContext(javax.ejb.SessionContext arg0) throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
}
