package org.apache.openejb.test;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ApplicationException extends Exception{

    public ApplicationException(String message){
        super(message);
    }

    public ApplicationException(){
        super();
    }
}
