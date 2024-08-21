package org.apache.openejb.threads.future;

import jakarta.enterprise.concurrent.SkippedException;
import org.apache.openejb.threads.task.ManagedTaskListenerTask;
import org.apache.openejb.threads.task.TriggerTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Delegates isDone calls to TriggerTask and throws SkippedExceptions in get methods if task execution has been skipped
 * @param <V>
 */
public class CUTriggerScheduledFuture<V> extends CUScheduledFuture<V> {
    private final TriggerTask<V> task;

    public CUTriggerScheduledFuture(ScheduledFuture<V> delegate, ManagedTaskListenerTask listener, TriggerTask<V> task) {
        super(delegate, listener);
        this.task = task;
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        V result = super.get();
        if (task.isSkipped()) {
            throw new SkippedException();
        }

        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        V result = super.get(timeout, unit);
        if (task.isSkipped()) {
            throw new SkippedException();
        }

        return result;
    }
}
