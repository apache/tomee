package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class JNDIRequest implements Request {

    private transient int requestMethod = -1;
    private transient String requestString;

    public JNDIRequest() {
    }

    public JNDIRequest(int requestMethod, String requestString) {
        this.requestMethod = requestMethod;
        this.requestString = requestString;
    }

    public byte getRequestType() {
        return JNDI_REQUEST;
    }

    public int getRequestMethod() {
        return requestMethod;
    }

    public String getRequestString() {
        return requestString;
    }

    public void setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        requestMethod = in.readByte();
        requestString = in.readUTF();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte((byte) requestMethod);
        out.writeUTF(requestString);
    }

}

