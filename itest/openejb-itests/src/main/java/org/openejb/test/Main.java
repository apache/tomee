package org.openejb.test;

import java.io.File;

import org.openejb.loader.SystemInstance;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class Main{

    public static void main (String args[]) {
        try{
            File directory = SystemInstance.get().getHome().getDirectory("lib");
            SystemInstance.get().getClassPath().addJarsToPath(directory);
            File directory1 = SystemInstance.get().getHome().getDirectory("dist");
            SystemInstance.get().getClassPath().addJarsToPath(directory1);
        } catch (Exception e){
            e.printStackTrace();
        }

        TestRunner.main( args );
    }

}
