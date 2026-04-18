package cus;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Cus cus) {
      super();
      Configuration config = (new Configuration()).configure(new File(cus.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllMonPoints() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<MonPoint> list = session.createQuery("from MonPoint").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateMonPoint(MonPoint monPoint) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(monPoint);
      session.getTransaction().commit();
      session.close();
   }

   public void removeMonPoint(MonPoint monPoint) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(monPoint);
      session.getTransaction().commit();
      session.close();
   }
}
