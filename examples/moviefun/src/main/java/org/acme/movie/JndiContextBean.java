package org.acme.movie;

import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.Context;
import javax.naming.NameParser;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.rmi.RemoteException;

/**
 * Nested EJB implementation class
 * Just delegates to this bean's java:comp/env namespace
 *
 * @author David Blevins <dblevins@visi.com>
 */
public class JndiContextBean implements SessionBean, JndiContext {
    public java.lang.Object lookup(Name name) throws NamingException {
        return context.lookup(name);
    }

    public java.lang.Object lookup(String name) throws NamingException {
        return context.lookup(name);
    }

    public void bind(Name name, java.lang.Object obj) throws NamingException {
        context.bind(name, obj);
    }

    public void bind(String name, java.lang.Object obj) throws NamingException {
        context.bind(name, obj);
    }

    public void rebind(Name name, java.lang.Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    public void rebind(String name, java.lang.Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    public void unbind(Name name) throws NamingException {
        context.unbind(name);
    }

    public void unbind(String name) throws NamingException {
        context.unbind(name);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        context.rename(oldName, newName);
    }

    public void rename(String oldName, String newName) throws NamingException {
        context.rename(oldName, newName);
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return context.list(name);
    }

    public NamingEnumeration list(String name) throws NamingException {
        return context.list(name);
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return context.listBindings(name);
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        return context.listBindings(name);
    }

    public void destroySubcontext(Name name) throws NamingException {
        context.destroySubcontext(name);
    }

    public void destroySubcontext(String name) throws NamingException {
        context.destroySubcontext(name);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return context.createSubcontext(name);
    }

    public Context createSubcontext(String name) throws NamingException {
        return context.createSubcontext(name);
    }

    public java.lang.Object lookupLink(Name name) throws NamingException {
        return context.lookupLink(name);
    }

    public java.lang.Object lookupLink(String name) throws NamingException {
        return context.lookupLink(name);
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return context.getNameParser(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
        return context.getNameParser(name);
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    public String composeName(String name, String prefix)
        throws NamingException {
        return context.composeName(name, prefix);
    }

    public java.lang.Object addToEnvironment(String propName, java.lang.Object propVal)
    throws NamingException {
        return context.addToEnvironment(propName, propVal);
    }

    public java.lang.Object removeFromEnvironment(String propName)
    throws NamingException {
        return context.removeFromEnvironment(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        return context.getEnvironment();
    }

    public void close() throws NamingException {
        context.close();
    }

    public String getNameInNamespace() throws NamingException {
        return context.getNameInNamespace();
    }

    private Context context;
    public void ejbCreate() throws CreateException{
        try {
            context = new InitialContext();
            context = (Context) context.lookup("java:comp/env");
        } catch (NamingException e) {
            throw (CreateException)new CreateException().initCause(e);
        }
    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
    }
}
