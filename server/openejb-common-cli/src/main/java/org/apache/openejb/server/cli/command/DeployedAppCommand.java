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
package org.apache.openejb.server.cli.command;

import org.apache.openejb.AppContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Command(name = "apps", usage = "apps", description = "list deployed applications")
public class DeployedAppCommand extends PathCommand {
    @Override
    public void execute(final String cmd) {
        streamManager.writeOut("Deployed applications:");
        final List<AppContext> apps = new ArrayList<AppContext>(SystemInstance.get()
                                            .getComponent(ContainerSystem.class).getAppContexts());
        Collections.sort(apps, new AppContextComparable());
        for (AppContext appContext : apps) {
            streamManager.writeOut("  - " + appContext.getId());
        }
    }

    private class AppContextComparable implements Comparator<AppContext> {
        @Override
        public int compare(final AppContext o1, final AppContext o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }
}
