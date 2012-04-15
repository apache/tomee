/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder.filter;

/**
 * @version $Rev$ $Date$
 */
public class SuffixFilter implements Filter {

    private final String suffix;

    public SuffixFilter(String suffix) {
        assert suffix != null;
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean accept(String name) {
        return name.endsWith(suffix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuffixFilter that = (SuffixFilter) o;

        return suffix.equals(that.suffix);
    }

    @Override
    public int hashCode() {
        return suffix.hashCode();
    }

    @Override
    public String toString() {
        return "SuffixFilter{" +
                "suffix='" + suffix + '\'' +
                '}';
    }
}