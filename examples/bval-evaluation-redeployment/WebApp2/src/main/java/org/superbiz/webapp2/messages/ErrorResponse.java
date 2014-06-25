/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.superbiz.webapp2.messages;

import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = 8888101217538645771L;

    private Long id;
    private Response.Status status;
    private String message;

    public ErrorResponse() {
        this.id = new Date().getTime();
    }

    public ErrorResponse(final Response.Status status, final String message) {
        this.id = new Date().getTime();
        this.status = status;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Response.Status getStatus() {
        return status;
    }

    @XmlAttribute
    public void setStatus(final Response.Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    @XmlAttribute
    public void setMessage(final String message) {
        this.message = message;
    }

    //    @Override
//    public String toString() {
//        return "{" + "id:" + id + ", status:" + status + ", message:" + message + '}';
//    }
    @Override
    public String toString() {
        return "ErrorResponse:" + "id=" + id + ", status=" + status + ", message=" + message;
    }
}
