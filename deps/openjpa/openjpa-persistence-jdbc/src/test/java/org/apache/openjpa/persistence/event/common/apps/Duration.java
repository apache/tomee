/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.event.common.apps;

import javax.persistence.Entity;

@Entity

/*

    Millisecond (only) accuracy timer.

    Java 1.4 supposedly has sun.misc.Perf.

    Java 1.5 has System.nanoTime (JSR 166)

*/

public class Duration

    implements Cloneable {

    private String _name;

    private boolean _started;

    private boolean _running;

    private long _startTime;        // millis

    private long _stopTime;        // millis

    // NYI clock time of day at start

    public Duration(String name) {

        _name = name;

        _started = false;

        _running = false;
    }

    public String getName() {

        return _name;
    }

    public synchronized void start() {

        if (_started) {

            throw new RuntimeException("Duration was already started.");
        }

        _startTime = System.currentTimeMillis();

        _started = true;

        _running = true;
    }

    public synchronized void stop() {

        if (!_started) {

            throw new RuntimeException("Duration was never started.");
        }

        if (!_running) {

            throw new RuntimeException("Duration was already stopped.");
        }

        _stopTime = System.currentTimeMillis();

        _running = false;
    }

    protected Object clone()

        throws CloneNotSupportedException {

        return super.clone();
    }

    /*

        Returns a new Duration object from a currently running timer

        as a snapshot of this object.

        The returned timer is stopped, while this object continue on.

    */

    public synchronized Duration getCurrentDuration() {

        if (!_started) {

            throw new RuntimeException("Duration was never started.");
        }

        if (!_running) {

            throw new RuntimeException("Duration is not running.");
        }

        long now = System.currentTimeMillis();

        Duration currentDuration;

        try {

            currentDuration = (Duration) this.clone();
        } catch (Exception e) {

            currentDuration = new Duration("");
        }

        currentDuration._stopTime = now;

        currentDuration._running = false;

        return currentDuration;
    }

    /* Obtain the duration that this timer has run (in seconds)	*/

    public synchronized double getDurationAsSeconds() {

        if (!_started) {

            throw new RuntimeException("Duration was never started.");
        }

        if (_running) {

            // snapshot

            Duration snapshot = getCurrentDuration();

            return (1000.0 * (snapshot._stopTime - snapshot._startTime));
        }

        // Return a double value. Someday this class may make use of

        // higher precision timing services (e.g. java 1.5)

        return ((_stopTime - _startTime) / (double) 1000.0);
    }

    public synchronized boolean isRunning() {

        return _running;
    }

    public synchronized boolean wasStarted() {

        return _started;
    }

    public String toString() {

        double time = 0.0;

        StringBuffer buf = new StringBuffer(256);

        if (wasStarted()) {

            if (isRunning()) {

                Duration snapshot = getCurrentDuration();

                time = snapshot.getDurationAsSeconds();
            } else {

                time = getDurationAsSeconds();
            }

            buf.append("Duration for '" + _name + "' is " + time + " (s).");
        } else {

            buf.append("Duration for '" + _name +

                "' has not yet been started.");
        }

        return buf.toString();
    }

/* Example usage:

       public static void main (String[] args)

        throws Exception

    {

        Duration test = new Duration ("hello, count to 1 million");

        System.out.println (test);

        test.start ();

        for (int i = 0; i < 1000000000; i++)

            {

            }

        test.stop ();

        System.out.println (test);

    }

    */
}

