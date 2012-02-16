package org.apache.tomee.loader.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jndi")
public class JndiDTO {

    public String module;
    public String name;
    public String value;
}
