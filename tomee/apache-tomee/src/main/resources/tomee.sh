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

version="${tomee.version}"

DEBUG=
#DEBUG="-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

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
  [ -n "$TOMEE_BASE" ] && TOMEE_BASE=`cygpath --unix "$TOMEE_BASE"`
fi
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  TOMEE_BASE=`cygpath --absolute --windows "$TOMEE_BASE"`
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
[ -z "$TOMEE_BASE" ] && TOMEE_BASE=`cd "$PRGDIR/.." >/dev/null; pwd`

. "$TOMEE_BASE"/bin/setclasspath.sh
[[ -f "$TOMEE_BASE"/bin/setenv.sh ]] && . "$TOMEE_BASE"/bin/setenv.sh

if [ -z $JAVA_HOME ]; then
  JAVA="java"
else
  JAVA=$JAVA_HOME"/bin/java"
fi

CP="$TOMEE_BASE/lib"
for i in $TOMEE_BASE/lib/*.jar; do
  CP="$CP:$i"
done

# execute the command
if [ "$1" = "deploy" ] || [ "$1" = "undeploy" ]; then
    if [ $# -eq 2 ]; then
        echo "${1}ing $2"
        $JAVA $DEBUG -Dopenejb.base="$TOMEE_BASE" -cp "\"$CP\"" org.apache.openejb.cli.Bootstrap $1 -s auto $2
    else
        echo "Usage: <tomee.sh> $1 <path>"
    fi
elif [ "$1" = "start" ] || [ "$1" = "stop" ]; then
    echo "To start or stop TomEE please use catalina.sh/startup.sh/shutdown.sh instead of tomee.sh"
else
    $JAVA $DEBUG -Dopenejb.base="$TOMEE_BASE" -cp "\"$CP\"" org.apache.openejb.cli.Bootstrap $*
fi

