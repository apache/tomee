/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;
import org.apache.openejb.jee.ContextService;
import org.apache.openejb.jee.ManagedExecutor;
import org.apache.openejb.jee.ManagedScheduledExecutor;
import org.apache.openejb.jee.ManagedThreadFactory;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jba.JndiName;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies {@link AnnotationDeployer.ProcessAnnotatedBeans#buildAnnotatedRefs} applies the
 * DD-wins merging rule (Jakarta EE platform §EE.5.2.5) when an annotation and a deployment
 * descriptor entry share a name: the DD value is retained for every attribute, and the
 * annotation only fills attributes the DD left unset.
 */
public class ConcurrencyDefinitionDDOverrideTest {

    // ---------------- ContextService ----------------

    @Test
    public void contextServiceDDOverridesAnnotation() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", CSBean.class.getName());

        final ContextService dd = new ContextService();
        dd.setName(jndi("java:app/concurrent/CS"));
        dd.getPropagated().add("StringContext");
        dd.getCleared().add("IntContext");
        dd.getUnchanged().add("Transaction");
        dd.getQualifier().add(DDQualifier.class.getName());
        bean.getContextServiceMap().put("java:app/concurrent/CS", dd);

        buildAnnotatedRefs(bean, CSBean.class);

        final ContextService merged = bean.getContextServiceMap().get("java:app/concurrent/CS");
        assertEquals("java:app/concurrent/CS", merged.getName().getvalue());
        assertEquals(List.of("StringContext"), merged.getPropagated());
        assertEquals(List.of("IntContext"), merged.getCleared());
        assertEquals(List.of("Transaction"), merged.getUnchanged());
        assertEquals(List.of(DDQualifier.class.getName()), merged.getQualifier());
    }

    @Test
    public void contextServiceAnnotationFillsMissingDDFields() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", CSBean.class.getName());
        buildAnnotatedRefs(bean, CSBean.class);

        final ContextService merged = bean.getContextServiceMap().get("java:app/concurrent/CS");
        assertEquals(List.of(ContextServiceDefinition.APPLICATION), merged.getPropagated());
        assertEquals(List.of(ContextServiceDefinition.TRANSACTION), merged.getCleared());
        assertTrue(merged.getUnchanged().isEmpty());
        assertEquals(List.of(AnnoQualifier.class.getName()), merged.getQualifier());
    }

    // ---------------- ManagedExecutor ----------------

    @Test
    public void managedExecutorDDOverridesAnnotation() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", MEBean.class.getName());

        final ManagedExecutor dd = new ManagedExecutor();
        dd.setName(jndi("java:app/concurrent/MES"));
        dd.setContextService(jndi("java:app/concurrent/DDCtx"));
        dd.setHungTaskThreshold(100_000L);
        dd.setMaxAsync(5);
        dd.setVirtual(Boolean.FALSE);
        dd.getQualifier().add(DDQualifier.class.getName());
        bean.getManagedExecutorMap().put("java:app/concurrent/MES", dd);

        buildAnnotatedRefs(bean, MEBean.class);

        final ManagedExecutor merged = bean.getManagedExecutorMap().get("java:app/concurrent/MES");
        assertEquals("java:app/concurrent/MES", merged.getName().getvalue());
        assertEquals("java:app/concurrent/DDCtx", merged.getContextService().getvalue());
        assertEquals(Long.valueOf(100_000L), merged.getHungTaskThreshold());
        assertEquals(Integer.valueOf(5), merged.getMaxAsync());
        assertEquals(Boolean.FALSE, merged.getVirtual());
        assertEquals(List.of(DDQualifier.class.getName()), merged.getQualifier());
    }

    @Test
    public void managedExecutorAnnotationFillsMissingDDFields() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", MEBean.class.getName());
        buildAnnotatedRefs(bean, MEBean.class);

        final ManagedExecutor merged = bean.getManagedExecutorMap().get("java:app/concurrent/MES");
        assertEquals("java:app/concurrent/AnnoCtx", merged.getContextService().getvalue());
        assertEquals(Long.valueOf(200_000L), merged.getHungTaskThreshold());
        assertEquals(Integer.valueOf(3), merged.getMaxAsync());
        assertEquals(Boolean.TRUE, merged.getVirtual());
        assertEquals(List.of(AnnoQualifier.class.getName()), merged.getQualifier());
    }

    // ---------------- ManagedScheduledExecutor ----------------

    @Test
    public void managedScheduledExecutorDDOverridesAnnotation() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", MSESBean.class.getName());

        final ManagedScheduledExecutor dd = new ManagedScheduledExecutor();
        dd.setName(jndi("java:app/concurrent/MSES"));
        dd.setContextService(jndi("java:app/concurrent/DDCtx"));
        dd.setHungTaskThreshold(150_000L);
        dd.setMaxAsync(4);
        dd.setVirtual(Boolean.FALSE);
        dd.getQualifier().add(DDQualifier.class.getName());
        bean.getManagedScheduledExecutorMap().put("java:app/concurrent/MSES", dd);

        buildAnnotatedRefs(bean, MSESBean.class);

        final ManagedScheduledExecutor merged = bean.getManagedScheduledExecutorMap().get("java:app/concurrent/MSES");
        assertEquals("java:app/concurrent/DDCtx", merged.getContextService().getvalue());
        assertEquals(Long.valueOf(150_000L), merged.getHungTaskThreshold());
        assertEquals(Integer.valueOf(4), merged.getMaxAsync());
        assertEquals(Boolean.FALSE, merged.getVirtual());
        assertEquals(List.of(DDQualifier.class.getName()), merged.getQualifier());
    }

    @Test
    public void managedScheduledExecutorAnnotationFillsMissingDDFields() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", MSESBean.class.getName());
        buildAnnotatedRefs(bean, MSESBean.class);

        final ManagedScheduledExecutor merged = bean.getManagedScheduledExecutorMap().get("java:app/concurrent/MSES");
        assertEquals("java:app/concurrent/AnnoCtx", merged.getContextService().getvalue());
        assertEquals(Long.valueOf(250_000L), merged.getHungTaskThreshold());
        assertEquals(Integer.valueOf(2), merged.getMaxAsync());
        assertEquals(Boolean.TRUE, merged.getVirtual());
        assertEquals(List.of(AnnoQualifier.class.getName()), merged.getQualifier());
    }

    // ---------------- ManagedThreadFactory ----------------

    @Test
    public void managedThreadFactoryDDOverridesAnnotation() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", MTFBean.class.getName());

        final ManagedThreadFactory dd = new ManagedThreadFactory();
        dd.setName(jndi("java:app/concurrent/MTF"));
        dd.setContextService(jndi("java:app/concurrent/DDCtx"));
        dd.setPriority(7);
        dd.setVirtual(Boolean.FALSE);
        dd.getQualifier().add(DDQualifier.class.getName());
        bean.getManagedThreadFactoryMap().put("java:app/concurrent/MTF", dd);

        buildAnnotatedRefs(bean, MTFBean.class);

        final ManagedThreadFactory merged = bean.getManagedThreadFactoryMap().get("java:app/concurrent/MTF");
        assertEquals("java:app/concurrent/DDCtx", merged.getContextService().getvalue());
        assertEquals(Integer.valueOf(7), merged.getPriority());
        assertEquals(Boolean.FALSE, merged.getVirtual());
        assertEquals(List.of(DDQualifier.class.getName()), merged.getQualifier());
    }

    @Test
    public void managedThreadFactoryAnnotationFillsMissingDDFields() throws Exception {
        final StatelessBean bean = new StatelessBean("Bean", MTFBean.class.getName());
        buildAnnotatedRefs(bean, MTFBean.class);

        final ManagedThreadFactory merged = bean.getManagedThreadFactoryMap().get("java:app/concurrent/MTF");
        assertEquals("java:app/concurrent/AnnoCtx", merged.getContextService().getvalue());
        assertEquals(Integer.valueOf(8), merged.getPriority());
        assertEquals(Boolean.TRUE, merged.getVirtual());
        assertEquals(List.of(AnnoQualifier.class.getName()), merged.getQualifier());
    }

    // ---------------- helpers ----------------

    private static void buildAnnotatedRefs(final StatelessBean bean, final Class<?> beanClass) throws Exception {
        final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(beanClass)).link();
        new AnnotationDeployer.ProcessAnnotatedBeans(false)
                .buildAnnotatedRefs(bean, finder, beanClass.getClassLoader());
    }

    private static JndiName jndi(final String value) {
        final JndiName n = new JndiName();
        n.setvalue(value);
        return n;
    }

    @Qualifier
    @Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DDQualifier {
    }

    @Qualifier
    @Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnoQualifier {
        @Nonbinding String value() default "";
    }

    @ContextServiceDefinition(
            name = "java:app/concurrent/CS",
            propagated = ContextServiceDefinition.APPLICATION,
            cleared = ContextServiceDefinition.TRANSACTION,
            qualifiers = AnnoQualifier.class)
    public static class CSBean {
    }

    @ManagedExecutorDefinition(
            name = "java:app/concurrent/MES",
            context = "java:app/concurrent/AnnoCtx",
            hungTaskThreshold = 200_000,
            maxAsync = 3,
            virtual = true,
            qualifiers = AnnoQualifier.class)
    public static class MEBean {
    }

    @ManagedScheduledExecutorDefinition(
            name = "java:app/concurrent/MSES",
            context = "java:app/concurrent/AnnoCtx",
            hungTaskThreshold = 250_000,
            maxAsync = 2,
            virtual = true,
            qualifiers = AnnoQualifier.class)
    public static class MSESBean {
    }

    @ManagedThreadFactoryDefinition(
            name = "java:app/concurrent/MTF",
            context = "java:app/concurrent/AnnoCtx",
            priority = 8,
            virtual = true,
            qualifiers = AnnoQualifier.class)
    public static class MTFBean {
    }
}
