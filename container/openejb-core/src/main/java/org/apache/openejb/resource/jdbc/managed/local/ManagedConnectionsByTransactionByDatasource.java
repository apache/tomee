package org.apache.openejb.resource.jdbc.managed.local;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.CommonDataSource;
import javax.transaction.Transaction;

public class ManagedConnectionsByTransactionByDatasource {
    private static final Map<Integer, Map<Transaction, Connection>> CONNECTION_BY_TX_BY_DS = new ConcurrentHashMap<Integer, Map<Transaction, Connection>>();
    
    public static void pushDataSource(final CommonDataSource ds) {
        CONNECTION_BY_TX_BY_DS.put(ds.hashCode(), new ConcurrentHashMap<Transaction, Connection>());
    }
    
    public static void cleanDataSource(final CommonDataSource ds) {
        final Map<Transaction, Connection> map = CONNECTION_BY_TX_BY_DS.remove(ds.hashCode());
        if (map != null) {
            map.clear();
        }
    }
    
    public static Map<Transaction, Connection> get(final CommonDataSource ds) {
        return CONNECTION_BY_TX_BY_DS.get(ds.hashCode());
    }
    
    public static Connection get(final CommonDataSource ds, final Transaction tx) {
        final Map<Transaction, Connection> connectionsByTransactions = get(ds);
        if (connectionsByTransactions == null) {
            return null;
        }
        
        final Connection connection = connectionsByTransactions.get(tx);
        return connection;
    }
}
