@rem
@rem Licensed to the Apache Software Foundation (ASF) under one
@rem or more contributor license agreements.  See the NOTICE file
@rem distributed with this work for additional information
@rem regarding copyright ownership.  The ASF licenses this file
@rem to you under the Apache License, Version 2.0 (the
@rem "License"); you may not use this file except in compliance
@rem with the License.  You may obtain a copy of the License at
@rem
@rem  http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing,
@rem software distributed under the License is distributed on an
@rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@rem KIND, either express or implied.  See the License for the
@rem specific language governing permissions and limitations
@rem under the License.
@rem

@echo off
@rem --------------------------------------------------------------------------------------------------
@rem  Finds the difference between versions on different branch
@rem --------------------------------------------------------------------------------------------------
set REPOS=https://svn.apache.org/repos/asf/openjpa/trunk
set BRANCH=1.0.x
set OLD_ROOT=https://svn.apache.org/repos/asf/openjpa/trunk
set NEW_ROOT=https://svn.apache.org/repos/asf/openjpa/branches/%BRANCH%
rem set FILE=openjpa-kernel/src/main/java/org/apache/openjpa/kernel/BrokerImpl.java
set FILE=%1
@rem openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/meta/strats/RelationToManyInverseKeyFieldStrategy.java
set NEW_URL=%NEW_ROOT%/%FILE%
set OLD_URL=%OLD_ROOT%/%FILE%
@echo svn diff %1% between trunk AND %BRANCH%
svn diff --old=%OLD_URL% --new=%NEW_URL%
