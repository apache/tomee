index-group=Environment Entries
type=page
status=published
title=Custom Injection
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## Pickup

    package org.superbiz.enventries;
    
    
    //START SNIPPET: code
    
    import java.beans.PropertyEditorManager;
    
    public enum Pickup {
    
        HUMBUCKER,
        SINGLE_COIL;
    
        // Here's the little magic where we register the PickupEditor
        // which knows how to create this object from a string.
        // You can add any of your own Property Editors in the same way.
        static {
            PropertyEditorManager.registerEditor(Pickup.class, PickupEditor.class);
        }
    }

## PickupEditor

    package org.superbiz.enventries;
    
    /**
     * With a java.beans.PropertyEditor, you can go way beyond the built-in
     * types that OpenEJB supports and can extend dependency injection to
     * just about anywhere.
     * <p/>
     * In the world of electric guitars, two types of pickups are used: humbucking, and single-coil.
     * Guitarists often refer to their guitars as HSS, meaning a guitar with 1 humbucker and
     * 2 single coil pickups, and so on.  This little PropertyEditor supports that shorthand notation.
     *
     * @version $Revision$ $Date$
     */
    //START SNIPPET: code
    public class PickupEditor extends java.beans.PropertyEditorSupport {
    
        public void setAsText(String text) throws IllegalArgumentException {
            text = text.trim();
    
            if (text.equalsIgnoreCase("H")) setValue(Pickup.HUMBUCKER);
            else if (text.equalsIgnoreCase("S")) setValue(Pickup.SINGLE_COIL);
            else throw new IllegalStateException("H and S are the only supported Pickup aliases");
        }
    }

## Stratocaster

    package org.superbiz.enventries;
    
    import javax.annotation.Resource;
    import javax.ejb.Stateless;
    import java.io.File;
    import java.util.Date;
    import java.util.List;
    import java.util.Map;
    
    /**
     * In addition to the standard env-entry types (String, Integer, Long, Short, Byte, Boolean, Double, Float, Character)
     * OpenEJB supports many other types.
     */
    //START SNIPPET: code
    @Stateless
    public class Stratocaster {
    
    
        @Resource(name = "pickups")
        private List<Pickup> pickups;
    
        @Resource(name = "style")
        private Style style;
    
        @Resource(name = "dateCreated")
        private Date dateCreated;
    
        @Resource(name = "guitarStringGuages")
        private Map<String, Float> guitarStringGuages;
    
        @Resource(name = "certificateOfAuthenticity")
        private File certificateOfAuthenticity;
    
        public Date getDateCreated() {
            return dateCreated;
        }
    
        /**
         * Gets the guage of the electric guitar strings
         * used in this guitar.
         *
         * @param string
         * @return
         */
        public float getStringGuage(String string) {
            return guitarStringGuages.get(string);
        }
    
        public List<Pickup> getPickups() {
            return pickups;
        }
    
        public Style getStyle() {
            return style;
        }
    
        public File getCertificateOfAuthenticity() {
            return certificateOfAuthenticity;
        }
    }

## Style

    package org.superbiz.enventries;
    
    /**
     * @version $Revision$ $Date$
     */
    //START SNIPPET: code
    public enum Style {
    
        STANDARD,
        DELUX,
        VINTAGE;
    }

## StratocasterTest

    package org.superbiz.enventries;
    
    import junit.framework.TestCase;
    
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    import java.io.File;
    import java.text.DateFormat;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;
    
    import static java.util.Arrays.asList;
    
    /**
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    //START SNIPPET: code
    public class StratocasterTest extends TestCase {
    
        @EJB
        private Stratocaster strat;
    
        public void test() throws Exception {
            EJBContainer.createEJBContainer().getContext().bind("inject", this);
    
            Date date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).parse("Mar 1, 1962");
            assertEquals("Strat.getDateCreated()", date, strat.getDateCreated());
    
            List<Pickup> pickups = asList(Pickup.SINGLE_COIL, Pickup.SINGLE_COIL, Pickup.SINGLE_COIL);
            assertEquals("Strat.getPickups()", pickups, strat.getPickups());
    
            assertEquals("Strat.getStyle()", Style.VINTAGE, strat.getStyle());
    
            assertEquals("Strat.getStringGuage(\"E1\")", 0.052F, strat.getStringGuage("E1"));
            assertEquals("Strat.getStringGuage(\"A\")", 0.042F, strat.getStringGuage("A"));
            assertEquals("Strat.getStringGuage(\"D\")", 0.030F, strat.getStringGuage("D"));
            assertEquals("Strat.getStringGuage(\"G\")", 0.017F, strat.getStringGuage("G"));
            assertEquals("Strat.getStringGuage(\"B\")", 0.013F, strat.getStringGuage("B"));
            assertEquals("Strat.getStringGuage(\"E\")", 0.010F, strat.getStringGuage("E"));
    
            File file = new File("/tmp/strat-certificate.txt");
            assertEquals("Strat.getCertificateOfAuthenticity()", file, strat.getCertificateOfAuthenticity());
    
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.enventries.StratocasterTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/custom-injection
    INFO - openejb.base = /Users/dblevins/examples/custom-injection
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/custom-injection/target/classes
    INFO - Beginning load: /Users/dblevins/examples/custom-injection/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/custom-injection
    WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean Stratocaster: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.enventries.StratocasterTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/custom-injection" loaded.
    INFO - Assembling app: /Users/dblevins/examples/custom-injection
    INFO - Jndi(name="java:global/custom-injection/Stratocaster!org.superbiz.enventries.Stratocaster")
    INFO - Jndi(name="java:global/custom-injection/Stratocaster")
    INFO - Jndi(name="java:global/EjbModule1663626738/org.superbiz.enventries.StratocasterTest!org.superbiz.enventries.StratocasterTest")
    INFO - Jndi(name="java:global/EjbModule1663626738/org.superbiz.enventries.StratocasterTest")
    INFO - Created Ejb(deployment-id=Stratocaster, ejb-name=Stratocaster, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=org.superbiz.enventries.StratocasterTest, ejb-name=org.superbiz.enventries.StratocasterTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=Stratocaster, ejb-name=Stratocaster, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=org.superbiz.enventries.StratocasterTest, ejb-name=org.superbiz.enventries.StratocasterTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/custom-injection)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.11 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
