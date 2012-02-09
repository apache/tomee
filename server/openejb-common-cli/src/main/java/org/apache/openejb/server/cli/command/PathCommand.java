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

import org.apache.openejb.loader.SystemInstance;

import java.io.File;

public abstract class PathCommand extends AbstractCommand {
    public static final String HOME = "$home";
    public static final String BASE = "$base";

    protected File resolve(final String prefix, final String cmd) {
        if (cmd == null || (prefix != null && cmd.trim().equals(prefix)) || cmd.trim().isEmpty()) {
            return SystemInstance.get().getBase().getDirectory();
        }
        int idx = 0;
        if (prefix != null) {
            idx = prefix.length() + 1;
        }
        String path = cmd.substring(idx);
        File workingFile = new File(path);
        if ((!path.startsWith(HOME) && !path.startsWith(BASE) && workingFile.getPath().equals(workingFile.getAbsolutePath())) || path.startsWith("..")) {
            throw new IllegalArgumentException("path should start with " + BASE + " or " + HOME + " or be relative");
        }

        if (path.startsWith(HOME)) {
            return new File(path.replace(HOME, SystemInstance.get().getHome().getDirectory().getAbsolutePath()));
        } else  if(path.startsWith(BASE)) {
            return new File(path.replace(BASE, SystemInstance.get().getBase().getDirectory().getAbsolutePath()));
        }
        return new File(SystemInstance.get().getBase().getDirectory().getAbsolutePath(), path);
    }
}
