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
package org.apache.openjpa.persistence.common.utils;

import java.io.*;

import junit.framework.*;

/**
 * Extension of an assertion error that indicates to the outside
 * build process (if any) that an assertion failed due to a known
 * bug.
 *
 * @author Marc Prud'hommeaux
 */
public class Bug
    extends AssertionFailedError {

    public static final String BUG_TOKEN = "SOLARBUG";
    public static final String BUG_DELIMITER = "|";

    private Throwable error = null;
    private int trackingId;

    // the the static factory method, please
    private Bug(int trackingId, Throwable t, String message) {
        super(BUG_DELIMITER + BUG_TOKEN + BUG_DELIMITER
            + trackingId + BUG_DELIMITER + message);
        this.trackingId = trackingId;
        error = t;

        printStackTrace(System.err);
    }

    public static Bug bug(int trackingId, Throwable t, String message) {
        return new Bug(trackingId, t, message);
    }

    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (error != null) {
            pw.println("Embedded error message:");
            error.printStackTrace(pw);
        }
    }

    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (error != null) {
            ps.println("Embedded error message:");
            error.printStackTrace(ps);
        }
    }

    public String getMessage() {
        return super.getMessage() + " [reported bug #" + trackingId + "]";
    }

    public int getTrackingId() {
        return trackingId;
    }
}


