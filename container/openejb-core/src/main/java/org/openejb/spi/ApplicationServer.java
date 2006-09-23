package org.openejb.spi;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.openejb.ProxyInfo;

/**
 * <h2><b>LOCAL to REMOTE SERIALIZATION</b></h2> <p>
 *
 * <i>Definition:</i><p>
 *     This is a serialization that initiates in the local vm, but
 *     is outside the scope of a marked IntraVM local serialization.
 * <p>
 * <i>Circumstances:</i><p>
 *     When an IntraVM implementation of a javax.ejb.* interface is
 *     serialized outside the scope of the IntraVM Server
 *     <p>
 *     These serializations happen when objects are sent from a
 *     local bean to a remote client as part of a return value, or
 *     when a stateful session bean is passified.
 * <p>
 * <i>Action:</i><p>
 *     Don't serialize the IntraVM javax.ejb.* interface
 *     implementation, instead ask the ApplicationServer to nominate
 *     its own implementation as a replacement.  This is done via
 *     the org.openejb.spi.ApplicationServer interface.
 * <p>
 * <i>Example Scenario:</i><p>
 *     SERIALIZATION<br>
 * <br>1.  ObjectOutputStream encounters an IntraVmMetaData instance
 *         in the object graph and calls its writeReplace method.
 * <br>2.  The IntraVmMetaData instance determines it is being
 *         serialized outside the scope of an IntraVM serialization
 *         by calling IntraVmCopyMonitor.isIntraVmCopyOperation().
 * <br>3.  The IntraVmMetaData instance calls the getEJBMetaData
 *         method on the ApplicationServer.
 * <br>4.  The IntraVmMetaData instance returns the
 *         ApplicationServer's EJBMetaData instance from the
 *         writeReplace method.
 * <br>5.  The ObjectOutputStream serializes the ApplicationServer's
 *         EJBMetaData instance in place of the IntraVmMetaData
 *         instance.
 *         <p>
 *         Note:  The ApplicationServer's EJBMetaData instance can
 *         be any object that implements the javax.ejb.EJBMetaData
 *         interface and can also implement any serialization
 *         methods, such as the writeReplace method, to nominate a
 *         replacement or implement protocol specific logic or
 *         otherwise gain control over the serialization of
 *         EJBMetaData instances destined for its remote clients.
 *         <p>
 *     DESERIALIZATION<p>
 *         The deserialization of the Application Server's
 *         javax.ejb.* implementations is implementation specific.
 * <p>
 *
 * @version $Revision$ $Date$
 */
public interface ApplicationServer {

    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo);

    public Handle getHandle(ProxyInfo proxyInfo);

    public HomeHandle getHomeHandle(ProxyInfo proxyInfo);

    public EJBObject getEJBObject(ProxyInfo proxyInfo);

    public EJBHome getEJBHome(ProxyInfo proxyInfo);

}