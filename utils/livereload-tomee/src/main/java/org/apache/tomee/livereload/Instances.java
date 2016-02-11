/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.livereload;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.openejb.util.LogCategory;

// Important note: take care to not create cycles with instances returned in getters.
public class Instances {
    private static final Instances INSTANCE = new Instances();

    public static Instances get() {
        return INSTANCE;
    }

    private Instances() {
        // no-op
    }

    private final LogCategory logCategory = LogCategory.OPENEJB.createChild("livereload");
    private final Mapper mapper = new MapperBuilder().build();
    private final FileWatcher watcher = new FileWatcher(logCategory, mapper);

    public FileWatcher getWatcher() {
        return watcher;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public LogCategory getLogCategory() {
        return logCategory;
    }
}
