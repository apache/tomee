package org.apache.openejb.test.entity.bmp;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class BasicBmpTestClient extends BmpTestClient{
    
    protected BasicBmpHome   ejbHome;
    protected BasicBmpObject ejbObject;

    public BasicBmpTestClient(String name){
        super(name);
    }
}

