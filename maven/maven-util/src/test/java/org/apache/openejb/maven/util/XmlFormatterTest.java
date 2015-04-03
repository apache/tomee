package org.apache.openejb.maven.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XmlFormatterTest {
    @Test
    public void format() {
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + 
                "<root>" + System.lineSeparator() + 
                "    <foo id=\"bar\"/>" + System.lineSeparator() + 
                "</root>\n",
                XmlFormatter.format("<root><foo id=\"bar\"/></root>"));
    }
}
