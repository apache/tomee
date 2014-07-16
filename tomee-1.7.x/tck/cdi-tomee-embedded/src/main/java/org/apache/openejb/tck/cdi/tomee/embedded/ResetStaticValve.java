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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.tck.cdi.tomee.embedded;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.testharness.AbstractTest;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class ResetStaticValve extends ValveBase {


    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            AbstractTest.setInContainer(true);
            getNext().invoke(request, response);
        } finally {
            AbstractTest.setInContainer(false);
        }

    }
}
