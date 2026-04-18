package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class QueryRunner {
   private volatile boolean pmdKnownBroken = false;

   public QueryRunner() {
      super();
   }

   private void close(Connection conn) throws SQLException {
      if (conn != null) {
         conn.close();
      }

   }

   private void close(ResultSet rs) throws SQLException {
      if (rs != null) {
         rs.close();
      }

   }

   private void close(Statement stmt) throws SQLException {
      if (stmt != null) {
         stmt.close();
      }

   }

   private void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {
      ParameterMetaData pmd = null;
      if (!this.pmdKnownBroken) {
         pmd = stmt.getParameterMetaData();
         int stmtCount = pmd.getParameterCount();
         int paramsCount = params == null ? 0 : params.length;
         if (stmtCount != paramsCount) {
            throw new SQLException("Wrong number of parameters: expected " + stmtCount + ", was given " + paramsCount);
         }
      }

      if (params != null) {
         for(int i = 0; i < params.length; ++i) {
            if (params[i] != null) {
               stmt.setObject(i + 1, params[i]);
            } else {
               int sqlType = 12;
               if (!this.pmdKnownBroken) {
                  try {
                     sqlType = pmd.getParameterType(i + 1);
                  } catch (SQLException var7) {
                     this.pmdKnownBroken = true;
                  }
               }

               stmt.setNull(i + 1, sqlType);
            }
         }

      }
   }

   private Object query(Connection conn, boolean closeConn, String sql, ResultSetHandler rsh, Object... params) throws SQLException {
      if (conn == null) {
         throw new SQLException("Null connection");
      } else if (sql == null) {
         if (closeConn) {
            this.close(conn);
         }

         throw new SQLException("Null SQL statement");
      } else if (rsh == null) {
         if (closeConn) {
            this.close(conn);
         }

         throw new SQLException("Null ResultSetHandler");
      } else {
         PreparedStatement stmt = null;
         ResultSet rs = null;
         T result = (T)null;

         try {
            stmt = conn.prepareStatement(sql);
            this.fillStatement(stmt, params);
            rs = stmt.executeQuery();
            result = (T)rsh.handle(rs);
         } catch (SQLException e) {
            this.rethrow(e, sql, params);
         } finally {
            try {
               this.close(rs);
            } finally {
               this.close((Statement)stmt);
               if (closeConn) {
                  this.close(conn);
               }

            }
         }

         return result;
      }
   }

   public Object query(Connection conn, String sql, ResultSetHandler rsh, Object... params) throws SQLException {
      return this.query(conn, false, sql, rsh, params);
   }

   private void rethrow(SQLException cause, String sql, Object... params) throws SQLException {
      String causeMessage = cause.getMessage();
      if (causeMessage == null) {
         causeMessage = "";
      }

      StringBuilder msg = new StringBuilder(causeMessage);
      msg.append(" Query: ");
      msg.append(sql);
      msg.append(" Parameters: ");
      if (params == null) {
         msg.append("[]");
      } else {
         msg.append(Arrays.deepToString(params));
      }

      SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
      e.setNextException(cause);
      throw e;
   }

   private int update(Connection conn, boolean closeConn, String sql, Object... params) throws SQLException {
      if (conn == null) {
         throw new SQLException("Null connection");
      } else if (sql == null) {
         if (closeConn) {
            this.close(conn);
         }

         throw new SQLException("Null SQL statement");
      } else {
         PreparedStatement stmt = null;
         int rows = 0;

         try {
            stmt = conn.prepareStatement(sql);
            this.fillStatement(stmt, params);
            rows = stmt.executeUpdate();
         } catch (SQLException e) {
            this.rethrow(e, sql, params);
         } finally {
            this.close((Statement)stmt);
            if (closeConn) {
               this.close(conn);
            }

         }

         return rows;
      }
   }

   public int update(Connection conn, String sql, Object param) throws SQLException {
      return this.update(conn, false, sql, param);
   }

   public int update(Connection conn, String sql, Object... params) throws SQLException {
      return this.update(conn, false, sql, params);
   }
}
