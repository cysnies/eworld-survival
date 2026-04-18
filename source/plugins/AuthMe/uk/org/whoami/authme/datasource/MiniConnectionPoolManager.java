package uk.org.whoami.authme.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class MiniConnectionPoolManager {
   private ConnectionPoolDataSource dataSource;
   private int maxConnections;
   private long timeoutMs;
   private PrintWriter logWriter;
   private Semaphore semaphore;
   private PoolConnectionEventListener poolConnectionEventListener;
   private LinkedList recycledConnections;
   private int activeConnections;
   private boolean isDisposed;
   private boolean doPurgeConnection;
   private PooledConnection connectionInTransition;

   public MiniConnectionPoolManager(ConnectionPoolDataSource dataSource, int maxConnections) {
      this(dataSource, maxConnections, 60);
   }

   public MiniConnectionPoolManager(ConnectionPoolDataSource dataSource, int maxConnections, int timeout) {
      super();
      this.dataSource = dataSource;
      this.maxConnections = maxConnections;
      this.timeoutMs = (long)timeout * 1000L;

      try {
         this.logWriter = dataSource.getLogWriter();
      } catch (SQLException var5) {
      }

      if (maxConnections < 1) {
         throw new IllegalArgumentException("Invalid maxConnections value.");
      } else {
         this.semaphore = new Semaphore(maxConnections, true);
         this.recycledConnections = new LinkedList();
         this.poolConnectionEventListener = new PoolConnectionEventListener((PoolConnectionEventListener)null);
      }
   }

   public synchronized void dispose() throws SQLException {
      if (!this.isDisposed) {
         this.isDisposed = true;
         SQLException e = null;

         while(!this.recycledConnections.isEmpty()) {
            PooledConnection pconn = (PooledConnection)this.recycledConnections.remove();

            try {
               pconn.close();
            } catch (SQLException e2) {
               if (e == null) {
                  e = e2;
               }
            }
         }

         if (e != null) {
            throw e;
         }
      }
   }

   public Connection getConnection() throws SQLException {
      return this.getConnection2(this.timeoutMs);
   }

   private Connection getConnection2(long timeoutMs) throws SQLException {
      synchronized(this) {
         if (this.isDisposed) {
            throw new IllegalStateException("Connection pool has been disposed.");
         }
      }

      try {
         if (!this.semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException();
         }
      } catch (InterruptedException e) {
         throw new RuntimeException("Interrupted while waiting for a database connection.", e);
      }

      boolean ok = false;

      Connection var6;
      try {
         Connection conn = this.getConnection3();
         ok = true;
         var6 = conn;
      } finally {
         if (!ok) {
            this.semaphore.release();
         }

      }

      return var6;
   }

   private synchronized Connection getConnection3() throws SQLException {
      if (this.isDisposed) {
         throw new IllegalStateException("Connection pool has been disposed.");
      } else {
         PooledConnection pconn;
         if (!this.recycledConnections.isEmpty()) {
            pconn = (PooledConnection)this.recycledConnections.remove();
         } else {
            pconn = this.dataSource.getPooledConnection();
            pconn.addConnectionEventListener(this.poolConnectionEventListener);
         }

         Connection conn;
         try {
            this.connectionInTransition = pconn;
            ++this.activeConnections;
            conn = pconn.getConnection();
         } finally {
            this.connectionInTransition = null;
         }

         this.assertInnerState();
         return conn;
      }
   }

   public Connection getValidConnection() {
      long time = System.currentTimeMillis();
      long timeoutTime = time + this.timeoutMs;
      int triesWithoutDelay = this.getInactiveConnections() + 1;

      do {
         Connection conn = this.getValidConnection2(time, timeoutTime);
         if (conn != null) {
            return conn;
         }

         --triesWithoutDelay;
         if (triesWithoutDelay <= 0) {
            triesWithoutDelay = 0;

            try {
               Thread.sleep(250L);
            } catch (InterruptedException e) {
               throw new RuntimeException("Interrupted while waiting for a valid database connection.", e);
            }
         }

         time = System.currentTimeMillis();
      } while(time < timeoutTime);

      throw new TimeoutException("Timeout while waiting for a valid database connection.");
   }

   private Connection getValidConnection2(long time, long timeoutTime) {
      long rtime = Math.max(1L, timeoutTime - time);

      Connection conn;
      try {
         conn = this.getConnection2(rtime);
      } catch (SQLException var11) {
         return null;
      }

      rtime = timeoutTime - System.currentTimeMillis();
      int rtimeSecs = Math.max(1, (int)((rtime + 999L) / 1000L));

      try {
         if (conn.isValid(rtimeSecs)) {
            return conn;
         }
      } catch (SQLException var10) {
      }

      this.purgeConnection(conn);
      return null;
   }

   private synchronized void purgeConnection(Connection conn) {
      try {
         this.doPurgeConnection = true;
         conn.close();
      } catch (SQLException var6) {
      } finally {
         this.doPurgeConnection = false;
      }

   }

   private synchronized void recycleConnection(PooledConnection pconn) {
      if (!this.isDisposed && !this.doPurgeConnection) {
         if (this.activeConnections <= 0) {
            throw new AssertionError("AuthMeDatabaseError");
         } else {
            --this.activeConnections;
            this.semaphore.release();
            this.recycledConnections.add(pconn);
            this.assertInnerState();
         }
      } else {
         this.disposeConnection(pconn);
      }
   }

   private synchronized void disposeConnection(PooledConnection pconn) {
      pconn.removeConnectionEventListener(this.poolConnectionEventListener);
      if (!this.recycledConnections.remove(pconn) && pconn != this.connectionInTransition) {
         if (this.activeConnections <= 0) {
            throw new AssertionError("AuthMeDatabaseError");
         }

         --this.activeConnections;
         this.semaphore.release();
      }

      this.closeConnectionAndIgnoreException(pconn);
      this.assertInnerState();
   }

   private void closeConnectionAndIgnoreException(PooledConnection pconn) {
      try {
         pconn.close();
      } catch (SQLException e) {
         this.log("Error while closing database connection: " + e.toString());
      }

   }

   private void log(String msg) {
      String s = "MiniConnectionPoolManager: " + msg;

      try {
         if (this.logWriter == null) {
            System.err.println(s);
         } else {
            this.logWriter.println(s);
         }
      } catch (Exception var4) {
      }

   }

   private synchronized void assertInnerState() {
      if (this.activeConnections < 0) {
         throw new AssertionError("AuthMeDatabaseError");
      } else if (this.activeConnections + this.recycledConnections.size() > this.maxConnections) {
         throw new AssertionError("AuthMeDatabaseError");
      } else if (this.activeConnections + this.semaphore.availablePermits() > this.maxConnections) {
         throw new AssertionError("AuthMeDatabaseError");
      }
   }

   public synchronized int getActiveConnections() {
      return this.activeConnections;
   }

   public synchronized int getInactiveConnections() {
      return this.recycledConnections.size();
   }

   public static class TimeoutException extends RuntimeException {
      private static final long serialVersionUID = 1L;

      public TimeoutException() {
         super("Timeout while waiting for a free database connection.");
      }

      public TimeoutException(String msg) {
         super(msg);
      }
   }

   private class PoolConnectionEventListener implements ConnectionEventListener {
      private PoolConnectionEventListener() {
         super();
      }

      public void connectionClosed(ConnectionEvent event) {
         PooledConnection pconn = (PooledConnection)event.getSource();
         MiniConnectionPoolManager.this.recycleConnection(pconn);
      }

      public void connectionErrorOccurred(ConnectionEvent event) {
         PooledConnection pconn = (PooledConnection)event.getSource();
         MiniConnectionPoolManager.this.disposeConnection(pconn);
      }

      // $FF: synthetic method
      PoolConnectionEventListener(PoolConnectionEventListener var2) {
         this();
      }
   }
}
