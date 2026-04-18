package chat;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Chat moral) {
      super();
      Configuration config = (new Configuration()).configure(new File(moral.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllChatUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<ChatUser> list = session.createQuery("from ChatUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateChatUser(ChatUser chatUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(chatUser);
      session.getTransaction().commit();
      session.close();
   }

   public void removeChatUser(ChatUser chatUser) {
      if (chatUser != null) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();
         session.delete(chatUser);
         session.getTransaction().commit();
         session.close();
      }
   }

   public List getAllBlackUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<BlackUser> list = session.createQuery("from BlackUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateBlackUser(BlackUser blackUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(blackUser);
      session.getTransaction().commit();
      session.close();
   }

   public void removeBlackUser(BlackUser blackUser) {
      if (blackUser != null) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();
         session.delete(blackUser);
         session.getTransaction().commit();
         session.close();
      }
   }

   public List getAllChannelUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<ChannelUser> list = session.createQuery("from ChannelUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateChannelUser(ChannelUser channelUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(channelUser);
      session.getTransaction().commit();
      session.close();
   }
}
