package org.openejb.naming;

import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

public class ConnectionFactoryReference implements Reference {

    private final ConnectionManager connectionManager;
    private final ManagedConnectionFactory connectionFactory;
    
    public ConnectionFactoryReference(ConnectionManager cm, ManagedConnectionFactory mcf){
        connectionManager = cm;
        connectionFactory = mcf;
    }
    
    public Object getObject() throws NamingException{
        try{
            return connectionFactory.createConnectionFactory(connectionManager);
        }catch(ResourceException re){
            throw new NamingException("Could not create ConnectionFactory from "+connectionFactory.getClass());
        }
        
    }
}