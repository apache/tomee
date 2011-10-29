package org.apache.openejb.arquillian.tests.sharedenv;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;


@Singleton
@LocalBean
public class Orange implements Environment {

    @Resource(name = "returnEmail")
    private String returnEmail;

    @Resource(name = "connectionPool")
    private Integer connectionPool;

    @Resource(name = "startCount")
    private Long startCount;

    @Resource(name = "initSize")
    private Short initSize;

    @Resource(name = "totalQuantity")
    private Byte totalQuantity;

    @Resource(name = "enableEmail")
    private Boolean enableEmail;

    @Resource(name = "optionDefault")
    private Character optionDefault;

    @Override
    public String getReturnEmail() {
        return returnEmail;
    }

    @Override
    public Integer getConnectionPool() {
        return connectionPool;
    }

    @Override
    public Long getStartCount() {
        return startCount;
    }

    @Override
    public Short getInitSize() {
        return initSize;
    }

    @Override
    public Byte getTotalQuantity() {
        return totalQuantity;
    }

    @Override
    public Boolean getEnableEmail() {
        return enableEmail;
    }

    @Override
    public Character getOptionDefault() {
        return optionDefault;
    }
}