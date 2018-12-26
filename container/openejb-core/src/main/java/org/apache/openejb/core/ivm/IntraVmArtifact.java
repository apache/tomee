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

package org.apache.openejb.core.ivm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

public class IntraVmArtifact implements Externalizable {

    private static final Handles staticHandles = new Handles() {
        @Override
        public synchronized int add(final Object obj) {
            return super.add(obj);
        }

        @Override
        public synchronized Object get(final int id) {
            return super.get(id);
        }
    };

    private static final ThreadLocal<Handles> threadHandles = new ThreadLocal<Handles>() {
        protected Handles initialValue() {
            return new Handles();
        }
    };

    // todo why not put in message catalog?
    private static final String NO_ARTIFACT_ERROR = "The artifact this object represents could not be found.";

    private int instanceHandle;
    private boolean staticArtifact;

    public IntraVmArtifact(final Object obj) {
        this(obj, false);
    }

    public IntraVmArtifact(final Object obj, final boolean storeStatically) {
        this.staticArtifact = storeStatically;
        final Handles handles = getHandles(storeStatically);
        instanceHandle = handles.add(obj);
    }

    private static Handles getHandles(final boolean staticArtifact) {
        return staticArtifact ? staticHandles : threadHandles.get();
    }

    public IntraVmArtifact() {
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeBoolean(staticArtifact);
        out.writeInt(instanceHandle);
    }

    public void readExternal(final ObjectInput in) throws IOException {
        staticArtifact = in.readBoolean();
        instanceHandle = in.readInt();
    }

    protected Object readResolve() throws ObjectStreamException {
        final Handles handles = getHandles(staticArtifact);
        final Object artifact = handles.get(instanceHandle);
        if (artifact == null) {
            throw new InvalidObjectException(NO_ARTIFACT_ERROR + instanceHandle);
        }
        return artifact;
    }

    private static class Handles {
        private final List<Object> list = new ArrayList<>();

        public int add(final Object obj) {
            final int id = list.size();
            list.add(obj);
            return id;
        }

        public Object get(final int id) {
            final Object obj = list.get(id);
            // todo WHY?
            if (list.size() == id + 1) {
                list.clear();
            }
            return obj;
        }
    }
}
