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
package org.apache.openejb.timer;

import java.io.Serializable;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.ObjectName;

//import org.apache.geronimo.kernel.KernelRegistry;
//import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TimerHandleImpl implements TimerHandle, Serializable {

    private final long id;
    private final String kernelName;
    private final ObjectName timerSourceName;

    public TimerHandleImpl(long id, String kernelName, ObjectName timerSourceName) {
        this.id = id;
        this.kernelName = kernelName;
        this.timerSourceName = timerSourceName;
    }

    public Timer getTimer() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        // TODO: Kernel Reference
//        Kernel kernel = KernelRegistry.getKernel(kernelName);
//        try {
//            return (Timer) kernel.invoke(timerSourceName, "getTimerById", new Object[] {new Long(id)}, new String[] {Long.class.getName()});
//        } catch (IllegalStateException e) {
//            throw e;
//        } catch (EJBException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new EJBException(e);
//        }
        return null;
    }
}
