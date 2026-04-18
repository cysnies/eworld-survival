package friend;

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

   public List getAllFriends() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<Friend> list = session.createQuery("from Friend").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateFriend(Friend friend) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(friend);
      session.getTransaction().commit();
      session.close();
   }

   public void removeFriend(Friend friend) {
      if (friend != null) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();
         session.delete(friend);
         session.getTransaction().commit();
         session.close();
      }
   }
}
