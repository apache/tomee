package org.openejb.jee.ejbjar;
/**
 * @version $Revision$ $Date$
 */

import junit.framework.*;
import org.openejb.jee.ejbjar.EjbJarMarshaller;
import org.openejb.jee.javaee.EnvEntry;
import org.openejb.jee.javaee.Icon;
import org.openejb.jee.javaee.PersistenceUnitRef;
import com.thoughtworks.xstream.XStream;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Iterator;

public class EjbJarMarshallerTest extends TestCase {

    private XStream xstream;

    public void setUp() {
        EjbJarMarshaller m = new EjbJarMarshaller();
        xstream = m.getEjb30XStream();
    }

    public void testEjbJar1() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ejbJar.getDescription().add("One");
        ejbJar.getDescription().add("Two");

        String actual = xstream.toXML(ejbJar);
        String expected = "<ejb-jar>\n" +
                "  <description>One</description>\n" +
                "  <description>Two</description>\n" +
                "</ejb-jar>";
//        assertEquals(expected, actual);

        Icon icon = new Icon();
        icon.setLargeIcon("large1.jpg");
        icon.setSmallIcon("small1.jpg");
        ejbJar.getIcons().add(icon);

        Icon icon2 = new Icon();
        icon2.setLargeIcon("large2.jpg");
        icon2.setSmallIcon("small2.jpg");
        ejbJar.getIcons().add(icon2);

        expected = "<ejb-jar>\n" +
                "  <description>One</description>\n" +
                "  <description>Two</description>\n" +
                "  <icon>\n" +
                "    <small-icon>small1.jpg</small-icon>\n" +
                "    <large-icon>large1.jpg</large-icon>\n" +
                "  </icon>\n" +
                "  <icon>\n" +
                "    <small-icon>small2.jpg</small-icon>\n" +
                "    <large-icon>large2.jpg</large-icon>\n" +
                "  </icon>\n" +
                "</ejb-jar>";

        assertBothWays(ejbJar, expected);

    }

    public void testBeanDefs() throws Exception {
        EjbJar ejbJar = new EjbJar();
        Session session = new Session("Foo", "org.acme.FooEjb");
        session.setRemoteInterfaces("org.acme.FooHome", "org.acme.Foo");
        ejbJar.getEnterpriseBeans().add(session);

        Entity entity = new Entity("Bar", "org.acme.BarEjb");
        entity.setRemoteInterfaces("org.acme.BarHome", "org.acme.Bar");
        ejbJar.getEnterpriseBeans().add(entity);

        String expected = "<ejb-jar>\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>Foo</ejb-name>\n" +
                "      <home>org.acme.FooHome</home>\n" +
                "      <remote>org.acme.Foo</remote>\n" +
                "      <ejb-class>org.acme.FooEjb</ejb-class>\n" +
                "    </session>\n" +
                "    <entity>\n" +
                "      <reenterant>false</reenterant>\n" +
                "      <ejb-name>Bar</ejb-name>\n" +
                "      <home>org.acme.BarHome</home>\n" +
                "      <remote>org.acme.Bar</remote>\n" +
                "      <ejb-class>org.acme.BarEjb</ejb-class>\n" +
                "    </entity>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>";

        assertBothWays(ejbJar, expected);
    }

    public void testJndiEntriesCollection() throws Exception {
        EjbJar ejbJar = new EjbJar();

        Session session = new Session("Foo", "org.acme.FooEjb");
        session.setRemoteInterfaces("org.acme.FooHome", "org.acme.Foo");
        ejbJar.getEnterpriseBeans().add(session);

        EnvEntry envEntry = new EnvEntry("fruit","java.lang.String","orange");
        session.getJndiEnvironmentRefs().add(envEntry);

        PersistenceUnitRef persistenceUnitRef = new PersistenceUnitRef("puRefName", "puName");
        session.getJndiEnvironmentRefs().add(persistenceUnitRef);

        String expected = "<ejb-jar>\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>Foo</ejb-name>\n" +
                "      <home>org.acme.FooHome</home>\n" +
                "      <remote>org.acme.Foo</remote>\n" +
                "      <ejb-class>org.acme.FooEjb</ejb-class>\n" +
                "      <env-entry>\n" +
                "        <env-entry-name>fruit</env-entry-name>\n" +
                "        <env-entry-type>java.lang.String</env-entry-type>\n" +
                "        <env-entry-value>orange</env-entry-value>\n" +
                "      </env-entry>\n" +
                "      <persistence-unit-ref>\n" +
                "        <persistence-unit-ref-name>puRefName</persistence-unit-ref-name>\n" +
                "        <persistence-unit-name>puName</persistence-unit-name>\n" +
                "      </persistence-unit-ref>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>";

        String resultXml = xstream.toXML(ejbJar);
        assertEquals(expected, resultXml);
        EjbJar result = (EjbJar) xstream.fromXML(expected);
        EnterpriseBean session2 = result.getEnterpriseBeans().get(0);
        assertEquals(session.getEjbName(), session.getEjbName());

    }

    public void testEnumTypes() throws Exception {
        EjbJar ejbJar = new EjbJar();
        Session session = new Session("Foo", "org.acme.FooEjb", SessionType.STATELESS, null);
        session.setRemoteInterfaces("org.acme.FooHome", "org.acme.Foo");
        ejbJar.getEnterpriseBeans().add(session);

        String expected = "<ejb-jar>\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <session-type>Stateless</session-type>\n" +
                "      <ejb-name>Foo</ejb-name>\n" +
                "      <home>org.acme.FooHome</home>\n" +
                "      <remote>org.acme.Foo</remote>\n" +
                "      <ejb-class>org.acme.FooEjb</ejb-class>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>";

        assertBothWays(ejbJar, expected);
    }

    protected Object assertBothWays(Object root, String xml) {
        String resultXml = xstream.toXML(root);
        assertEquals(xml, resultXml);
        Object resultRoot = xstream.fromXML(xml);
        assertObjectsEqual(root, resultRoot);
        return resultRoot;
    }

    /**
     * More descriptive version of assertEquals
     */
    protected void assertObjectsEqual(Object expected, Object actual) {

        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull("Should not be null", actual);
            if (actual.getClass().isArray()) {
                assertArrayEquals(expected, actual);
            } else {
//                assertEquals(expected.getClass(), actual.getClass());
                if (!expected.equals(actual)) {
                    assertEquals("Object deserialization failed",
                            "DESERIALIZED OBJECT\n" + xstream.toXML(expected),
                            "DESERIALIZED OBJECT\n" + xstream.toXML(actual));
                }
            }
        }
    }

    protected void assertArrayEquals(Object expected, Object actual) {
        assertEquals(Array.getLength(expected), Array.getLength(actual));
        for (int i = 0; i < Array.getLength(expected); i++) {
            assertEquals(Array.get(expected, i), Array.get(actual, i));
        }
    }


    public boolean equals(EjbJar a, EjbJar b) {

        if (a.getEjbClientJar() != null ? !a.getEjbClientJar().equals(b.getEjbClientJar()) : b.getEjbClientJar() != null) return false;
        if (a.getIcons() != null ? !a.getIcons().equals(b.getIcons()) : b.getIcons() != null) return false;
        if (a.getVersion() != null ? !a.getVersion().equals(b.getVersion()) : b.getVersion() != null) return false;
        if (a.getMetadataComplete() != b.getMetadataComplete()) return false;

        if (!equals(a.getDescription(), b.getDescription())) return false;
        if (!equals(a.getDisplayName(), b.getDisplayName())) return false;

        if (!equals(a.getIcons(), b.getIcons())) return false;
        if (!equals(a.getEnterpriseBeans(), b.getEnterpriseBeans())) return false;
        if (!equals(a.getInterceptors(), b.getInterceptors())) return false;
        if (!equals(a.getRelationships(), b.getRelationships())) return false;

        if (!equals(a.getAssemblyDescriptor(), b.getAssemblyDescriptor())) return false;

        return true;
    }

    private boolean equals(AssemblyDescriptor a, AssemblyDescriptor b) {
        if (a == b) return true;
        if (a == null && b != null) return false;

        if (a.getId() != null ? !a.getId().equals(b.getId()) : b.getId() != null) return false;
        if (!equals(a.getApplicationExceptions(), b.getApplicationExceptions())) return false;
        if (!equals(a.getContainerTransactions(), b.getContainerTransactions())) return false;
        if (!equals(a.getExcludeLists(), b.getExcludeLists())) return false;
        if (!equals(a.getInterceptorBindings(), b.getInterceptorBindings())) return false;
        if (!equals(a.getMessageDestinations(), b.getMessageDestinations())) return false;
        if (!equals(a.getMethodPermissions(), b.getMethodPermissions())) return false;
        if (!equals(a.getSecurityRoles(), b.getSecurityRoles())) return false;

        return false;
    }

    private boolean equals(List listA, List listB) {
        if (listA == listB) return true;
        if (listA == null && listB != null) return false;

        if (listA.size() != listB.size()) return false;
        for (Iterator iterator = listA.iterator(); iterator.hasNext();) {
            Object item = iterator.next();
            if (!listB.contains(item)) return false;
        }
        return true;
    }


}

