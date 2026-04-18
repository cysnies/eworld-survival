package infos;

public class ServerTotalInfo {
   private long id;
   private long startTime;
   private long openTime;
   private long totalJoinTimes;
   private long totalOnlineTime;
   private int maxOnline;
   private long totalKills;
   private long totalDeaths;
   private long totalMines;
   private long totalBreaks;
   private long totalPlaces;
   private long totalKillMonsters;
   private long totalKillAnimals;
   private int totalPlayers;
   private int aliveAmounts;
   private int activeAmounts;

   public ServerTotalInfo() {
      super();
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public void setStartTime(long startTime) {
      this.startTime = startTime;
   }

   public long getOpenTime() {
      return this.openTime;
   }

   public void setOpenTime(long openTime) {
      this.openTime = openTime;
   }

   public long getTotalJoinTimes() {
      return this.totalJoinTimes;
   }

   public void setTotalJoinTimes(long totalJoinTimes) {
      this.totalJoinTimes = totalJoinTimes;
   }

   public long getTotalOnlineTime() {
      return this.totalOnlineTime;
   }

   public void setTotalOnlineTime(long totalOnlineTime) {
      this.totalOnlineTime = totalOnlineTime;
   }

   public long getTotalKills() {
      return this.totalKills;
   }

   public void setTotalKills(long totalKills) {
      this.totalKills = totalKills;
   }

   public long getTotalDeaths() {
      return this.totalDeaths;
   }

   public void setTotalDeaths(long totalDeaths) {
      this.totalDeaths = totalDeaths;
   }

   public long getTotalMines() {
      return this.totalMines;
   }

   public void setTotalMines(long totalMines) {
      this.totalMines = totalMines;
   }

   public long getTotalBreaks() {
      return this.totalBreaks;
   }

   public void setTotalBreaks(long totalBreaks) {
      this.totalBreaks = totalBreaks;
   }

   public long getTotalPlaces() {
      return this.totalPlaces;
   }

   public void setTotalPlaces(long totalPlaces) {
      this.totalPlaces = totalPlaces;
   }

   public long getTotalKillMonsters() {
      return this.totalKillMonsters;
   }

   public void setTotalKillMonsters(long totalKillMonsters) {
      this.totalKillMonsters = totalKillMonsters;
   }

   public long getTotalKillAnimals() {
      return this.totalKillAnimals;
   }

   public void setTotalKillAnimals(long totalKillAnimals) {
      this.totalKillAnimals = totalKillAnimals;
   }

   public void setTotalPlayers(int totalPlayers) {
      this.totalPlayers = totalPlayers;
   }

   public int getAliveAmounts() {
      return this.aliveAmounts;
   }

   public void setAliveAmounts(int aliveAmounts) {
      this.aliveAmounts = aliveAmounts;
   }

   public int getActiveAmounts() {
      return this.activeAmounts;
   }

   public void setActiveAmounts(int activeAmounts) {
      this.activeAmounts = activeAmounts;
   }

   public int getTotalPlayers() {
      return this.totalPlayers;
   }

   public int getMaxOnline() {
      return this.maxOnline;
   }

   public void setMaxOnline(int maxOnline) {
      this.maxOnline = maxOnline;
   }
}
