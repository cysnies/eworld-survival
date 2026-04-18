package sub;

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

   public void addOrUpdateLandLimit(LandLimit landLimit) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(landLimit);
      session.getTransaction().commit();
      session.close();
   }

   public LandLimit getLandLimit(String name) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<LandLimit> list = session.createQuery("from LandLimit ll where ll.name='" + name + "'").list();
      session.getTransaction().commit();
      session.close();
      return list.isEmpty() ? null : (LandLimit)list.get(0);
   }

   public void addOrUpdatePackLimit(PackLimit packLimit) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(packLimit);
      session.getTransaction().commit();
      session.close();
   }

   public PackLimit getPackLimit(String name) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<PackLimit> list = session.createQuery("from PackLimit pl where pl.name='" + name + "'").list();
      session.getTransaction().commit();
      session.close();
      return list.isEmpty() ? null : (PackLimit)list.get(0);
   }
}
