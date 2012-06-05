package org.superbiz.deltaspike.exceptionhandling;

import java.util.logging.Logger;
import org.apache.deltaspike.core.api.exception.control.annotation.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.annotation.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;

@ExceptionHandler
public class OSExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(OSExceptionHandler.class.getName());

    public void printExceptions(@Handles final ExceptionEvent<OSRuntimeException> evt) {
        LOGGER.severe("==> a bad OS was mentionned");
        evt.handled();
    }
}
