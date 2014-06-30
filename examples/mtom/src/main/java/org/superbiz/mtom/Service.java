package org.superbiz.mtom;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.MTOM;

@WebService
@SOAPBinding(use = Use.LITERAL, parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT)
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING)
@MTOM
public interface Service {

    Response convertToBytes(Request request);

}
