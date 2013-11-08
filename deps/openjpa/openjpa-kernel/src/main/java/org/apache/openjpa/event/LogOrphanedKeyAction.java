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
package org.apache.openjpa.event;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactoryImpl;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ValueMetaData;

/**
 * Log a message when an orphaned key is discovered.
 *
 * @author Abe White
 * @since 0.3.2.2
 */
public class LogOrphanedKeyAction
    implements OrphanedKeyAction {

    private static final Localizer _loc = Localizer.forPackage
        (LogOrphanedKeyAction.class);

    private String _channel = OpenJPAConfiguration.LOG_RUNTIME;
    private short _level = Log.WARN;

    /**
     * The channel to log to. Defaults to
     * <code>org.apache.openjpa.Runtime</code>.
     */
    public String getChannel() {
        return _channel;
    }

    /**
     * The channel to log to. Defaults to
     * <code>org.apache.openjpa.Runtime</code>.
     */
    public void setChannel(String channel) {
        _channel = channel;
    }

    /**
     * The level to log at. Defaults to <code>WARN</code>.
     */
    public short getLevel() {
        return _level;
    }

    /**
     * The level to log at. Defaults to <code>WARN</code>.
     */
    public void setLevel(short level) {
        _level = level;
    }

    /**
     * The level to log at. Defaults to <code>WARN</code>.
     */
    public void setLevel(String level) {
        _level = LogFactoryImpl.getLevel(level);
    }

    public Object orphan(Object oid, OpenJPAStateManager sm,
        ValueMetaData vmd) {
        Log log = vmd.getRepository().getConfiguration().getLog(_channel);
        Object owner = (sm == null) ? null : sm.getId();
        String msg = (owner == null) ? "orphaned-key" : "orphaned-key-owner";
        switch (_level) {
            case Log.TRACE:
                if (log.isTraceEnabled())
                    log.trace(_loc.get(msg, oid, vmd, owner));
                break;
            case Log.INFO:
                if (log.isInfoEnabled())
                    log.info(_loc.get(msg, oid, vmd, owner));
                break;
            case Log.WARN:
                if (log.isWarnEnabled())
                    log.warn(_loc.get(msg, oid, vmd, owner));
                break;
            case Log.ERROR:
                if (log.isErrorEnabled())
                    log.error(_loc.get(msg, oid, vmd, owner));
                break;
            case Log.FATAL:
                if (log.isFatalEnabled())
                    log.fatal(_loc.get(msg, oid, vmd, owner));
                break;
        }
        return null;
	}
}
