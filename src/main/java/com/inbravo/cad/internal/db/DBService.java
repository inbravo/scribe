package com.inbravo.cad.internal.db;

import java.sql.Connection;

import oracle.jdbc.pool.OracleDataSource;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface DBService {

  Connection getConnection() throws Exception;

  OracleDataSource getOracleDataSource() throws Exception;
}
