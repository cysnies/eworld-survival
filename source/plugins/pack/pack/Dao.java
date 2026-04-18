package pack;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Main pack) {
      super();
      Configuration config = (new Configuration()).configure(new File(pack.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public boolean addPackUser(PackUser user) {
      Session session = this.sessionFactory.openSession();

      try {
         session.beginTransaction();
         session.save(user);
         return true;
      } catch (Exception var16) {
      } finally {
         try {
            session.getTransaction().commit();
         } catch (Exception var15) {
         }

         try {
            session.close();
         } catch (Exception var14) {
         }

      }

      return false;
   }

   public void addOrUpdatePackUser(PackUser user) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(user);
      session.getTransaction().commit();
      session.close();
   }

   public PackUser getPackUser(String name) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<PackUser> list = session.createQuery("from PackUser pu where pu.name='" + name + "'").list();
      session.getTransaction().commit();
      session.close();
      return list.isEmpty() ? null : (PackUser)list.get(0);
   }
}
