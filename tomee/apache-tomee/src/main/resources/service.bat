@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem The following line can be used to define a specific jre or jdk path
rem set "JAVA_HOME=C:/JDK"

REM Prefer a local JRE if we find one in the current bin directory
IF EXIST "%~dp0jre" (
  SET "JRE_HOME=%~dp0jre"
) 

REM Prefer a local JDK if we find one in the current bin directory
IF EXIST "%~dp0jdk" (
  SET "JAVA_HOME=%~dp0jdk"
)

@IF NOT "%ECHO%" == ""  ECHO %ECHO%
@IF "%OS%" == "Windows_NT" setlocal

IF "%OS%" == "Windows_NT" (
  SET "DIRNAME=%~dp0%"
) ELSE (
  SET DIRNAME=.\
)

pushd %DIRNAME%

rem ---------------------------------------------------------------------------
rem NT Service Install/Uninstall script
rem
rem Options
rem install                Install the service using TomEE as service name.
rem                        Service is installed using default settings.
rem remove                 Remove the service from the System.
rem
rem name        (optional) If the second argument is present it is considered
rem                        to be new service name
rem
rem $Id: service.bat 1000718 2010-09-24 06:00:00Z mturk $
rem ---------------------------------------------------------------------------

SET proc=undefined

IF /i %PROCESSOR_ARCHITECTURE% EQU X86 SET "proc=%~dp0TomEE.x86.exe"
IF /i %PROCESSOR_ARCHITECTURE% EQU AMD64 SET "proc=%~dp0TomEE.amd64.exe"
IF /i %PROCESSOR_ARCHITECTURE% EQU IA64 SET "proc=%~dp0TomEE.ia64.exe"

IF /i "%proc%" EQU undefined (
	ECHO Failed to determine OS architecture
	GOTO end
)

set "SELF=%~dp0%service.bat"
rem Guess CATALINA_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%CATALINA_HOME%" == "" goto gotHome
set "CATALINA_HOME=%cd%"
if exist "%CATALINA_HOME%\bin\service.bat" goto okHome
rem CD to the upper dir
cd ..
set "CATALINA_HOME=%cd%"
:gotHome
if exist "%CATALINA_HOME%\bin\service.bat" goto okHome
echo The service exe was not found...
echo The CATALINA_HOME environment variable is not defined correctly.
echo This environment variable is needed to run this program
goto end
:okHome
rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJdkHome
if not "%JRE_HOME%" == "" goto gotJreHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo Service will try to guess them from the registry.
goto okJavaHome
:gotJreHome
if not exist "%JRE_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JRE_HOME%\bin\javaw.exe" goto noJavaHome
goto okJavaHome
:gotJdkHome
if not exist "%JAVA_HOME%\jre\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\jre\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
if not "%JRE_HOME%" == "" goto okJavaHome
set "JRE_HOME=%JAVA_HOME%\jre"
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK not a JRE
goto end
:okJavaHome
if not "%CATALINA_BASE%" == "" goto gotBase
set "CATALINA_BASE=%CATALINA_HOME%"
:gotBase

set "EXECUTABLE=%proc%"

rem Set default Service name (If you change this then rename also TomEE.exe to the same name)
set SERVICE_NAME=TomEE
set PR_DISPLAYNAME=Apache TomEE

if "x%1x" == "xx" goto displayUsage
set SERVICE_CMD=%1
shift
if "x%1x" == "xx" goto checkServiceCmd
:checkUser
if "x%1x" == "x/userx" goto runAsUser
if "x%1x" == "x--userx" goto runAsUser
set SERVICE_NAME=%1
set PR_DISPLAYNAME=Apache TomEE (%1)
shift
if "x%1x" == "xx" goto checkServiceCmd
goto checkUser
:runAsUser
shift
if "x%1x" == "xx" goto displayUsage
set SERVICE_USER=%1
shift
runas /env /savecred /user:%SERVICE_USER% "%COMSPEC% /K \"%SELF%\" %SERVICE_CMD% %SERVICE_NAME%"
goto end
:checkServiceCmd
if /i %SERVICE_CMD% == install goto doInstall
if /i %SERVICE_CMD% == remove goto doRemove
if /i %SERVICE_CMD% == uninstall goto doRemove
echo Unknown parameter "%1"
:displayUsage
echo.
echo Usage: service.bat install/remove [service_name] [/user username]
goto end

:doRemove
rem Remove the service
"%EXECUTABLE%" //DS//%SERVICE_NAME%
if not errorlevel 1 goto removed
echo Failed removing '%SERVICE_NAME%' service
goto end
:removed
echo The service '%SERVICE_NAME%' has been removed
goto end

:doInstall
rem Install the service
echo Installing the service '%SERVICE_NAME%' ...
echo Using CATALINA_HOME:    "%CATALINA_HOME%"
echo Using CATALINA_BASE:    "%CATALINA_BASE%"
echo Using JAVA_HOME:        "%JAVA_HOME%"
echo Using JRE_HOME:         "%JRE_HOME%"

rem Use the environment variables as an example
rem Each command line option is prefixed with PR_

set "PR_DESCRIPTION=Apache TomEE - http://tomee.apache.org/"
set "PR_INSTALL=%EXECUTABLE%"
set "PR_LOGPATH=%CATALINA_BASE%\logs"
set "PR_CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar;%CATALINA_BASE%\bin\tomcat-juli.jar;%CATALINA_HOME%\bin\tomcat-juli.jar"
rem Set the server jvm from JAVA_HOME
set "PR_JVM=%JRE_HOME%\bin\server\jvm.dll"
if exist "%PR_JVM%" goto foundJvm
rem Set the client jvm from JAVA_HOME
set "PR_JVM=%JRE_HOME%\bin\client\jvm.dll"
if exist "%PR_JVM%" goto foundJvm
set PR_JVM=auto
:foundJvm
echo Using JVM:              "%PR_JVM%"

"%EXECUTABLE%" //IS//%SERVICE_NAME% ^
    --DisplayName=%SERVICE_NAME% ^
    --StartClass org.apache.catalina.startup.Bootstrap ^
    --StopClass org.apache.catalina.startup.Bootstrap ^
    --StartParams start ^
    --StopParams stop ^
    --Startup auto ^
    --JvmMs=512 ^
    --JvmMx=1024 ^
    --JvmSs=2048 ^
    --StartMode jvm ^
    --StopMode jvm ^
    --LogLevel Info ^
    --LogPrefix TomEE
    
echo Installed, will now configure TomEE
    
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end

:installed
rem Clear the environment variables. They are not needed any more.
set PR_DISPLAYNAME=
set PR_DESCRIPTION=
set PR_INSTALL=
set PR_LOGPATH=
set PR_CLASSPATH=
set PR_JVM=

rem Set extra parameters
"%EXECUTABLE%" //US//%SERVICE_NAME% ^
	++JvmOptions "-javaagent:%CATALINA_HOME%\lib\openejb-javaagent.jar;-Dcatalina.base=%CATALINA_BASE%;-Dcatalina.home=%CATALINA_HOME%;-Djava.endorsed.dirs=%CATALINA_HOME%\endorsed"

rem More extra parameters
set "PR_LOGPATH=%CATALINA_BASE%\logs"
set PR_STDOUTPUT=auto
set PR_STDERROR=auto

rem before this option was added: "++JvmOptions=-Djava.library.path="%CATALINA_BASE%\bin" ^"
rem the drawback was it was preventing custom native lib to be loaded even if added to Path
"%EXECUTABLE%" //US//%SERVICE_NAME% ^
	++JvmOptions "-Djava.io.tmpdir=%CATALINA_BASE%\temp;-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager;-Djava.util.logging.config.file=%CATALINA_BASE%\conf\logging.properties;-Djava.awt.headless=true;-XX:+UseParallelGC;-XX:MaxPermSize=256M"

echo The service '%SERVICE_NAME%' has been installed.

:end
cd "%CURRENT_DIR%"
