package town;

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

   public List getAllTownUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<TownUser> list = session.createQuery("from TownUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateTownUser(TownUser townUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(townUser);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllTownInfos() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<TownInfo> list = session.createQuery("from TownInfo").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateTownInfo(TownInfo townInfo) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(townInfo);
      session.getTransaction().commit();
      session.close();
   }
}
