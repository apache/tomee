package org.apache.openejb.core.ivm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import org.apache.openejb.util.FastThreadLocal;

public class IntraVmArtifact implements Externalizable {

    private int instanceHandle;

    private static FastThreadLocal thread = new FastThreadLocal();

    private static final String NO_MAP_ERROR = "There is no HashMap stored in the thread.  This object may have been moved outside the Virtual Machine.";

    private static final String NO_ARTIFACT_ERROR = "The artifact this object represents could not be found.";

    public IntraVmArtifact(Object obj) {

        List list = (List) thread.get();
        if (list == null) {
            list = new ArrayList();
            thread.set(list);
        }
        instanceHandle = list.size();
        list.add(obj);
    }

    public IntraVmArtifact() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(instanceHandle);
    }

    public void readExternal(ObjectInput in) throws IOException {
        instanceHandle = in.read();
    }

    private Object readResolve() throws ObjectStreamException {
        List list = (List) thread.get();
        if (list == null) throw new InvalidObjectException(NO_MAP_ERROR);
        Object artifact = list.get(instanceHandle);
        if (artifact == null) throw new InvalidObjectException(NO_ARTIFACT_ERROR + instanceHandle);
        if (list.size() == instanceHandle + 1) {
            list.clear();
        }
        return artifact;
    }

}
