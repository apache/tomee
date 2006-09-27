package org.apache.openejb.test.entity.bmp;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BasicBmp2DataSourcesBean implements javax.ejb.EntityBean{
    
    public static int primaryKey = 1;
    public String firstName;
    public String lastName;
    public EntityContext ejbContext;
    public Hashtable allowedOperationsTable = new Hashtable();
    
    
    //=============================
    // Home interface methods
    //    
    
    /**
     * Maps to BasicBmp2DataSourcesHome.sum
     * 
     * Adds x and y and returns the result.
     * 
     * @param one
     * @param two
     * @return x + y
     * @see BasicBmp2DataSourcesHome#sum
     */
    public int ejbHomeSum(int x, int y) {
        testAllowedOperations("ejbHome");
        return x+y;
    }
    
    
    /**
     * Maps to BasicBmp2DataSourcesHome.findEmptyCollection
     *
     * @return Collection
     * @exception javax.ejb.FinderException
     */
    public java.util.Collection ejbFindEmptyCollection()
    throws javax.ejb.FinderException, java.rmi.RemoteException {
        return new java.util.Vector();
    }

    /**
     * Maps to BasicBmp2DataSourcesHome.findByPrimaryKey
     * 
     * @param primaryKey
     * @return Integer
     * @exception javax.ejb.FinderException
     */
    public Integer ejbFindByPrimaryKey(Integer primaryKey)
    throws javax.ejb.FinderException{
        boolean found = false;
        try{
            InitialContext jndiContext = new InitialContext( ); 
            DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            Connection con = ds.getConnection();
            
            PreparedStatement stmt = con.prepareStatement("select * from entity where id = ?");
            stmt.setInt(1, primaryKey.intValue());
            found = stmt.executeQuery().next();
            con.close();
        }catch(Exception e){
            throw new FinderException("FindByPrimaryKey failed");
        }
        
        if(found) return primaryKey;
        else      throw new javax.ejb.ObjectNotFoundException();
    }

    /**
     * Maps to BasicBmp2DataSourcesHome.create
     * 
     * @param name
     * @return Integer
     * @exception javax.ejb.CreateException
     */
    public Integer ejbCreate(String name)
    throws javax.ejb.CreateException{
        try{
        StringTokenizer st = new StringTokenizer(name, " ");    
        firstName = st.nextToken();
        lastName = st.nextToken();
        
        InitialContext jndiContext = new InitialContext( ); 
 
        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
        
        Connection con = ds.getConnection();
        
        // Support for Oracle because Oracle doesn't do auto increment
        PreparedStatement stmt = con.prepareStatement("insert into entity (id, first_name, last_name) values (?,?,?)");
        stmt.setInt(1, primaryKey++);
        stmt.setString(2, firstName);
        stmt.setString(3, lastName);
        stmt.executeUpdate();
        
        stmt = con.prepareStatement("select id from entity where first_name = ? AND last_name = ?");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        ResultSet set = stmt.executeQuery();
        while(set.next()) primaryKey = set.getInt("id");
        con.close();
        
        // Do backup
        ds = (DataSource)jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabaseBackup");
        
        con = ds.getConnection();
        
        // Support for Oracle because Oracle doesn't do auto increment
        stmt = con.prepareStatement("insert into entityBackup (id, first_name, last_name) values (?,?,?)");
        stmt.setInt(1, primaryKey);
        stmt.setString(2, firstName);
        stmt.setString(3, lastName);
        stmt.executeUpdate();
        
        con.close();
        
        return new Integer(primaryKey);
        
        }catch(Exception e){
            e.printStackTrace();
            throw new javax.ejb.CreateException("can't create");
        }
    }
    
    public void ejbPostCreate(String name)
    throws javax.ejb.CreateException{
    }
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    /**
     * Maps to BasicBmp2DataSourcesObject.businessMethod
     * 
     * @return String
     */
    public String businessMethod(String text){
        testAllowedOperations("businessMethod");
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    
    /**
     * Maps to BasicBmp2DataSourcesObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     * 
     * @return null
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicBmp2DataSourcesObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return OperationPolicy 
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }
    
    //    
    // Remote interface methods
    //=============================


    //================================
    // EntityBean interface methods
    //    
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by loading it state from the
     * underlying database.
     */
    public void ejbLoad() throws EJBException,RemoteException {
        try{
        InitialContext jndiContext = new InitialContext( ); 
        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
        Connection con = ds.getConnection();
        
        PreparedStatement stmt = con.prepareStatement("select * from entity where id = ?");
        Integer primaryKey = (Integer)ejbContext.getPrimaryKey();
        stmt.setInt(1, primaryKey.intValue());
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            lastName = rs.getString("last_name");
            firstName = rs.getString("first_name");
        }
        con.close();
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(EntityContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setEntityContext");
    }
    
    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() throws EJBException,RemoteException {
        testAllowedOperations("unsetEntityContext");
    }
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() throws EJBException,RemoteException {
        try{
        InitialContext jndiContext = new InitialContext( ); 
        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
        Connection con = ds.getConnection();
        
        PreparedStatement stmt = con.prepareStatement("update entity set first_name = ?, last_name = ? where EmployeeID = ?");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setInt(3, primaryKey);
        stmt.execute();
        con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() throws RemoveException,EJBException,RemoteException {
        try{
            InitialContext jndiContext = new InitialContext( ); 
            DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            Connection con = ds.getConnection();
            
            PreparedStatement stmt = con.prepareStatement("delete from entity where id = ?");
            Integer primaryKey = (Integer)ejbContext.getPrimaryKey();
            stmt.setInt(1, primaryKey.intValue());
            stmt.executeUpdate();
            con.close();
        
        }catch(Exception e){
            e.printStackTrace();
            throw new javax.ejb.EJBException(e);
        }
    }
    
    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbActivate");
    }
    
    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbPassivate");
    }
    //    
    // EntityBean interface methods
    //================================
    
    protected void testAllowedOperations(String methodName){
        OperationsPolicy policy = new OperationsPolicy();
        
        /*[1] Test getEJBHome /////////////////*/ 
        try{
            ejbContext.getEJBHome();
            policy.allow(policy.Context_getEJBHome);
        }catch(IllegalStateException ise){}
        
        /*[2] Test getCallerPrincipal /////////*/ 
        try{
            ejbContext.getCallerPrincipal();
            policy.allow( policy.Context_getCallerPrincipal );
        }catch(IllegalStateException ise){}
        
        /*[3] Test isCallerInRole /////////////*/ 
        try{
            ejbContext.isCallerInRole("ROLE");
            policy.allow( policy.Context_isCallerInRole );
        }catch(IllegalStateException ise){}
        
        /*[4] Test getRollbackOnly ////////////*/ 
        try{
            ejbContext.getRollbackOnly();
            policy.allow( policy.Context_getRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[5] Test setRollbackOnly ////////////*/ 
        try{
            ejbContext.setRollbackOnly();
            policy.allow( policy.Context_setRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[6] Test getUserTransaction /////////*/ 
        try{
            ejbContext.getUserTransaction();
            policy.allow( policy.Context_getUserTransaction );
        }catch(Exception e){}
        
        /*[7] Test getEJBObject ///////////////*/ 
        try{
            ejbContext.getEJBObject();
            policy.allow( policy.Context_getEJBObject );
        }catch(IllegalStateException ise){}

        /*[8] Test getPrimaryKey //////////////*/ 
        try{
            ejbContext.getPrimaryKey();
            policy.allow( policy.Context_getPrimaryKey );
        }catch(IllegalStateException ise){}
         
        /* TO DO:  
         * Check for policy.Enterprise_bean_access       
         * Check for policy.JNDI_access_to_java_comp_env 
         * Check for policy.Resource_manager_access      
         */
        allowedOperationsTable.put(methodName, policy);
    }

}
