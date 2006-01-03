package org.openejb.test.stateful;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class BasicStatefulTestClient extends StatefulTestClient{
    
    protected BasicStatefulHome   ejbHome;
    protected BasicStatefulObject ejbObject;

    public BasicStatefulTestClient(String name){
        super(name);
    }
}

