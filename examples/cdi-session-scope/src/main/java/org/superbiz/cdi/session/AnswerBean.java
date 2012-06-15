package org.superbiz.cdi.session;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

// just to take care to refresh it, otherwise using postcontruct you can see kind of cache effect on session bean
@RequestScoped
public class AnswerBean {
    @Inject
    private SessionBean bean;

    private String value;

    @PostConstruct
    public void init() {
        value = '{' + bean.getName() + '}';
    }

    public String value() {
        return value;
    }
}
