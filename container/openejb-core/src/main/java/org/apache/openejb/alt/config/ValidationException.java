package org.apache.openejb.alt.config;

import org.apache.openejb.util.Messages;

public class ValidationException extends java.lang.Exception {
    protected static final Messages messages = new Messages("org.apache.openejb.alt.config.rules");
    protected Bean bean;
    protected Object[] details;
    protected String message;
    protected String prefix;

    public ValidationException(String message) {
        this.message = message;
    }

    public void setDetails(Object arg1) {
        this.details = new Object[]{arg1};
    }

    public void setDetails(Object arg1, Object arg2) {
        this.details = new Object[]{arg1, arg2};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3) {
        this.details = new Object[]{arg1, arg2, arg3};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3, Object arg4) {
        this.details = new Object[]{arg1, arg2, arg3, arg4};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.details = new Object[]{arg1, arg2, arg3, arg4, arg5};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        this.details = new Object[]{arg1, arg2, arg3, arg4, arg5, arg6};
    }

    public Object[] getDetails() {
        return details;
    }

    public String getSummary() {
        return getMessage(1);
    }

    public String getMessage() {
        return getMessage(2);
    }

    public String getMessage(int level) {
        return messages.format(level + "." + message, details);
    }

    public Bean getBean() {
        return bean;
    }

    public void setBean(Bean bean) {
        this.bean = bean;
    }

    public String getPrefix() {
        return "";
    }

    public String getCategory() {
        return "";
    }
}
