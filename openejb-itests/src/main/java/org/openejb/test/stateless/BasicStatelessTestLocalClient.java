package org.openejb.test.stateless;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class BasicStatelessTestLocalClient extends StatelessTestClient {
    
    protected BasicStatelessLocalHome   ejbLocalHome;
    protected BasicStatelessLocalObject ejbLocalObject;

    public BasicStatelessTestLocalClient(String name){
        super(name);
    }
}

