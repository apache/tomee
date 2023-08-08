package org.superbiz.rest.service;


import jakarta.ejb.*;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

@Singleton
@TransactionManagement(TransactionManagementType.CONTAINER)
public class Broadcaster {

    @EJB
    private Producer producer;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void broadcastMessage(final String message) {
        try {
            final InitialContext context = new InitialContext();
            final NamingEnumeration<NameClassPair> list = context.list("openejb:Resource");

            while (list.hasMoreElements()) {
                final NameClassPair nameClassPair = list.nextElement();
                final String name = nameClassPair.getName();
                if (name.endsWith("ConnectionFactory")) {
                    producer.sendMessage(message, name.substring(0, name.length() - 17));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





}
