package org.apache.openejb.core.ivm;

import java.io.ObjectStreamException;

public interface IntraVmProxy extends java.io.Serializable {

    public Object writeReplace() throws ObjectStreamException;

}
