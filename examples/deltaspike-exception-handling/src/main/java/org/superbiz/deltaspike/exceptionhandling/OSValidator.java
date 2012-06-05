package org.superbiz.deltaspike.exceptionhandling;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;

public class OSValidator {
    @Inject
    private Event<ExceptionToCatchEvent> exceptionEvent;

    public void validOS(final String os) {
        if (os.toLowerCase().contains("win")) {
            final Exception ex = new OSRuntimeException(); // can be a caugh exceptino
            exceptionEvent.fire(new ExceptionToCatchEvent(ex));
        }
    }
}
