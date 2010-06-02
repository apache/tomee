package org.apache.openejb.core.asynch;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javax.ejb.AsyncResult;

import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;

/**
 * Abstract base class that will execute a bean in an
 * asynchronous fashion. This class should be subclassed
 * in the various containers (Stateless, Singleton, etc.)
 * that need to provide this functionality.
 * 
 * @author Matthew B. Jones
 *
 */
public abstract class AsynchMethodRunnable implements Callable<Object>{
	protected Method callMethod;
	protected Method runMethod;
	protected Object[] args;
	protected InterfaceType type;
	protected CoreDeploymentInfo deployInfo;
	protected Object primKey;
	
	public AsynchMethodRunnable(Method callMethod, Method runMethod, Object[] args, InterfaceType type, CoreDeploymentInfo deployInfo, Object primKey){
		this.callMethod = callMethod;
		this.runMethod = runMethod;
		this.args = args;
		this.type = type;
		this.deployInfo = deployInfo;
		this.primKey = primKey;
	}
	
	protected abstract Object performInvoke(Object bean, ThreadContext callContext) throws OpenEJBException;
	
	protected abstract Object createBean(ThreadContext callContext) throws OpenEJBException;
	
	protected abstract void releaseBean(Object bean, ThreadContext callContext) throws OpenEJBException;

	public Object call() throws Exception{
		ThreadContext callContext = new ThreadContext(this.deployInfo, this.primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
		Object bean = this.createBean(callContext);
		try{
			Object result = this.performInvoke(bean, callContext);
			if(result == null){
				return null;
			}else if(!(result instanceof AsyncResult)){
				// The bean isn't returning the right result!
				// TODO What should we do?
				System.err.println("Bad things happened!");
				return null;
			}else{
				AsyncResult asynchResult = (AsyncResult)result;
				return asynchResult.get();
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if (bean != null) {
                this.releaseBean(bean, callContext);
            }
            ThreadContext.exit(oldCallContext);
		}
	}

}
