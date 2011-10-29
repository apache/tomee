package org.apache.openejb.arquillian.tests.sharedenv;

import javax.annotation.Resource;
import javax.inject.Inject;


public class Green implements Environment {

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

    @Inject
    public Green() {
    }

    public Green(String returnEmail, Integer connectionPool, Long startCount, Short initSize, Byte totalQuantity, Boolean enableEmail, Character optionDefault) {
        this.returnEmail = returnEmail;
        this.connectionPool = connectionPool;
        this.startCount = startCount;
        this.initSize = initSize;
        this.totalQuantity = totalQuantity;
        this.enableEmail = enableEmail;
        this.optionDefault = optionDefault;
    }

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