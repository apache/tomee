package org.apache.openejb.maven.util;

import org.junit.Test;

public class XmlFormatterTest {
    @Test
    public void format() {
        System.out.println(XmlFormatter.format("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                "    <foo id=\"bar\"/>\n" +
                "</root>"));
    }
}
