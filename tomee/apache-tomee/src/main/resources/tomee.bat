@echo off
rem
rem Tomitribe Confidential
rem
rem Copyright Tomitribe Corporation. 2015
rem
rem The source code for this program is not published or otherwise divested
rem of its trade secrets, irrespective of what has been deposited with the
rem U.S. Copyright Office.
rem


setlocal

set port=8080

rem Guess CATALINA_HOME if not defined
set "CURRENT_DIR=%cd%"
if DEFINED CATALINA_HOME goto gotHome
set "CATALINA_HOME=%CURRENT_DIR%"
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
cd ..
set "CATALINA_HOME=%cd%"
cd "%CURRENT_DIR%"
:gotHome

if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
echo The CATALINA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Copy CATALINA_BASE from CATALINA_HOME if not defined
if DEFINED CATALINA_BASE goto gotBase
set "CATALINA_BASE=%CATALINA_HOME%"
:gotBase

rem Ensure that any user defined CLASSPATH variables are not used on startup,
rem but allow them to be specified in setenv.bat, in rare case when it is needed.
set CLASSPATH=

if not exist "%CATALINA_BASE%\bin\tomcat-juli.jar" goto juliClasspathHome
set "CLASSPATH=%CLASSPATH%;%CATALINA_BASE%\bin\tomcat-juli.jar"
goto juliClasspathDone
:juliClasspathHome
set "CLASSPATH=%CLASSPATH%;%CATALINA_HOME%\bin\tomcat-juli.jar"
:juliClasspathDone

rem Get standard Java environment variables
if exist "%CATALINA_HOME%\bin\setclasspath.bat" goto okSetclasspath
echo Cannot find "%CATALINA_HOME%\bin\setclasspath.bat"
echo This file is needed to run this program
goto end
:okSetclasspath
call "%CATALINA_HOME%\bin\setclasspath.bat" %1
if errorlevel 1 goto end

if DEFINED CATALINA_TMPDIR goto gotTmpdir
set "CATALINA_TMPDIR=%CATALINA_BASE%\temp"
:gotTmpdir

if not exist %CATALINA_BASE% goto :libClasspathHome
set CLASSPATH=%CLASSPATH%;%CATALINA_BASE%\lib\*
if "%CATALINA_BASE%" equ %CATALINA_HOME% goto :libClasspathDone
:libClasspathHome
if not exist %CATALINA_HOME% goto :libClasspathDone
set CLASSPATH=%CLASSPATH%;%CATALINA_HOME%\lib\*
:libClasspathDone

set DEBUG=
set "args=%*"
if ""%1"" == ""deploy"" goto doDeploy
if ""%1"" == ""undeploy"" goto doUndeploy
if ""%1"" == ""start"" goto unsupportedCmd
if ""%1"" == ""stop"" goto unsupportedCmd
if not ""%1"" == ""debug"" goto doExec
set DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
set "args=%*"
set "args=%args:* =%"
goto doExec

:doDeploy
:doUndeploy
%_RUNJAVA% -Djava.io.tmpdir="%CATALINA_TMPDIR%" org.apache.openejb.cli.Bootstrap %1 -s http://localhost:%port%/tomee/ejb %2
goto end

:unsupportedCmd
echo start/stop commands are not compatible with tomee.bat, please use catalina.bar/startup.bat/shutdown.bat
goto end

:doExec
%_RUNJAVA% %DEBUG% "-Dopenejb.base=%CATALINA_BASE%" "-Dopenejb.home=%CATALINA_HOME%" "-Djava.io.tmpdir=%CATALINA_TMPDIR%" org.apache.openejb.cli.Bootstrap %args%
goto end

:end

