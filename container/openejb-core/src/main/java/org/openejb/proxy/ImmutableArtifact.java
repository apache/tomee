/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.proxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import org.openejb.util.FastThreadLocal;

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
