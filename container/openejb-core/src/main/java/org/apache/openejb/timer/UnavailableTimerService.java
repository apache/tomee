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

import java.util.Date;
import java.util.Collection;
import java.io.Serializable;
import javax.ejb.Timer;
import javax.ejb.EJBException;

/**
 * @version $Rev$ $Date$
 */
public final class UnavailableTimerService implements BasicTimerService {

    public static final BasicTimerService INSTANCE = new UnavailableTimerService();

    private UnavailableTimerService() {
    }

    public Timer createTimer(Object id, Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        throw new IllegalStateException("Timer service is not available");
    }

    public Timer createTimer(Object id, Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        throw new IllegalStateException("Timer service is not available");
    }

    public Timer createTimer(Object id, long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        throw new IllegalStateException("Timer service is not available");
    }

    public Timer createTimer(Object id, long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        throw new IllegalStateException("Timer service is not available");
    }

    public Collection getTimers(Object id) throws IllegalStateException, EJBException {
        throw new IllegalStateException("Timer service is not available");
    }

    public Timer getTimerById(Long id) {
        throw new IllegalStateException("Timer service is not available");
    }
}
