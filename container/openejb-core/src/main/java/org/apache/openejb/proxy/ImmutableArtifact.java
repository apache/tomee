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
package org.apache.openejb.proxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import org.apache.openejb.util.FastThreadLocal;

public class ImmutableArtifact implements Externalizable {

    /**
     * A handle created using information about the object
     * instance for which this IntraVMArtifact was created.
     */
    private int instanceHandle;
    
    /**
     * Holds a list of threads.  Each thread gets a HashMap to store 
     * instances artifacts of the intra-vm.  The instances are not serialized,
     * instead, a key for the object is serialized to the stream.
     * 
     * At deserialization, the key is used to get the original object
     * instance from the List
     */
    private static FastThreadLocal thread = new FastThreadLocal();
    
    /**
     * Error detailing that the List for this Thread can not be found.
     */
    // TODO: move text to Message.properties
    private static final String NO_MAP_ERROR = "There is no HashMap stored in the thread.  This object may have been moved outside the Virtual Machine.";
    
    /**
     * Error detailing that the object instance can not be found in the thread's List.
     */
    // TODO: move text to Message.properties
    private static final String NO_ARTIFACT_ERROR = "The artifact this object represents could not be found.";

    /**
     * Used to creat an ImmutableArtifact object that can represent
     * the true intra-vm artifact in a stream.
     * 
     * @param obj    The object instance this class should represent in the stream.
     */
    public ImmutableArtifact(Object obj) {
        // the prev implementation used a hash map and removed the handle in the readResolve method
        // which would prevent marshaling of objects with the same hashcode in one request.
        List list = (List)thread.get();
        if (list == null) {
            list = new ArrayList();
            thread.set(list);
        }
        instanceHandle = list.size();
        list.add(obj);
            }

    /**
     * This class is Externalizable and this public, no-arg, constructor is required.
     * 
     * This constructor should only be used by the deserializing stream.
     */
    public ImmutableArtifact() {
    }

    /**
     * Writes the instanceHandle to the stream.
     * 
     * @param out
     * @exception IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException{
        out.write(instanceHandle);                                            
    }

    /**
     * Reads the instanceHandle from the stream
     * 
     * @param in
     * @exception IOException
     */
    public void readExternal(ObjectInput in) throws IOException{
        instanceHandle = in.read();
    }

    /**
     * During deserialization, it is this object that is deserialized.
     * This class implements the readResolve method of the serialization API.
     * In the readResolve method, the original object instance is retrieved
     * from the List and returned instead.
     * 
     * @return 
     * @exception ObjectStreamException
     */
    private Object readResolve() throws ObjectStreamException{
        List list = (List) thread.get();
        if (list == null) throw new InvalidObjectException(NO_MAP_ERROR);
        Object artifact = list.get(instanceHandle);
        if (artifact == null) throw new InvalidObjectException(NO_ARTIFACT_ERROR+instanceHandle);
        if(list.size()==instanceHandle+1) {
            list.clear();
        }
        return artifact;
    }

}
