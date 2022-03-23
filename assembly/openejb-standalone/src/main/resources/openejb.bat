@echo off
CLS
REM================================================
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
REM $Rev$ $Date$
REM================================================

@echo off
rem -------------------------------------------------------------------------
rem Manager Bootstrap Script for Windows
rem -------------------------------------------------------------------------

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set "DIRNAME=.\"
)

pushd "%DIRNAME%

REM echo Path is: "%DIRNAME%"
set OPENEJB_HOME=.

cd ..

set "OPENEJB_HOME=%CD%"

REM echo OPENEJB_HOME is: %OPENEJB_HOME%

set OPENEJB_CORE_JAR="%OPENEJB_HOME%\lib\openejb-core-*.jar"
set OPENEJB_EE_JAR="%OPENEJB_HOME%\lib\jakartaee-api-*.jar"
set OPENEJB_JAVAAGENT_JAR="%OPENEJB_HOME%\lib\openejb-javaagent-*.jar"

for %%a in (%OPENEJB_CORE_JAR%) do (
  set OPENEJB_CORE_JAR="%%a"
)

for %%a in (%OPENEJB_EE_JAR%) do (
  set OPENEJB_EE_JAR="%%a"
)

for %%a in (%OPENEJB_JAVAAGENT_JAR%) do (
  set OPENEJB_JAVAAGENT_JAR="%%a"
)

REM echo OPENEJB_CORE_JAR is: %OPENEJB_CORE_JAR%
REM echo OPENEJB_EE_JAR is: %OPENEJB_EE_JAR%
REM echo OPENEJB_JAVAAGENT_JAR is: %OPENEJB_JAVAAGENT_JAR%

if exist "%OPENEJB_CORE_JAR%" goto openejbHomeSet

:noOpenEJBHome
echo OPENEJB_HOME is set incorrectly or OpenEJB could not be located at "%OPENEJB_HOME%".
goto EOF

:openejbHomeSet
set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

REM echo %OPENEJB_OPTS% -javaagent:%OPENEJB_JAVAAGENT_JAR% -jar %OPENEJB_CORE_JAR% %*

java %OPENEJB_OPTS% -Djava.util.logging.config.file=%OPENEJB_HOME%/conf/logging.properties -javaagent:%OPENEJB_JAVAAGENT_JAR% -cp %OPENEJB_CORE_JAR%;%OPENEJB_EE_JAR%  org.apache.openejb.cli.Bootstrap %*

:EOF
ENDLOCAL
