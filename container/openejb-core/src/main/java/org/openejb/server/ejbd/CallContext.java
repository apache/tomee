package org.openejb.server.ejbd;

import org.openejb.DeploymentInfo;

import org.openejb.client.EJBRequest;

import org.openejb.util.FastThreadLocal;

public class CallContext {

    protected static final FastThreadLocal threads = new FastThreadLocal();

    protected DeploymentInfo deploymentInfo;

    protected EJBRequest request;

    public CallContext(){

    }

    public void reset() {

        deploymentInfo = null;

        request        = null;

    }

    public DeploymentInfo getDeploymentInfo() {

        return deploymentInfo;

    }

    public void setDeploymentInfo(DeploymentInfo info) {

        deploymentInfo = info;

    }

    public EJBRequest getEJBRequest(){

        return request;

    }

    public void setEJBRequest(EJBRequest request){

        this.request = request;

    }

    public static void setCallContext(CallContext ctx) {

        if ( ctx == null ) {

            ctx = (CallContext)threads.get();

            if ( ctx != null ) ctx.reset();

        } else {

            threads.set( ctx );

        }

    }

    public static CallContext getCallContext( ) {

        CallContext ctx = (CallContext)threads.get();

        if ( ctx == null ) {

            ctx = new CallContext();

            threads.set( ctx );

        }

        return ctx;

    }

}

