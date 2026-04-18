package ticket;

import java.io.File;
import java.util.List;
import lib.hashList.HashList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Ticket moral) {
      super();
      Configuration config = (new Configuration()).configure(new File(moral.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllTicketUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<TicketUser> list = session.createQuery("from TicketUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateTicketUser(TicketUser user) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(user);
      session.getTransaction().commit();
      session.close();
   }

   public void removeTicketUser(TicketUser user) {
      if (user != null) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();
         session.delete(user);
         session.getTransaction().commit();
         session.close();
      }
   }

   public void addOrUpdateCode(TicketCode code) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(code);
      session.getTransaction().commit();
      session.close();
   }

   public void addOrUpdateTicketCodes(HashList successList) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();

      for(TicketCode tc : successList) {
         session.saveOrUpdate(tc);
      }

      session.getTransaction().commit();
      session.close();
   }

   public void removeCode(Code code) {
      if (code != null) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();
         session.delete(code);
         session.getTransaction().commit();
         session.close();
      }
   }

   public List getAllTicketLogs() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<TicketLog> list = session.createQuery("from TicketLog").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public TicketCode getTicketCode(String code) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<TicketCode> list = session.createQuery("from TicketCode tc where tc.code='" + code + "'").list();
      session.getTransaction().commit();
      session.close();
      return list.isEmpty() ? null : (TicketCode)list.get(0);
   }

   public List getAllTicketCodes() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<TicketCode> list = session.createQuery("from TicketCode").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateTicketLog(TicketLog ticketLog) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(ticketLog);
      session.getTransaction().commit();
      session.close();
   }

   public void removeTicketLog(TicketLog ticketLog) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(ticketLog);
      session.getTransaction().commit();
      session.close();
   }
}
