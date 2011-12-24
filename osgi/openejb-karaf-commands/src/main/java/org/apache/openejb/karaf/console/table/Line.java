package org.apache.openejb.karaf.console.table;

import java.io.PrintStream;

public class Line {
    public static final String COL_SEP = "|";
    public static final String HEADER_CHAR = "=";
    public static final String LINE_CHAR = "-";
    public static final char EMPTY_CHAR = ' ';

    private String[] columns;

    public Line(String... columns) {
        this.columns = columns;
    }

    public String[] getColumns() {
        return columns;
    }

    public void print(final int[] max, final PrintStream out) {
        print(max, out, false);
    }

    public void print(final int[] max, final PrintStream out, boolean header) {
        final StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < max.length; i++) {
            sb.append(EMPTY_CHAR);
            int spaces = max[i] - columns[i].length();
            for (int k = 0; k < spaces / 2; k++) {
                sb.append(EMPTY_CHAR);
            }
            sb.append(columns[i]);
            for (int k = 0; k < spaces - (spaces / 2); k++) {
                sb.append(EMPTY_CHAR);
            }
            sb.append(EMPTY_CHAR).append(COL_SEP);
        }

        final String lineStr = sb.toString();

        final StringBuilder sep = new StringBuilder("");
        final String s;
        if (header) {
            s = HEADER_CHAR;
        } else {
            s = LINE_CHAR;
        }
        for (int i = 0; i < lineStr.length(); i++) {
            sep.append(s);
        }

        if (header) {
            printLine(out, sep.toString());
        }

        printLine(out, lineStr);
        printLine(out, sep.toString());
    }

    private static void printLine(final PrintStream out, final String s) {
        out.println(s);
    }
}
