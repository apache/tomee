@echo off
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

SETLOCAL

set OPENEJB_CORE_JAR=%OPENEJB_HOME%/lib/openejb-core-${version}.jar
set OPENEJB_JAVAAGENT_JAR=%OPENEJB_HOME%/lib/openejb-javaagent-${version}.jar

rem find OPENEJB_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%OPENEJB_CORE_JAR%" goto openejbHomeSet

:noOpenEJBHome
echo OPENEJB_HOME is set incorrectly or OpenEJB could not be located. Please set OPENEJB_HOME.
goto EOF

:openejbHomeSet
set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

java %OPENEJB_OPTS% -javaagent:%OPENEJB_JAVAAGENT_JAR% -jar %OPENEJB_CORE_JAR% validate %1 %2 %3 %4 %5 %6 %7 %8 %9

:EOF
ENDLOCAL
