package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
/*
  This class is used as a replacement when a IvmContext referenced by a stateful bean 
  is being serialized for passivation along with the bean.  It ensures that the entire
  JNDI ENC graph is not serialized with the bean and returns a reference to the correct
  IvmContext when its deserialized.

  Stateful beans are activated by a thread with the relavent DeploymentInfo object in the 
  ThreadContext which makes it possible to lookup the correct IvmContext and swap in place of 
  this object.
*/

public class JndiEncArtifact implements java.io.Serializable {
    String path = new String();

    public JndiEncArtifact(IvmContext context) {
        NameNode node = context.mynode;
        do {
            path = node.atomicName + "/" + path;
            node = node.parent;
        } while (node != null);
    }

    public Object readResolve() throws java.io.ObjectStreamException {
        ThreadContext thrdCntx = ThreadContext.getThreadContext();
        CoreDeploymentInfo deployment = thrdCntx.getDeploymentInfo();
        javax.naming.Context cntx = deployment.getJndiEnc();
        try {
            Object obj = cntx.lookup(path);
            if (obj == null)
                throw new java.io.InvalidObjectException("JNDI ENC context reference could not be properly resolved when bean instance was activated");
            return obj;
        } catch (javax.naming.NamingException e) {
            throw new java.io.InvalidObjectException("JNDI ENC context reference could not be properly resolved due to a JNDI exception, when bean instance was activated");
        }
    }

}