package chest;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Chest chest) {
      super();
      Configuration config = (new Configuration()).configure(new File(chest.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllChestInfos() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<ChestInfo> list = session.createQuery("from ChestInfo").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateChestInfo(ChestInfo chestInfo) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(chestInfo);
      session.getTransaction().commit();
      session.close();
   }

   public void removeChestInfo(ChestInfo chestInfo) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(chestInfo);
      session.getTransaction().commit();
      session.close();
   }
}
