@echo off
cls

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%

SET proc=undefined

if /i %PROCESSOR_ARCHITECTURE% EQU X86 SET proc="%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"
if /i %PROCESSOR_ARCHITECTURE% EQU AMD64 SET proc="%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"
if /i %PROCESSOR_ARCHITECTURE% EQU IA64 SET proc=%~dp0OpenEJB.%PROCESSOR_ARCHITECTURE%.exe"

if /i %proc% EQU undefined GOTO failed

%proc% //DS//OpenEJBServer

GOTO complete

:failed

ECHO Failed to determine OS architecture

:complete