package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.MethodInfo;
import org.w3c.dom.Node;

public class Method extends MethodInfo implements DomObject{

    public static final String DESCRIPTION = "description";

    public static final String EJB_DEPLOYMENT_ID = "ejb-deployment-id";

    public static final String METHOD_INTF = "method-intf";

    public static final String METHOD_NAME = "method-name";

    public static final String METHOD_PARAMS = "method-params";

    public static final String METHOD_PARAM = "method-param";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        ejbDeploymentId = DomTools.getChildElementPCData(node, EJB_DEPLOYMENT_ID);
        methodIntf = DomTools.getChildElementPCData(node, METHOD_INTF);
        methodName = DomTools.getChildElementPCData(node, METHOD_NAME);

        Node methodParamsElement = DomTools.getChildElement(node, METHOD_PARAMS);
        if (methodParamsElement == null) methodParams = null;
        else{
            methodParams = DomTools.getChildElementsPCData(methodParamsElement, METHOD_PARAM);

        }

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}

    private java.lang.Class getClassForParam(java.lang.String className)
      throws Exception {

      if(className.equals("int")) {
        return java.lang.Integer.TYPE; 
      }
      else if(className.equals("double")) {
        return java.lang.Double.TYPE; 
      }
      else if(className.equals("long")) {
        return java.lang.Long.TYPE; 
      } 
      else if(className.equals("boolean")) {
        return java.lang.Boolean.TYPE; 
      } 
      else if(className.equals("float")) {
        return java.lang.Float.TYPE; 
      } 
      else if(className.equals("char")) {
        return java.lang.Character.TYPE; 
      }
      else if(className.equals("short")) {
        return java.lang.Short.TYPE; 
      }
      else if(className.equals("byte")) {
        return java.lang.Byte.TYPE; 
      }     
      else return Class.forName(className); 

    } 
}
