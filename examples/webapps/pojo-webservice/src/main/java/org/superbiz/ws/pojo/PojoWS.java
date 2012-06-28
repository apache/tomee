package org.superbiz.ws.pojo;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.transaction.UserTransaction;

@WebService
public class PojoWS implements WS {
    @Resource
    private WebServiceContext webServiceContext;

    @Resource
    private UserTransaction userTransaction;

    @Override
    public String ws() {
        return webServiceContext + " & " + userTransaction;
    }

    public void setWebServiceContext(WebServiceContext webServiceContext) {
        this.webServiceContext = webServiceContext;
    }

    public void setUserTransaction(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }
}
