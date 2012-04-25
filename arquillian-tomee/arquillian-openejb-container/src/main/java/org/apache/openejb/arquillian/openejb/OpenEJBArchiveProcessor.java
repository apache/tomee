package org.apache.openejb.arquillian.openejb;

import java.io.IOException;
import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class OpenEJBArchiveProcessor implements ApplicationArchiveProcessor {
    private static final String EJB_JAR_XML = "ejb-jar.xml";
    private static final String WEB_INF = "/WEB-INF";
    private static final String META_INF = "/META-INF";

    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        final ArchivePath path = ArchivePaths.create(EJB_JAR_XML);
        final Asset newAsset;
        if (archive.contains(ArchivePaths.create(path.get()))) {
            final Node node = archive.get(path);
            final Asset asset = node.getAsset();
            newAsset = enhancedAsset(asset, testClass.getJavaClass());
        } else {
            newAsset = new StringAsset(ejbJar(testClass.getJavaClass()));
        }


        if (archive instanceof WebArchive) {
            archive.delete(WEB_INF + path.get());
            ((WebArchive) archive).addAsWebInfResource(newAsset, path);
        } else if (archive instanceof JavaArchive) {
            archive.delete(META_INF + path.get());
            ((JavaArchive) archive).addAsManifestResource(newAsset, path);
        }
    }

    private Asset enhancedAsset(Asset asset, Class<?> javaClass) {
        if (asset instanceof EmptyAsset) {
            return new StringAsset(ejbJar(javaClass));
        } else {
            String content;
            try {
                content = IO.slurp(asset.openStream());
            } catch (IOException e) {
                content = "";
            }

            if (content == null || content.isEmpty()) {
                return new StringAsset(ejbJar(javaClass));
            } else if (content.contains("<enterprise-beans>")) {
                content = content.replace("<enterprise-beans>", "<enterprise-beans>\n" + managedBeanBlock(javaClass));
                return new StringAsset(content);
            } else if (content.contains("<ejb-jar>")) {
                content = content.replace("<ejb-jar>", "<ejb-jar>\n" + enterpriseBean(javaClass));
                return new StringAsset(content);
            }
            return asset; // shouldn't happen
        }
    }

    private String enterpriseBean(final Class<?> clazz) {
        return "   <enterprise-beans>\n" +
                managedBeanBlock(clazz) +
                "   </enterprise-beans>\n";
    }

    private String ejbJar(final Class<?> clazz) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ejb-jar xmlns=\"http://java.sun.com/xml/ns/javaee\" \n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd\"\n" +
                "         version=\"3.0\">\n" +
                enterpriseBean(clazz) +
                "</ejb-jar>\n";
    }

    private String managedBeanBlock(final Class<?> clazz) {
        return "      <session>\n" +
                "         <ejb-name>" + clazz.getSimpleName() + "</ejb-name>\n" +
                "         <ejb-class>" + clazz.getName() + "</ejb-class>\n" +
                "         <session-type>Managed</session-type>\n" +
                "         <transaction-type>Bean</transaction-type>\n" +
                "      </session>\n";
    }
}
