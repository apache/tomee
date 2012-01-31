#! /bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

version="${openejb.version}"
port=8080

# DEBUG=
DEBUG="-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# get java
cygwin=false
darwin=false
os400=false
case "`uname`" in
  CYGWIN*) cygwin=true;;
  Darwin*) darwin=true;;
esac

if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JRE_HOME" ] && JRE_HOME=`cygpath --unix "$JRE_HOME"`
  [ -n "$TOMEE_HOME" ] && TOMEE_HOME=`cygpath --unix "$TOMEE_HOME"`
  [ -n "$CATALINA_BASE" ] && CATALINA_BASE=`cygpath --unix "$CATALINA_BASE"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  JRE_HOME=`cygpath --absolute --windows "$JRE_HOME"`
  TOMEE_HOME=`cygpath --absolute --windows "$TOMEE_HOME"`
  CATALINA_BASE=`cygpath --absolute --windows "$CATALINA_BASE"`
  CATALINA_TMPDIR=`cygpath --absolute --windows "$CATALINA_TMPDIR"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
fi

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
[ -z "$TOMEE_HOME" ] && TOMEE_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

. "$TOMEE_HOME"/bin/setclasspath.sh

if [ -d $JAVA_HOME ]; then
  JAVA=$JAVA_HOME"/bin/java"
else
  JAVA="java"
fi

CP=""
for i in $TOMEE_HOME/webapps/tomee/lib/*.jar; do
  CP="$CP:$i"
done

# execute the command
if [ "$1" = "deploy" ] || [ "$1" = "undeploy" ]; then
    if [ $# -eq 2 ]; then
        echo "${1}ing $2"
        $JAVA $DEBUG -Dopenejb.base="$TOMEE_HOME" -cp "\"$CP\"" org.apache.openejb.cli.Bootstrap $1 -s http://localhost:$port/tomee/ejb $2
    else
        echo "Usage: <tomee.sh> $1 <path>"
    fi
else
    echo "Usage:"
    echo "  deploy   -> <tomee.sh> deploy <path>"
    echo "  undeploy -> <tomee.sh> undeploy <path>"
fi
