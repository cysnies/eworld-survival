package infos;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import lib.hashList.HashList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private static final long DAY = 86400000L;
   private SessionFactory sessionFactory;

   public Dao(Infos infos) {
      super();
      Configuration config = (new Configuration()).configure(new File(infos.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllPlayerInfos() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<PlayerInfo> list = session.createQuery("from PlayerInfo").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public PlayerInfo getPlayerInfo(String name) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<PlayerInfo> list = session.createQuery("from PlayerInfo pi where pi.name=:name").setString("name", name).list();
      session.getTransaction().commit();
      session.close();
      return list.size() == 0 ? null : (PlayerInfo)list.get(0);
   }

   public void addOrUpdatePlayerInfo(PlayerInfo playerInfo) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(playerInfo);
      session.getTransaction().commit();
      session.close();
   }

   public void updatePlayerInfos(HashList needUpdateList, HashMap playerHash) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();

      for(String s : needUpdateList) {
         PlayerInfo playerInfo = (PlayerInfo)playerHash.get(s);
         if (playerInfo != null) {
            session.saveOrUpdate(playerInfo);
         }
      }

      session.getTransaction().commit();
      session.close();
   }

   public ServerTotalInfo getServerTotalInfo() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<ServerTotalInfo> list = session.createQuery("from ServerTotalInfo").list();
      session.getTransaction().commit();
      session.close();
      return list.size() == 0 ? null : (ServerTotalInfo)list.get(0);
   }

   public void addOrUpdateServerTotalInfo(ServerTotalInfo serverTotalInfo) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(serverTotalInfo);
      session.getTransaction().commit();
      session.close();
   }

   public void clearDayInfo(int clearDay) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      int nowDay = (int)(System.currentTimeMillis() / 86400000L);
      int checkDay = nowDay - clearDay;
      session.createQuery("delete PlayerDayInfo pdi where pdi.time<:time").setInteger("time", checkDay).executeUpdate();
      session.getTransaction().commit();
      session.close();
   }

   public List getAllPlayerDayInfos() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<PlayerDayInfo> list = session.createQuery("from PlayerDayInfo").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void fixAllPlayerDayInfos() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      int nowDay = (int)(System.currentTimeMillis() / 86400000L);
      session.createQuery("delete PlayerDayInfo pdi where pdi.time>:day").setInteger("day", nowDay).executeUpdate();
      session.getTransaction().commit();
      session.close();
   }

   public List getAllPlayerDayInfos(int day) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      int nowDay = (int)(System.currentTimeMillis() / 86400000L);
      int checkDay = nowDay - day;
      List<PlayerDayInfo> list = session.createQuery("from PlayerDayInfo pdi where pdi.time>=:day").setInteger("day", checkDay).list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrSavePlayerDayInfos(HashList result) {
      if (result != null && !result.isEmpty()) {
         Session session = this.sessionFactory.openSession();
         session.beginTransaction();

         for(PlayerDayInfo pdi : result) {
            session.saveOrUpdate(pdi);
         }

         session.getTransaction().commit();
         session.close();
      }
   }

   public List getAllGiftUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<GiftUser> list = session.createQuery("from GiftUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateGiftUser(GiftUser giftUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(giftUser);
      session.getTransaction().commit();
      session.close();
   }

   public void addOrUpdateGiftUsers(Collection list) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();

      for(GiftUser giftUser : list) {
         session.saveOrUpdate(giftUser);
      }

      session.getTransaction().commit();
      session.close();
   }

   public List getAllJoinUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<JoinUser> list = session.createQuery("from JoinUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateJoinUser(JoinUser joinUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(joinUser);
      session.getTransaction().commit();
      session.close();
   }
}
