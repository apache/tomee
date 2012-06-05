package org.superbiz.deltaspike.i18n;

import org.apache.deltaspike.core.api.message.annotation.MessageBundle;
import org.apache.deltaspike.core.api.message.annotation.MessageTemplate;

@MessageBundle
public interface MessageHelper {
    @MessageTemplate("{openejb.and.deltaspike}")
    String openejbAndDeltaspike();
}
