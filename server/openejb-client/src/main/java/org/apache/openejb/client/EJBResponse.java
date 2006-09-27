package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EJBResponse implements Response {

    private transient int responseCode = -1;
    private transient Object result;

    public EJBResponse() {

    }

    public EJBResponse(int code, Object obj) {
        responseCode = code;
        result = obj;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResponse(int code, Object result) {
        this.responseCode = code;
        this.result = result;
    }

    public String toString() {
        StringBuffer s = null;
        switch (responseCode) {
            case EJB_APP_EXCEPTION:
                s = new StringBuffer("EJB_APP_EXCEPTION");
                break;
            case EJB_ERROR:
                s = new StringBuffer("EJB_ERROR");
                break;
            case EJB_OK:
                s = new StringBuffer("EJB_OK");
                break;
            case EJB_OK_CREATE:
                s = new StringBuffer("EJB_OK_CREATE");
                break;
            case EJB_OK_FOUND:
                s = new StringBuffer("EJB_OK_FOUND");
                break;
            case EJB_OK_FOUND_COLLECTION:
                s = new StringBuffer("EJB_OK_FOUND_COLLECTION");
                break;
            case EJB_OK_FOUND_ENUMERATION:
                s = new StringBuffer("EJB_OK_FOUND_ENUMERATION");
                break;
            case EJB_OK_NOT_FOUND:
                s = new StringBuffer("EJB_OK_NOT_FOUND");
                break;
            case EJB_SYS_EXCEPTION:
                s = new StringBuffer("EJB_SYS_EXCEPTION");
                break;
            default:
                s = new StringBuffer("UNKNOWN_RESPONSE");
        }
        s.append(':').append(result);

        return s.toString();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        responseCode = in.readByte();

        result = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeByte(responseCode);
        out.writeObject(result);
    }
}
