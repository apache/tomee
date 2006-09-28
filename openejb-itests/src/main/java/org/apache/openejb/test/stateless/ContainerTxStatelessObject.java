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

import javax.transaction.RollbackException;

import org.apache.openejb.test.object.Account;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface ContainerTxStatelessObject extends javax.ejb.EJBObject{
    
    public String txMandatoryMethod(String message) throws RemoteException;
    
    public String txNeverMethod(String message) throws RemoteException;
    
    public String txNotSupportedMethod(String message) throws RemoteException;
    
    public String txRequiredMethod(String message) throws RemoteException;
    
    public String txRequiresNewMethod(String message) throws RemoteException;
    
    public String txSupportsMethod(String message) throws RemoteException;

    public Account retreiveAccount(String ssn) throws RemoteException;
    
    public void openAccount(Account acct, Boolean rollback) throws RemoteException, RollbackException;
}
