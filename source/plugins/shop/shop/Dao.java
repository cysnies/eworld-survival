package shop;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Main main) {
      super();
      Configuration config = (new Configuration()).configure(new File(main.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllShops() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<Shop> list = session.createQuery("from Shop").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateShop(Shop shop) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(shop);
      session.getTransaction().commit();
      session.close();
   }

   public void removeShop(Shop shop) {
      if (shop != null) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();
         session.delete(shop);
         session.getTransaction().commit();
         session.close();
      }
   }
}
