package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class JNDIResponse implements Response {

    private transient int responseCode = -1;
    private transient Object result;

    public JNDIResponse() {
    }

    public JNDIResponse(int code, Object obj) {
        responseCode = code;
        result = obj;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        responseCode = in.readByte();

        switch (responseCode) {
            case JNDI_OK:
            case JNDI_NAMING_EXCEPTION:
            case JNDI_RUNTIME_EXCEPTION:
            case JNDI_ERROR:
                result = in.readObject();
                break;
            case JNDI_CONTEXT:
            case JNDI_NOT_FOUND:
                break;
            case JNDI_EJBHOME:
                EJBMetaDataImpl m = new EJBMetaDataImpl();
                m.readExternal(in);
                result = m;
                break;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeByte((byte) responseCode);

        switch (responseCode) {
            case JNDI_OK:
            case JNDI_NAMING_EXCEPTION:
            case JNDI_RUNTIME_EXCEPTION:
            case JNDI_ERROR:
                out.writeObject(result);
                break;
            case JNDI_CONTEXT:
            case JNDI_NOT_FOUND:
                break;
            case JNDI_EJBHOME:
                EJBMetaDataImpl m = (EJBMetaDataImpl) result;
                m.writeExternal(out);
                break;

        }
    }
}
