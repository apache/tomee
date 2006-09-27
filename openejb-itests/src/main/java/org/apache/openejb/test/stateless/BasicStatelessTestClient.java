package org.apache.openejb.test.stateless;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class BasicStatelessTestClient extends StatelessTestClient{
    
    protected BasicStatelessHome   ejbHome;
    protected BasicStatelessObject ejbObject;

    public BasicStatelessTestClient(String name){
        super(name);
    }
}

