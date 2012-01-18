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
package org.apache.openejb.server.ssh;

import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class SSHServerTest {
    private static EJBContainer container;

    @BeforeClass
    public static void start() {
        System.setProperty("openejb.server.ssh.key", "target/ssh-key");
        System.setProperty("openejb.logger.external", "true");
        container = EJBContainer.createEJBContainer(new HashMap<Object, Object>() {{
            put(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        }});
    }

    @AfterClass
    public static void close() {
        container.close();
        System.getProperties().remove("openejb.logger.external");
        System.getProperties().remove("openejb.server.ssh.key");
    }

    @Test
    public void call() {
        System.out.println("ok");
    }
}
