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
package org.apache.openjpa.persistence.meta;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Simple logger sets log level from javac compilers annotation processing 
 * options <code>-Alog=TRACE|INFO|WARN|ERROR</code> and uses the processing
 * environment to determine the log output stream.
 * 
 * @author Pinaki Poddar
 *
 */
public class CompileTimeLogger {
    private static enum Level {TRACE, INFO, WARN, ERROR};
    private static Localizer _loc = Localizer.forPackage(CompileTimeLogger.class);
    private static Level DEFAULT_LEVEL = Level.WARN;
    private int logLevel;
    private Messager messager;
    
    public CompileTimeLogger(ProcessingEnvironment env, String level) {
        messager = env.getMessager();
        
        if (level == null) {
            logLevel = DEFAULT_LEVEL.ordinal();
            return;
        }
        if ("trace".equalsIgnoreCase(level))
            logLevel = Level.TRACE.ordinal();
        else if ("info".equalsIgnoreCase(level))
            logLevel = Level.INFO.ordinal();
        else if ("warn".equalsIgnoreCase(level))
            logLevel = Level.WARN.ordinal();
        else if ("error".equalsIgnoreCase(level))
            logLevel = Level.ERROR.ordinal();
        else {
            logLevel = DEFAULT_LEVEL.ordinal();
            warn(_loc.get("mmg-bad-log", level, DEFAULT_LEVEL));
        }
    }
    
    public void info(Localizer.Message message) {
        log(Level.INFO, message, Diagnostic.Kind.NOTE);
    }
    
    public void trace(Localizer.Message message) {
        log(Level.TRACE, message, Diagnostic.Kind.NOTE);
    }
    
    public void warn(Localizer.Message message) {
        log(Level.WARN, message, Diagnostic.Kind.MANDATORY_WARNING);
    }
    
    public void error(Localizer.Message message) {
        error(message, null);
    }
    
    public void error(Localizer.Message message, Throwable t) {
        log(Level.ERROR, message, Diagnostic.Kind.ERROR);
        if (t != null)
            t.printStackTrace();
    }
    
    private void log(Level level, Localizer.Message message, 
        Diagnostic.Kind kind) {
        if (logLevel <= level.ordinal()) {
            messager.printMessage(kind, message.toString());
        }
    }
}
