/*
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
package org.apache.tomee.installer;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlertsTest {
    @Test
    public void errorWithExceptionIsRecorded() {
        final Alerts alerts = new Alerts();
        alerts.addError("Unable to copy jar", new IOException("disk full"));

        assertTrue(alerts.hasErrors());
        assertEquals(1, alerts.getErrors().size());
        assertTrue(alerts.getErrors().get(0).startsWith("Unable to copy jar"));
        assertTrue(alerts.getErrors().get(0).contains("disk full"));
    }

    @Test
    public void errorWithoutException() {
        final Alerts alerts = new Alerts();
        alerts.addError("Unable to copy jar", null);

        assertEquals(1, alerts.getErrors().size());
        assertEquals("Unable to copy jar", alerts.getErrors().get(0));
    }
}
