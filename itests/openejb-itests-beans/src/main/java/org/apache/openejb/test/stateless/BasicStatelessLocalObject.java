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

import java.util.Properties;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:nour.mohammad@gmail.com">Mohammad Nour El-Din</a>
 */
public interface BasicStatelessLocalObject extends javax.ejb.EJBLocalObject{
    
    /**
     * Reverses the string passed in then returns it
     * 
     * @return string
     */
    public String businessMethod(String text);
    
    /**
     * Throws an ApplicationException when invoked
     * 
     */
    public void throwApplicationException() throws ApplicationException;
    
    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the 
     * destruction of the instance and invalidation of the
     * remote reference.
     * 
     */
    public void throwSystemException_NullPointer();
    
    /**
     * Returns a report of the bean's 
     * runtime permissions
     * 
     * @return properties
     */
    public Properties getPermissionsReport();
    
    /**
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return operations policy 
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName);

    /**
     * Schedules a timer with the specified name.  This name is used to notify via the TimerSyncBean.
     * @param name the name used to notify via the TimerSyncBean
     */
    public void scheduleTimer(String name);
}
