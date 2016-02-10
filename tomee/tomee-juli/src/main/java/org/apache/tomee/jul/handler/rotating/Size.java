/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jul.handler.rotating;

import static java.lang.Long.MAX_VALUE;

class Size {
    private long size;
    private SizeUnit unit;

    private Size() {
        // no-op
    }

    private Size(final long size, final SizeUnit unit) {
        this.size = size;
        this.unit = unit;
    }

    Size(final String string) {
        this(string, null);
    }

    private Size(final String string, final SizeUnit defaultUnit) {
        final String[] strings = string.split(",| and ");

        Size total = new Size();
        for (String s : strings) {
            final Size part = new Size();
            s = s.trim();

            final StringBuilder t = new StringBuilder();
            final StringBuilder u = new StringBuilder();

            int i = 0;

            // get the number
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isDigit(c) || i == 0 && c == '-' || i > 0 && c == '.') {
                    t.append(c);
                } else {
                    break;
                }
            }

            if (t.length() == 0) {
                invalidFormat(s);
            }

            // skip whitespace
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (!Character.isWhitespace(c)) {
                    break;
                }
            }

            // get time unit text part
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isLetter(c)) {
                    u.append(c);
                } else {
                    invalidFormat(s);
                }
            }


            part.unit = parseUnit(u.toString());

            if (part.unit == null) {
                part.unit = defaultUnit;
            }

            final String size = t.toString();
            if (size.contains(".")) {
                if (part.unit == null) {
                    throw new IllegalArgumentException("unit must be specified with floating point numbers");
                }
                final double d = Double.parseDouble(size);
                final long bytes = part.unit.toBytes(1);
                part.size = (long) (bytes * d);
                part.unit = SizeUnit.BYTES;
            } else {
                part.size = Integer.parseInt(size);
            }

            total = total.add(part);
        }

        this.size = total.size;
        this.unit = total.unit;
    }

    public long asBytes() {
        return unit.toBytes(size);
    }

    private static final class Normalize {
        private final long a;
        private final long b;
        private final SizeUnit base;

        private Normalize(final Size a, final Size b) {
            this.base = lowest(a, b);
            this.a = a.unit == null ? a.size : base.convert(a.size, a.unit);
            this.b = b.unit == null ? b.size : base.convert(b.size, b.unit);
        }

        private static SizeUnit lowest(final Size a, final Size b) {
            if (a.unit == null) {
                return b.unit;
            }
            if (b.unit == null) {
                return a.unit;
            }
            if (a.size == 0) {
                return b.unit;
            }
            if (b.size == 0) {
                return a.unit;
            }
            return SizeUnit.values()[Math.min(a.unit.ordinal(), b.unit.ordinal())];
        }
    }

    public Size add(final Size that) {
        final Normalize n = new Normalize(this, that);
        return new Size(n.a + n.b, n.base);
    }

    private static void invalidFormat(final String text) {
        throw new IllegalArgumentException("Illegal size format: '" + text + "'.  Valid examples are '10kb' or '10 kilobytes'.");
    }

    private static SizeUnit parseUnit(final String u) {
        if (u.length() == 0) {
            return null;
        }

        if ("BYTES".equalsIgnoreCase(u)) {
            return SizeUnit.BYTES;
        }
        if ("BYTE".equalsIgnoreCase(u)) {
            return SizeUnit.BYTES;
        }
        if ("B".equalsIgnoreCase(u)) {
            return SizeUnit.BYTES;
        }

        if ("KILOBYTES".equalsIgnoreCase(u)) {
            return SizeUnit.KILOBYTES;
        }
        if ("KILOBYTE".equalsIgnoreCase(u)) {
            return SizeUnit.KILOBYTES;
        }
        if ("KILO".equalsIgnoreCase(u)) {
            return SizeUnit.KILOBYTES;
        }
        if ("KB".equalsIgnoreCase(u)) {
            return SizeUnit.KILOBYTES;
        }
        if ("K".equalsIgnoreCase(u)) {
            return SizeUnit.KILOBYTES;
        }

        if ("MEGABYTES".equalsIgnoreCase(u)) {
            return SizeUnit.MEGABYTES;
        }
        if ("MEGABYTE".equalsIgnoreCase(u)) {
            return SizeUnit.MEGABYTES;
        }
        if ("MEGA".equalsIgnoreCase(u)) {
            return SizeUnit.MEGABYTES;
        }
        if ("MB".equalsIgnoreCase(u)) {
            return SizeUnit.MEGABYTES;
        }
        if ("M".equalsIgnoreCase(u)) {
            return SizeUnit.MEGABYTES;
        }

        if ("GIGABYTES".equalsIgnoreCase(u)) {
            return SizeUnit.GIGABYTES;
        }
        if ("GIGABYTE".equalsIgnoreCase(u)) {
            return SizeUnit.GIGABYTES;
        }
        if ("GIGA".equalsIgnoreCase(u)) {
            return SizeUnit.GIGABYTES;
        }
        if ("GB".equalsIgnoreCase(u)) {
            return SizeUnit.GIGABYTES;
        }
        if ("G".equalsIgnoreCase(u)) {
            return SizeUnit.GIGABYTES;
        }

        throw new IllegalArgumentException("Unknown size unit '" + u + "'");
    }

    private enum SizeUnit {
        BYTES {
            public long toBytes(final long s) {
                return s;
            }

            public long toKilobytes(final long s) {
                return s / (B1 / B0);
            }

            public long toMegabytes(final long s) {
                return s / (B2 / B0);
            }

            public long toGigabytes(final long s) {
                return s / (B3 / B0);
            }

            public long toTerabytes(final long s) {
                return s / (B4 / B0);
            }

            public long convert(final long s, final SizeUnit u) {
                return u.toBytes(s);
            }
        },

        KILOBYTES {
            public long toBytes(final long s) {
                return x(s, B1 / B0, MAX_VALUE / (B1 / B0));
            }

            public long toKilobytes(final long s) {
                return s;
            }

            public long toMegabytes(final long s) {
                return s / (B2 / B1);
            }

            public long toGigabytes(final long s) {
                return s / (B3 / B1);
            }

            public long toTerabytes(final long s) {
                return s / (B4 / B1);
            }

            public long convert(final long s, final SizeUnit u) {
                return u.toKilobytes(s);
            }
        },

        MEGABYTES {
            public long toBytes(final long s) {
                return x(s, B2 / B0, MAX_VALUE / (B2 / B0));
            }

            public long toKilobytes(final long s) {
                return x(s, B2 / B1, MAX_VALUE / (B2 / B1));
            }

            public long toMegabytes(final long s) {
                return s;
            }

            public long toGigabytes(final long s) {
                return s / (B3 / B2);
            }

            public long toTerabytes(final long s) {
                return s / (B4 / B2);
            }

            public long convert(final long s, final SizeUnit u) {
                return u.toMegabytes(s);
            }
        },

        GIGABYTES {
            public long toBytes(final long s) {
                return x(s, B3 / B0, MAX_VALUE / (B3 / B0));
            }

            public long toKilobytes(final long s) {
                return x(s, B3 / B1, MAX_VALUE / (B3 / B1));
            }

            public long toMegabytes(final long s) {
                return x(s, B3 / B2, MAX_VALUE / (B3 / B2));
            }

            public long toGigabytes(final long s) {
                return s;
            }

            public long toTerabytes(final long s) {
                return s / (B4 / B3);
            }

            public long convert(final long s, final SizeUnit u) {
                return u.toGigabytes(s);
            }
        },

        TERABYTES {
            public long toBytes(final long s) {
                return x(s, B4 / B0, MAX_VALUE / (B4 / B0));
            }

            public long toKilobytes(final long s) {
                return x(s, B4 / B1, MAX_VALUE / (B4 / B1));
            }

            public long toMegabytes(final long s) {
                return x(s, B4 / B2, MAX_VALUE / (B4 / B2));
            }

            public long toGigabytes(final long s) {
                return x(s, B4 / B3, MAX_VALUE / (B4 / B3));
            }

            public long toTerabytes(final long s) {
                return s;
            }

            public long convert(final long s, final SizeUnit u) {
                return u.toTerabytes(s);
            }
        };

        static final long B0 = 1L;
        static final long B1 = B0 * 1024L;
        static final long B2 = B1 * 1024L;
        static final long B3 = B2 * 1024L;
        static final long B4 = B3 * 1024L;


        static long x(final long d, final long m, final long over) {
            if (d > over) {
                return MAX_VALUE;
            }
            if (d < -over) {
                return Long.MIN_VALUE;
            }
            return d * m;
        }

        public long toBytes(final long size) {
            throw new AbstractMethodError();
        }

        public long toKilobytes(final long size) {
            throw new AbstractMethodError();
        }

        public long toMegabytes(final long size) {
            throw new AbstractMethodError();
        }

        public long toGigabytes(final long size) {
            throw new AbstractMethodError();
        }

        public long toTerabytes(final long size) {
            throw new AbstractMethodError();
        }

        public long convert(final long sourceSize, final SizeUnit sourceUnit) {
            throw new AbstractMethodError();
        }
    }
}
