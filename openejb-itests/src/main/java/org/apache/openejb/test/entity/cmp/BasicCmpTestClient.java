package org.apache.openejb.test.entity.cmp;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class BasicCmpTestClient extends CmpTestClient{
    
    protected BasicCmpHome   ejbHome;
    protected BasicCmpObject ejbObject;

    public BasicCmpTestClient(String name){
        super(name);
    }
}

