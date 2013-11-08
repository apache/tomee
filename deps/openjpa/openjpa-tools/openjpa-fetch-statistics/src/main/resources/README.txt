Apache OpenJPA - README.txt
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

Open JPA Fetch Statistics Tool monitors persistent field access and determines which fields are never used. This tool
can be used to help tune an application. 

Note: Open JPA Fetching Statistics Tool works with the runtime enhancement.

JSE usage instructions: 

1.] Configuration
  * Append the path of openjpa-fetch-statistics-[version].jar file to the classpath prior to lanuching the JVM.
    
2.] Statistics Collecting and Monitoring     
  * When this tool is configured, it will be active for all persistence units in the JVM. Statistics will be dumped via the 
  openjpa.Runtime channel with the INFO level every 10 minutes, or when the JVM terminates. Any field that is logged 
  has not been accessed by an application.
    
3.] Configuration removal
  * Stop the JVM.
  * Remove openjpa-fetch-statistics-[version].jar from the classpath.
    
Performance Consideration

There will be a large performance impact when running this tooling. It is not supported, nor recommended for production
use. This tool should not be used on a production machine.

