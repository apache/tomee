/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.table;

import org.apache.openejb.util.JavaSecurityManagers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Lines {
    private final List<Line> lines = new ArrayList<>();
    private final String cr;

    public Lines() {
        this(JavaSecurityManagers.getSystemProperty("line.separator"));
    }

    public Lines(final String cr) {
        this.cr = cr;
    }

    public void add(final Line line) {
        if (!lines.isEmpty() && lines.iterator().next().getColumns().length != line.getColumns().length) {
            throw new IllegalArgumentException("columns should have all the same size");
        }
        line.setCr(cr);
        lines.add(line);
    }

    public void print(final PrintStream out) {
        print(out, true);
    }

    public void print(final PrintStream out, final boolean headers) {
        final Iterator<Line> it = lines.iterator();
        if (!it.hasNext()) {
            return;
        }

        final int[] max = max(lines);
        it.next().print(max, out, headers);
        while (it.hasNext()) {
            it.next().print(max, out);
        }
    }

    private static int[] max(final List<Line> lines) {
        final int[] max = new int[lines.iterator().next().getColumns().length];
        for (final Line line : lines) {
            for (int i = 0; i < max.length; i++) {
                final int ll = line.getColumns()[i].length();
                if (max[i] == 0) { // init
                    max[i] = ll;
                } else if (max[i] < ll) {
                    max[i] = ll;
                }
            }
        }
        return max;
    }

    public List<Line> getLines() {
        return lines;
    }
}
