package org.openejb.alt.assembler.classic.xml;

import java.util.Properties;
import java.util.Vector;

import org.openejb.OpenEJBException;
import org.openejb.util.SafeToolkit;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DomTools{

    public static final SafeToolkit toolkit = SafeToolkit.getToolkit("XML configuration loader");

    public static final String PROPERTIES = "properties";

    public static final String PROPERTY = "property";

    public static final String PROPERTY_NAME = "property-name";

    public static final String PROPERTY_VALUE = "property-value";

    public static Properties readProperties(Node node) {
        Node propertiesElement = getChildElement(node, PROPERTIES);

        if(propertiesElement == null) return new Properties();

        Node[] property = getChildElements(propertiesElement,PROPERTY);
        Properties properties = new Properties();
        String name = null, value = null;

        for (int i=0; i< property.length; i++){
            name = getChildElementPCData(property[i], PROPERTY_NAME);
            value = getChildElementPCData(property[i], PROPERTY_VALUE);
            if (name == null || value == null) continue;
            properties.setProperty( name, value );
        }
        return properties;
    }

    public static final boolean debug = false;

    public static int debugRecursionDepth = 0;

    protected static DomObject[] collectChildElementsByType(Node node, Class classType, String elementType) throws OpenEJBException{

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.println(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        if (node == null) return null;

        NodeList list = node.getChildNodes();
        Vector tmp = new Vector();
        Node child = null;

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    DomObject info = (DomObject)toolkit.newInstance(classType);
                    tmp.addElement(info);
                    info.initializeFromDOM(element);
                }
            }
        }

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        DomObject[] domObjects = new DomObject[tmp.size()];
        tmp.copyInto(domObjects);
        return domObjects;
    }

    protected static DomObject collectChildElementByType(Node node, Class classType, String elementType) throws OpenEJBException{
        try{

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.println(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        NodeList list = node.getChildNodes();
        Node child = null;
        DomObject domObject = null;
        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    domObject = (DomObject)toolkit.newInstance(classType);
                    domObject.initializeFromDOM(element);
                    break;
                }
            }
        }

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        return domObject;
        } catch(Exception e){
            e.printStackTrace();
            throw new OpenEJBException(e);
        }

    }

    protected static String[] getChildElementsPCData(Node node, String elementType) {

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.print(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        NodeList list = node.getChildNodes();

        Node child = null;
        Vector tmp = new Vector();

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    tmp.addElement(getElementPCData(element));
                }
            }
        }
        String[] pcdata = new String[tmp.size()];
        tmp.copyInto(pcdata);

        if (debug){/*--------------------------------- * Debug Block * ------*/

            String tabs = "";
            for(int i=0;i<debugRecursionDepth;i++){tabs+="\t";}
            System.out.println(".length = "+pcdata.length);
            for(int i=0;i<pcdata.length;i++) System.out.println(tabs + node.getNodeName()+"."+ elementType + "["+ i +"] = " +pcdata[i]);
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        return pcdata;
    }

    protected static String getChildElementPCData(Node node, String elementType) {

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.print(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        NodeList list = node.getChildNodes();
        Node child = null;
        String pcdata = null;
        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    pcdata = getElementPCData(element);
                    break;
                }
            }
        }

        if (debug){/*--------------------------------- * Debug Block * ------*/
            System.out.println(" = "+pcdata);
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        return pcdata;
    }

    protected static String getElementPCData(Node node) {
        Node child = node.getFirstChild();
        if (child == null || child.getNodeType() != Node.TEXT_NODE) return null;

        try{
            Text text = (Text)child;
            String pcdata = text.getData();
            return (pcdata!=null)?pcdata.trim():null;
        } catch (DOMException e) {
            throw e;
        }
    }

    protected static Properties getElementAttributes(Node node){
        NamedNodeMap nodeMap = node.getAttributes();
        int size = nodeMap.getLength();
        Properties attributes = new Properties();
        for(int i = 0; i < size; i++){
            node = nodeMap.item(i);
            attributes.setProperty(node.getNodeName(), node.getNodeValue());
        }
        return attributes;
    }

    protected static Node getChildElement(Node node, String childName) {

        NodeList list = node.getChildNodes();
        Node child = null;

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(childName))
                    return child;
            }
        }
        return null;
    }

    protected static Node[] getChildElements(Node node, String childName) {

        NodeList list = node.getChildNodes();
        Node child = null;
        Vector tmp = new Vector();

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(childName)){
                    tmp.addElement(element);
                }
            }
        }

        Node[] children = new Node[tmp.size()];
        tmp.copyInto(children);
        return children;
    }

}