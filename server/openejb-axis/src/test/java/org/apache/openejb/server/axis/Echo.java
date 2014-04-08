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


public interface Echo extends java.rmi.Remote {
    public void echoVoid() throws java.rmi.RemoteException;

    public int echoInt(int in) throws java.rmi.RemoteException;

    public double echoDouble(double in) throws java.rmi.RemoteException;

    public float echoFloat(float in) throws java.rmi.RemoteException;

    public boolean echoBoolean(boolean in) throws java.rmi.RemoteException;

    public String echoString(String in) throws java.rmi.RemoteException;

    public short echoShort(short in) throws java.rmi.RemoteException;

    public long echoLong(long in) throws java.rmi.RemoteException;

    //public char echoChar(char in);
    public byte[] echoBytes(byte[] in) throws java.rmi.RemoteException;

    public void echoEvoid() throws java.rmi.RemoteException;

    public EchoStruct echoStruct(EchoStruct in) throws java.rmi.RemoteException;
    //public EchoStruct[] echoAStruct(EchoStruct[] in)throws java.rmi.RemoteException;

}
