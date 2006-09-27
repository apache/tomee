package org.apache.openejb.test.object;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.transaction.UserTransaction;
/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class Transaction implements java.io.Externalizable{
    
    private String instance;

    public Transaction(UserTransaction obj){
        instance = obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
    }

    public Transaction(){
    }

    public boolean equals(Object object){
        if ( !(object instanceof Transaction ) ) return false;

        Transaction that = (Transaction)object;
        return this.instance.equals(that.instance);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(instance);
    }
    
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        instance = in.readUTF();
    }

    public String toString(){
        return instance;
    }
}
