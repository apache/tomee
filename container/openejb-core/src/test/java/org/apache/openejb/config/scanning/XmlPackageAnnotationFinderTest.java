package org.apache.openejb.config.scanning;

public class XmlPackageAnnotationFinderTest extends AbstractXmlAnnotationFinderTest {
    @Override
    protected String scanXml() {
        return "test-package-scan.xml";
    }
}
