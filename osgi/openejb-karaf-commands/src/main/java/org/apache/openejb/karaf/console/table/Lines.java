package org.apache.openejb.karaf.console.table;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Lines {
    private List<Line> lines = new ArrayList<Line>();

    public void add(Line line) {
        if (!lines.isEmpty() && lines.iterator().next().getColumns().length != line.getColumns().length) {
            throw new IllegalArgumentException("columns should have all the same size");
        }
        lines.add(line);
    }

    public void print(final PrintStream out) {
        print(out, true);
    }

    public void print(final PrintStream out, boolean headers) {
        final Iterator<Line> it = lines.iterator();
        if (!it.hasNext()) {
            return;
        }

        int[] max = max(lines);
        it.next().print(max, out, headers);
        while (it.hasNext()) {
            it.next().print(max, out);
        }
    }

    private static int[] max(final List<Line> lines) {
        int[] max = new int[lines.iterator().next().getColumns().length];
        for (Line line : lines) {
            for (int i = 0; i < max.length; i++) {
                int ll = line.getColumns()[i].length();
                if (max[i] == 0) { // init
                    max[i] = ll;
                } else if (max[i] < ll) {
                    max[i] = ll;
                }
            }
        }
        return max;
    }
}
