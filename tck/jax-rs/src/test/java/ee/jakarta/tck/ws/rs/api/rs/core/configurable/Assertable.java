/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.api.rs.core.configurable;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;

public abstract class Assertable {
    final static String[] LOCATION = { "Client", "WebTarget", "Invocation.Builder", "Invocation" };

    private int locationIndex = 0;

    public abstract void check1OnClient(Client client) throws Fault;

    public abstract void check2OnTarget(WebTarget target) throws Fault;

    public void incrementLocation() {
        locationIndex = (locationIndex + 1 == LOCATION.length) ? 0 : locationIndex + 1;
    }

    public String getLocation() {
        return new StringBuilder().append("on ").append(LOCATION[locationIndex]).append(" configuration").toString();
    }

    public static String getLocation(int index) {
        return LOCATION[index];
    }

    public int getLocationIndex() {
        return locationIndex;
    }

}
