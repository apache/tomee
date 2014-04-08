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
package org.apache.openejb.log;

import java.util.logging.ConsoleHandler;
import org.apache.openejb.loader.SystemInstance;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.WindowsAnsiOutputStream;

import static org.apache.openejb.log.JULUtil.level;

public class ConsoleColorHandler extends ConsoleHandler {
    private static boolean wrapped;

    static {
        // mess output with maven on linux and not really mandatory in linux
        // TODO: not tested under windows, it needs to add jna
        wrapped = AnsiConsole.wrapOutputStream(System.out) instanceof WindowsAnsiOutputStream;
        if (wrapped && "true".equals(SystemInstance.get().getProperty("openejb.log.color.install", "true"))) {
            AnsiConsole.systemInstall();
        }
    }

    public ConsoleColorHandler() {
        setFormatter(new ColorFormatter());
        setLevel(level());
        if (wrapped) {
            setOutputStream(AnsiConsole.out);
        } else {
            setOutputStream(System.out);
        }
    }
}
