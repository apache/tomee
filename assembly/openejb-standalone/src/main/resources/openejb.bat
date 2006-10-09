@echo off
REM================================================
REM   Control script for OpenEJB
REM   --------------------------
REM    
REM   This script is the central entry point to 
REM   all of OpenEJB's functions.
REM  
REM   Tested on Windows 2000
REM
REM
REM   Created by David Blevins 
REM             <david.blevins@visi.com>
REM _______________________________________________
REM $Rev$ $Date$
REM================================================

SETLOCAL

set OPENEJB_CORE_JAR=%OPENEJB_HOME%/lib/openejb-core-${pom.version}.jar

rem find OPENEJB_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%OPENEJB_CORE_JAR%" goto openejbHomeSet

:noOpenEJBHome
echo OPENEJB_HOME is set incorrectly or OpenEJB could not be located. Please set OPENEJB_HOME.
goto EOF

:openejbHomeSet
set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

set P1=_%1
set P2=_%2

if /I %P1% EQU _TEST         goto TEST
if /I %P1% EQU _VALIDATE     goto VALIDATE 
if /I %P1% EQU _DEPLOY       goto DEPLOY 
if /I %P1% EQU _START        goto START_SERVER
if /I %P1% EQU _STOP         goto STOP_SERVER

echo Unknown option: %1
goto HELP

goto EOF
REM================================================
:HELP
   
	java -jar %OPENEJB_CORE_JAR%

goto EOF
REM================================================
:TEST
   if /I %P2% EQU _LOCAL     goto TEST_INTRAVM
   if /I %P2% EQU _REMOTE    goto TEST_SERVER
   if /I %P2% EQU _--HELP    goto HELP_TEST
   if /I %P2% EQU _          goto TEST_NOARGS

   echo Unknown option: %2
   goto HELP_TEST                                   

goto EOF
REM================================================
:VALIDATE 
   shift
   java -jar %OPENEJB_CORE_JAR% validate %1 %2 %3 %4 %5 %6 %7 %8 %9

goto EOF
REM================================================
:DEPLOY 
   shift
   java -jar %OPENEJB_CORE_JAR% deploy %1 %2 %3 %4 %5 %6 %7 %8 %9

goto EOF
REM================================================
:START_SERVER
   shift
   java -jar %OPENEJB_CORE_JAR% start %1 %2 %3 %4 %5 %6 %7 %8 %9

goto EOF
REM================================================
:STOP_SERVER
   shift
   java -jar %OPENEJB_CORE_JAR% stop %1 %2 %3 %4 %5 %6 %7 %8 %9

goto EOF
REM================================================
:TEST_NOARGS
   goto TEST_INTRAVM
goto EOF
REM================================================
:TEST_INTRAVM

   java -jar %OPENEJB_CORE_JAR% test local
         
if /I %P2% EQU _ goto TEST_SERVER
goto EOF
REM================================================
:TEST_SERVER

   java -jar %OPENEJB_CORE_JAR% test remote
   
goto EOF
REM================================================
:HELP_TEST
   
	java -jar %OPENEJB_CORE_JAR% test --help

goto EOF
REM================================================
:HELP_DEPLOY
   
	java -jar %OPENEJB_CORE_JAR% deploy --help
	
goto EOF
REM================================================
:HELP_VALIDATE
   
	java -jar %OPENEJB_CORE_JAR% validate --help

goto EOF
REM================================================
:HELP_START
   
	java -jar %OPENEJB_CORE_JAR% start --help

goto EOF
REM================================================
:HELP_STOP
   
	java -jar %OPENEJB_CORE_JAR% stop --help

goto EOF

:EOF
ENDLOCAL
