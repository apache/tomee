package org.apache.openejb.test.object;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ObjectGraph implements Serializable{
    
    private Serializable object;

    public ObjectGraph(Object obj){
        this.object = (Serializable)obj;
    }

    public ObjectGraph(){
    }

    public void setObject(Object obj){
        this.object = (Serializable)obj;
    }

    public Object getObject(){
        return object;
    }

    public String toString(){
        return ((Object)object).toString();
    }
}

