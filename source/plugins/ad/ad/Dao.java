package ad;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Ad ad) {
      super();
      Configuration config = (new Configuration()).configure(new File(ad.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllAds() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<AdUser> list = session.createQuery("from AdUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateAdUser(AdUser adUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(adUser);
      session.getTransaction().commit();
      session.close();
   }

   public void removeUser(AdUser adUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(adUser);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllAdChats() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<AdChatUser> list = session.createQuery("from AdChatUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateAdChatUser(AdChatUser adChatUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(adChatUser);
      session.getTransaction().commit();
      session.close();
   }
}
