@ECHO off
CLS
setlocal
REM ================================================
REM Licensed to the Apache Software Foundation (ASF) under one or more
REM contributor license agreements.  See the NOTICE file distributed with
REM this work for additional information regarding copyright ownership.
REM The ASF licenses this file to You under the Apache License, Version 2.0
REM (the "License"); you may not use this file except in compliance with
REM the License.  You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM _______________________________________________
REM $Rev$
REM ================================================

@IF NOT "%ECHO%" == ""  ECHO %ECHO%
@IF "%OS%" == "Windows_NT" setlocal

IF "%OS%" == "Windows_NT" (
  SET "DIRNAME=%~dp0%"
) ELSE (
  SET DIRNAME=.\
)

pushd %DIRNAME%

SET proc=undefined

IF /i %PROCESSOR_ARCHITECTURE% EQU X86 SET proc="%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"
IF /i %PROCESSOR_ARCHITECTURE% EQU AMD64 SET proc="%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"
IF /i %PROCESSOR_ARCHITECTURE% EQU IA64 SET proc="%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"

IF /i %proc% EQU undefined (
	ECHO Failed to determine OS architecture
	GOTO failed
)

SET jvm=auto
REM SET JAVA_HOME=[Full path to JDK or JRE]

REM Prefer a local JRE or JDK if we find one in the current or parent directory
pushd..
set dir=%cd%
popd

IF EXIST "%dir%\jre" (
	SET JAVA_HOME="%dir%"
	ECHO Found local JRE
	GOTO found_java_home
)

IF EXIST "%dir%\jdk" (
	SET JAVA_HOME="%dir%\jdk"
	ECHO Found local JDK
	GOTO found_java_home
)

if not "%JAVA_HOME%" == "" GOTO found_java_home

ECHO Environment variable JAVA_HOME is not set and no local JRE was found.
ECHO Please set JAVA_HOME to the directory of your local JDK or JRE to avoid this message.
GOTO skip_java_home

:found_java_home

REM Make an attempt to find either the server or client jvm.dll
IF EXIST "%JAVA_HOME%\jre\bin\server\jvm.dll" (
	SET jvm="%JAVA_HOME%\jre\bin\server\jvm.dll"
) ELSE (
	IF EXIST "%JAVA_HOME%\jre\bin\client\jvm.dll" (
		SET jvm="%JAVA_HOME%\jre\bin\client\jvm.dll"
	) ELSE (
		IF EXIST "%JAVA_HOME%\bin\server\jvm.dll" (
			SET jvm="%JAVA_HOME%\bin\server\jvm.dll"
		) ELSE (
			IF EXIST "%JAVA_HOME%\bin\client\jvm.dll" (
				SET jvm="%JAVA_HOME%\bin\client\jvm.dll"
			) ELSE (
				ECHO Cannot locate a jvm.dll - Are you sure JAVA_HOME is set correctly?
				ECHO The service installer will now attempt to locate and use 'any' JVM.
			)
		)
	)
)

:skip_java_home

SET openejb=.
CD ..

SET "openejb=%CD%"
SET logs=%openejb%\logs
SET lib=%openejb%\lib
SET corejar="%lib%\openejb-core-*.jar"
SET daemonjar="%lib%\openejb-daemon-*.jar"

FOR %%a IN (%corejar%) DO (
  SET corejar="%%a"
)

FOR %%a IN (%daemonjar%) DO (
  SET daemonjar="%%a"
)

SET classpath="%corejar%;%daemonjar%"

ECHO Installing service using JVM: %jvm%

REM Allow file access to Local System 
cacls "%openejb%" /E /P System:F

REM Extensive documentation can be found here - http://commons.apache.org/daemon/procrun.html

%proc% //IS//OpenEJBServer --DisplayName="OpenEJB Server" ^
	--Install=%proc% ^
	--Startup auto ^
	--StartPath="%openejb%" ^
	--Description="OpenEJB Server Service" ^
	--Jvm="%jvm%" ^
	--Classpath=%classpath% ^
	--StartMode=jvm ^
	--StartClass=org.apache.openejb.daemon.NTService --StartMethod=start ^
	--StopMode=jvm ^
	--StopClass=org.apache.openejb.daemon.NTService --StopMethod=stop ^
	--LogPrefix=service ^
	--LogPath="%logs%" --StdOutput="%logs%\service.out.log" --StdError="%logs%\service.err.log" --PidFile=service.pid ^
	--LogLevel=Info ^
	--LibraryPath="%openejb%\bin" ^
	++JvmOptions=-Dopenejb.home="%openejb%";-Xms128M;-Xmx512M;-XX:MaxPermSize=256M
	REM ++DependsOn=AnotherServiceName
	REM Add ^ symbol to end of ++JvmOptions line if ++DependsOn is uncommented 

NET START OpenEJBServer

GOTO complete

:failed

ECHO Failed to install service

:complete