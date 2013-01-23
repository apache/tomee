package org.apache.openejb.util;

import org.apache.openejb.loader.SystemInstance;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertyPlaceHolderTest {
    @Test
    public void simpleReplace() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest", "ok");

        final String foo = PropertyPlaceHolderHelper.simpleValue("${PropertyPlaceHolderTest}");
        assertEquals("ok", foo);

        SystemInstance.get().getProperties().remove("PropertyPlaceHolderTest");
    }

    @Test
    public void composedReplace() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest1", "uno");
        SystemInstance.get().setProperty("PropertyPlaceHolderTest2", "due");

        final String foo = PropertyPlaceHolderHelper.simpleValue("jdbc://${PropertyPlaceHolderTest1}/${PropertyPlaceHolderTest2}");
        assertEquals("jdbc://uno/due", foo);

        SystemInstance.get().getProperties().remove("PropertyPlaceHolderTest1");
        SystemInstance.get().getProperties().remove("PropertyPlaceHolderTest2");
    }
}
