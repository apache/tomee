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

package org.apache.openejb.core.timer;

import org.apache.openejb.OpenEJBException;

import javax.ejb.Timer;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public interface EjbTimerService {
    Timer getTimer(long id);

    Collection<Timer> getTimers(Object primaryKey);

    Timer createTimer(Object primaryKey, Date initialExpiration, long intervalDuration, Serializable info);

    Timer createTimer(Object primaryKey, Date expiration, Serializable info);

    Timer createTimer(Object primaryKey, long initialDuration, long intervalDuration, Serializable info);

    Timer createTimer(Object primaryKey, long duration, Serializable info);

    void start() throws OpenEJBException;

    void stop();
}
