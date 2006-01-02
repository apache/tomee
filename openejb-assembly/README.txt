===============================================
OpenEJB -- EJB Container System and EJB Server

OpenEJB v1.0-beta1      http://www.openejb.org/
-----------------------------------------------
___________________
Installation
===================
 
 If you are reading this, you've already unpacked the OpenEJB release.  
 In that case, congratulations, you've installed OpenEJB.  
 
 If you've unpacked OpenEJB into the directory C:\openejb, for example.
 Than this directory is your OPENEJB_HOME directory.  The OPENEJB_HOME
 directory is refered to in various parts of the documentation, so it's
 good to remeber where it is.

 Add OPENEJB_HOME/bin directory to your PATH environment variable:

   set PATH=%OPENEJB_HOME%/bin;%PATH%

 or

   export PATH=$OPENEJB_HOME/bin:$PATH

___________________
Using OpenEJB
===================
 
 Now all you need to do is to type:
 
   openejb help
 
 For Windows users, that looks like this:
  
   C:\openejb> openejb help
 
 For UNIX/Linux/Mac OS X users, that looks like this:
 
   [user@host openejb]# openejb help
 
 You really only need to know two commands to use OpenEJB, deploy
 and start.  Both are completely documented and have examples.
 
 For help information and command options, try this:
 
   openejb deploy -help
   openejb start -help
 
 For examples on using the command and options, try this:
 
   openejb deploy -examples
   openejb start -examples
 
 That's it!

___________________
Documentation
===================
 
 The initial versions of the documentation for this release 
 are in the directory docs/html,  see file docs/html/index.html

___________________
Support
===================
 
 Any problems with this release can be reported to our user
 mailing list.

 Send email to subscribe-user@openejb.org or follow this link
 for other subscription and list information:
 http://www.openejb.org/Mailing+Lists#user
