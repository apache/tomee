Apache OpenJPA - README.txt
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

WebSphere Instructions -- 

Open JPA Fetch Statistics Tool monitors persistent field access and determines which fields are never used. This tool
can be used to help tune an application. 

Note: Open JPA Fetching Statistics Tool works with the runtime enhancement.

Usage instructions: 

1.] Configuration
  * Add openjpa-fetch-statistics-[VERSION]-websphere.jar into the [WAS_HOME]\plugins directory.
  * Run [WAS_HOME]\bin\osgiCfgInit.sh(bat) to clear the osgi cache.
    
2.] Statistics Collecting and Monitoring     
  * When this tool is configured, it will be active for all persistence units in the JVM. Statistics will be dumped via the 
  openjpa.Runtime channel with the INFO level every 10 minutes, or when the JVM terminates. Any field that is logged 
  has not been accessed by an application.
    
3.] Configuration removal
  * Stop all WebSphere processes using the [WAS_HOME]\plugins installation.
  * Remove the openjpa-fetch-statistics-[VERSION]-websphere.jar jar from the [WAS_HOME]\plugins directory.
  * Run [WAS_HOME]\bin\osgiCfgInit.sh(bat) to clear the osgi cache.
    
Performance Consideration

There will be a large performance impact when running this tooling. It is not supported, nor recommended for production
use. This tool should not be used on a production machine.
