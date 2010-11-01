@ECHO off
CLS

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
IF /i %PROCESSOR_ARCHITECTURE% EQU IA64 SET proc=%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"

IF /i %proc% EQU undefined (
	ECHO Failed to determine OS architecture
	GOTO failed
)

SET jvm=auto
REM SET JAVA_HOME=[Full path to JDK or JRE]

REM Prefer a local JRE if we find one in the current directory
IF EXIST "%~dp0jre" (
	SET JAVA_HOME="%~dp0"
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

%proc% //IS//OpenEJBServer --DisplayName="OpenEJB Server" ^
	--Install=%proc% ^
	--Startup auto ^
	--StartPath=%openejb% ^
	--Description="OpenEJB Server Service" ^
	--Jvm=%jvm% ^
	--Classpath=%classpath% ^
	--StartMode=jvm ^
	--StartClass=org.apache.openejb.daemon.NTService --StartMethod=start ^
	--StopMode=jvm ^
	--StopClass=org.apache.openejb.daemon.NTService --StopMethod=stop ^
	--LogPrefix=service ^
	--LogPath=%logs% --StdOutput="%logs%\service.out.log" --StdError="%logs%\service.err.log" --PidFile=service.pid ^
	--LogLevel=Info ^
	++JvmOptions=-Dopenejb.home="%openejb%";-Xms128M;-Xmx512M;-XX:MaxPermSize=256M
	rem ++DependsOn= \

GOTO complete

:failed

ECHO Failed to install service

:complete