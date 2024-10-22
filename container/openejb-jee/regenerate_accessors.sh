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

# This regenerates the accessor classes, adds the ASF license header to the generated classes and moves them to ../openejb-jee-accessors
# Run this when modifying JAXB classes in this maven module!
mvn -Pgenerate-accessors -DskipTests

SRC_DIR="target/sxc/org/apache/openejb/jee"
DEST_DIR="../openejb-jee-accessors/src/main/java/org/apache/openejb/jee"

# Define the ASF license header
ASF_LICENSE_HEADER='/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */'

rm -rf $DEST_DIR
mkdir -p $DEST_DIR

find "$SRC_DIR" -type f -name "*.java" | while read -r file; do
  echo "$ASF_LICENSE_HEADER" | cat - "$file" > temp && mv temp "$file"
  relative_path="${file#$SRC_DIR/}"
  mkdir -p "$DEST_DIR/$(dirname "$relative_path")"
  mv "$file" "$DEST_DIR/$relative_path"
done