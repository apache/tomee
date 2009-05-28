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
REM $Rev: 636963 $ $Date: 2008-03-13 19:40:08 -0700 (Thu, 13 Mar 2008) $
REM================================================

rem @echo off
echo alias %1
echo keypass %2
echo keystoreName %3
echo KeyStorePass %4
echo keyName %5

echo keyName %5
keytool -genkey -alias %1 -keypass %2 -keystore %3 -storepass %4  -dname "cn=%1" -keyalg RSA
keytool -selfcert -alias %1 -keystore %3 -storepass %4 -keypass %2
keytool -export -alias %1 -file %5 -keystore %3 -storepass %4
