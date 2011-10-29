package org.apache.openejb.arquillian.tests.listenerlocalinject;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;

public class PojoServletContextListener implements ServletContextListener {

    @Inject
    private Car car;

    @EJB
    private CompanyLocal localCompany;

    @EJB
    private SuperMarket market;

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

    /* TODO: Enable this resource after functionality is fixed
    @Resource
    */
    private Code defaultCode;

    /* TODO: Enable this resource after functionality is fixed
            @Resource
            @SuppressWarnings("unchecked")
    */
    private Class auditWriter;


    public void contextInitialized(ServletContextEvent event) {
        final String name = "OpenEJB";
        final ServletContext context = event.getServletContext();

        if (car != null) {
            context.setAttribute(ContextAttributeName.KEY_Car.name(), car.drive(name));
        }
        if (localCompany != null) {
            context.setAttribute(ContextAttributeName.KEY_LocalEjb.name(), "Local: " + localCompany.employ(name));
        }
        if (market != null) {
            context.setAttribute(ContextAttributeName.KEY_Market.name(), market.shop(name));
        }
        if (connectionPool != null) {
            context.setAttribute(ContextAttributeName.KEY_ConnPool.name(), "Connection Pool: " + connectionPool);
        }
        if (startCount != null) {
            context.setAttribute(ContextAttributeName.KEY_StartCount.name(), "Start Expressions.Count: " + startCount);
        }
        if (initSize != null) {
            context.setAttribute(ContextAttributeName.KEY_InitSize.name(), "Init Size: " + initSize);
        }
        if (totalQuantity != null) {
            context.setAttribute(ContextAttributeName.KEY_TotalQuantity.name(), "Total Quantity: " + totalQuantity);
        }
        if (enableEmail != null) {
            context.setAttribute(ContextAttributeName.KEY_EnableEmail.name(), "Enable Email: " + enableEmail);
        }
        if (optionDefault != null) {
            context.setAttribute(ContextAttributeName.KEY_DefaultOption.name(), "Option Default: " + optionDefault);
        }
        if (StringUtils.isNotEmpty(returnEmail) && returnEmail.equals("tomee@apache.org")) {
            context.setAttribute(ContextAttributeName.KEY_ReturnEmail.name(), returnEmail);
        }
        if (auditWriter != null) {
            context.setAttribute(ContextAttributeName.KEY_AuditWriter.name(), auditWriter.getClass().getName());
        }
        if (defaultCode != null) {
            context.setAttribute(ContextAttributeName.KEY_DefaultCode.name(), "DefaultCode: " + defaultCode);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

}