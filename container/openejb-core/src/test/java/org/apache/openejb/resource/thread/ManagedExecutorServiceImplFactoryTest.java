package org.apache.openejb.resource.thread;


import jakarta.enterprise.concurrent.ManagedThreadFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ContextServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagedExecutorServiceImplFactoryTest {

    public static AtomicInteger threadCount = new AtomicInteger(0);

    @BeforeClass
    public static void forceSecurityService() {
        SystemInstance.get().setComponent(SecurityService.class, new PseudoSecurityService());
    }

    @Test
    public void testExecutorServiceFactoryWithQueue() throws Exception {
        // we expect two core threads to be used, and everything else to queue up

        final ManagedExecutorServiceImplFactory factory = new ManagedExecutorServiceImplFactory();
        factory.setCore(2);
        factory.setMax(4);
        factory.setQueue(1000);
        factory.setThreadFactory("org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactoryTest$MyThreadFactory");

        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();

        final ManagedExecutorServiceImpl executorService = factory.create(contextService);

        final CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(0, latch.getCount());
        Assert.assertEquals(2, executorService.getLargestPoolSize().longValue());
    }

    @Test
    public void testExecutorServiceFactoryWithoutQueue() throws Exception {
        final ManagedExecutorServiceImplFactory factory = new ManagedExecutorServiceImplFactory();
        factory.setCore(2);
        factory.setMax(4);
        factory.setQueue(0);
        factory.setThreadFactory("org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactoryTest$MyThreadFactory");

        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();

        final ManagedExecutorServiceImpl executorService = factory.create(contextService);

        final CountDownLatch latch = new CountDownLatch(10);
        final AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            try {
                executorService.execute(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        latch.countDown();
                    }
                });
            } catch (RejectedExecutionException e) {
                rejectedCount.incrementAndGet();
            }
        }

        latch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(6, latch.getCount());
        Assert.assertEquals(4, executorService.getLargestPoolSize().longValue());
        Assert.assertEquals(6, rejectedCount.get());
    }

    @Test
    public void testExecutorServiceFactoryWithoutScalingThePool() throws Exception {
        final ManagedExecutorServiceImplFactory factory = new ManagedExecutorServiceImplFactory();
        factory.setCore(2);
        factory.setMax(4);
        factory.setQueue(-1);
        factory.setThreadFactory("org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactoryTest$MyThreadFactory");

        final ContextServiceImpl contextService = ContextServiceImplFactory.newDefaultContextService();

        final ManagedExecutorServiceImpl executorService = factory.create(contextService);

        final CountDownLatch latch = new CountDownLatch(10);
        final AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            try {
                executorService.execute(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        latch.countDown();
                    }
                });
            } catch (RejectedExecutionException e) {
                rejectedCount.incrementAndGet();
            }
        }

        latch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(0, latch.getCount());
        Assert.assertEquals(2, executorService.getLargestPoolSize().longValue());
        Assert.assertEquals(0, rejectedCount.get());
    }


    public static class MyThreadFactory implements ManagedThreadFactory, ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "tpe-" + threadCount.getAndIncrement());
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return null;
        }
    }



}