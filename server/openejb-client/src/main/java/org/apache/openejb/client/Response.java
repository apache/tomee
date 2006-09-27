package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public interface Response extends Externalizable, ResponseCodes {

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;

    public void writeExternal(ObjectOutput out) throws IOException;
}

