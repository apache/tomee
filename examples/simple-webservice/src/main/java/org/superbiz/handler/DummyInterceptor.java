package org.superbiz.handler;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class DummyInterceptor implements SOAPHandler<SOAPMessageContext> {
	public DummyInterceptor() {
		super();
	}

	public Set<QName> getHeaders() {
		return Collections.emptySet();
	}

	public void close(MessageContext mc) {
	}

	public boolean handleFault(SOAPMessageContext mc) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext mc) {
		return true;
	}
}