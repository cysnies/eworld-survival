package level;

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

   public List getAllLevelUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<LevelUser> list = session.createQuery("from LevelUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateLevelUser(LevelUser levelUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(levelUser);
      session.getTransaction().commit();
      session.close();
   }
}
