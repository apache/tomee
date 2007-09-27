======================================================
Apache OpenEJB -- EJB Container System and EJB Server

Apache OpenEJB ${pom.version}      http://openejb.apache.org/
------------------------------------------------------
___________________
Installation
===================
 
 If you are reading this, you've already unpacked the Apache OpenEJB release.  
 In that case, congratulations, you've installed OpenEJB.  
 
 If you've unpacked OpenEJB into the directory C:\openejb, for example.
 Than this directory is your OPENEJB_HOME directory.  The OPENEJB_HOME
 directory is refered to in various parts of the documentation, so it's
 good to remeber where it is.

 Add OPENEJB_HOME/bin directory to your PATH environment variable:

   set PATH=%OPENEJB_HOME%/bin;%PATH%

 or

   export PATH=$OPENEJB_HOME/bin:$PATH

__________________________
Using Apache OpenEJB
==========================
 
 Now all you need to do is to type:
 
   openejb --help
 
 For Windows users, that looks like this:
  
   C:\openejb> openejb --help
 
 For UNIX/Linux/Mac OS X users, that looks like this:
 
   [user@host openejb]# openejb --help
 
 You really only need to know two commands to use OpenEJB, deploy
 and start.  Both are completely documented and have examples.
 
 For help information and command options, try this:
 
   openejb deploy --help
   openejb start --help
 
 
 That's it!

___________________
Support
===================
 
 Any problems with this release can be reported to our user
 mailing list at users@openejb.apache.org 
 Follow this link for other subscription and list information:
 http://openejb.apache.org/mailing-lists.html


=========================================================================
==  Cryptographic Software Notice                                      ==
=========================================================================

This distribution includes cryptographic software.  The country in
which you currently reside may have restrictions on the import,
possession, use, and/or re-export to another country, of
encryption software.  BEFORE using any encryption software, please
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included cryptographic
software:
  Apache ActiveMQ  Included Binary
  Apache Derby     Included Binary
  Apache Geronimo  Included Binary
  Apache Tomcat    Specially Designed to Work With

