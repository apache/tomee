package org.apache.openejb.alt.config;

public interface Service {
    public java.lang.String getContent();

    public java.lang.String getId();

    public java.lang.String getJar();

    public java.lang.String getProvider();

    public void setContent(java.lang.String content);

    public void setId(java.lang.String id);

    public void setJar(java.lang.String jar);

    public void setProvider(java.lang.String provider);

    public void validate() throws org.exolab.castor.xml.ValidationException;
}