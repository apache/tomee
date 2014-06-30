package org.superbiz.mtom;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Response {

    @XmlMimeType("application/octet-stream")
    private DataHandler result;

    public Response() {
    }

    public Response(final DataHandler result) {
        this.result = result;
    }

    public DataHandler getResult() {
        return this.result;
    }

    public void setResult(final DataHandler result) {
        this.result = result;
    }
}
