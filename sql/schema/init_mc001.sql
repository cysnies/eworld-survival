-- mc001 库表结构：仓库内唯一权威 DDL（与旧 HeidiSQL 导出的纯结构 dump 内容等价；
-- Compose 可选挂载至 docker-entrypoint-initdb.d，或由 ./db.sh init-schema 导入。
USE mc001;

-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        5.6.21-log - MySQL Community Server (GPL)
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- 导出  表 mc001.adchatuser 结构
CREATE TABLE IF NOT EXISTS `adchatuser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `msg` blob,
  `count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.aduser 结构
CREATE TABLE IF NOT EXISTS `aduser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `msg` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `start` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.chat_blackuser 结构
CREATE TABLE IF NOT EXISTS `chat_blackuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `blackList` tinyblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.chat_channeluser 结构
CREATE TABLE IF NOT EXISTS `chat_channeluser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `channel` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.chat_chatuser 结构
CREATE TABLE IF NOT EXISTS `chat_chatuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `c` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.chestinfo 结构
CREATE TABLE IF NOT EXISTS `chestinfo` (
  `id` bigint(20) NOT NULL,
  `pos` tinyblob,
  `generate` tinyint(1) DEFAULT NULL,
  `refresh` tinyint(1) DEFAULT NULL,
  `check_` int(11) DEFAULT NULL,
  `chance` int(11) DEFAULT NULL,
  `itemType` varchar(255) DEFAULT NULL,
  `enchantType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.debtuser_s 结构
CREATE TABLE IF NOT EXISTS `debtuser_s` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `debt` int(11) DEFAULT NULL,
  `log` blob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.friend 结构
CREATE TABLE IF NOT EXISTS `friend` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `tip` tinyint(1) DEFAULT NULL,
  `limits` int(11) DEFAULT NULL,
  `friendList` tinyblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.giftuser 结构
CREATE TABLE IF NOT EXISTS `giftuser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `giftHash` tinyblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.houseuser 结构
CREATE TABLE IF NOT EXISTS `houseuser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `x` int(11) DEFAULT NULL,
  `z` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.infos_playerdayinfo 结构
CREATE TABLE IF NOT EXISTS `infos_playerdayinfo` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  `onlineTime` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.infos_playerinfo 结构
CREATE TABLE IF NOT EXISTS `infos_playerinfo` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `joinTime` bigint(20) DEFAULT NULL,
  `lastTime` bigint(20) DEFAULT NULL,
  `alive` tinyint(1) DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `onlineTime` int(11) DEFAULT NULL,
  `mineNum` int(11) DEFAULT NULL,
  `breakNum` int(11) DEFAULT NULL,
  `placeNum` int(11) DEFAULT NULL,
  `killMonsterNum` int(11) DEFAULT NULL,
  `killAnimalNum` int(11) DEFAULT NULL,
  `killPlayerNum` int(11) DEFAULT NULL,
  `death` int(11) DEFAULT NULL,
  `power` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `qq` varchar(255) DEFAULT NULL,
  `xq` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.infos_servertotalinfo 结构
CREATE TABLE IF NOT EXISTS `infos_servertotalinfo` (
  `id` bigint(20) NOT NULL,
  `startTime` bigint(20) DEFAULT NULL,
  `openTime` bigint(20) DEFAULT NULL,
  `totalJoinTimes` bigint(20) DEFAULT NULL,
  `totalOnlineTime` bigint(20) DEFAULT NULL,
  `maxOnline` int(11) DEFAULT NULL,
  `totalKills` bigint(20) DEFAULT NULL,
  `totalDeaths` bigint(20) DEFAULT NULL,
  `totalMines` bigint(20) DEFAULT NULL,
  `totalBreaks` bigint(20) DEFAULT NULL,
  `totalPlaces` bigint(20) DEFAULT NULL,
  `totalKillMonsters` bigint(20) DEFAULT NULL,
  `totalKillAnimals` bigint(20) DEFAULT NULL,
  `totalPlayers` int(11) DEFAULT NULL,
  `aliveAmounts` int(11) DEFAULT NULL,
  `activeAmounts` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.joinuser 结构
CREATE TABLE IF NOT EXISTS `joinuser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `free` tinyint(1) DEFAULT NULL,
  `joinUser` varchar(255) DEFAULT NULL,
  `amount` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.landlimit 结构
CREATE TABLE IF NOT EXISTS `landlimit` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `limit_` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.land_entertip 结构
CREATE TABLE IF NOT EXISTS `land_entertip` (
  `id` bigint(20) NOT NULL,
  `landId` bigint(20) NOT NULL,
  `tip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.land_land 结构
CREATE TABLE IF NOT EXISTS `land_land` (
  `id` bigint(20) NOT NULL,
  `fix` tinyint(1) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `overlap` tinyint(1) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `range_` tinyblob,
  `level` int(11) DEFAULT NULL,
  `friendPer` tinyint(1) DEFAULT NULL,
  `flags` blob,
  `pers` blob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.land_landcmd 结构
CREATE TABLE IF NOT EXISTS `land_landcmd` (
  `id` bigint(20) NOT NULL,
  `landId` bigint(20) NOT NULL,
  `cmd` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.land_landspawn 结构
CREATE TABLE IF NOT EXISTS `land_landspawn` (
  `id` bigint(20) NOT NULL,
  `landId` bigint(20) NOT NULL,
  `spawn` tinyblob,
  `yaw` float DEFAULT NULL,
  `pitch` float DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.land_landuser 结构
CREATE TABLE IF NOT EXISTS `land_landuser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `maxLands` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.land_leavetip 结构
CREATE TABLE IF NOT EXISTS `land_leavetip` (
  `id` bigint(20) NOT NULL,
  `landId` bigint(20) NOT NULL,
  `tip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.leveluser 结构
CREATE TABLE IF NOT EXISTS `leveluser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `levelHash` blob,
  `showLevelId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.monpoint 结构
CREATE TABLE IF NOT EXISTS `monpoint` (
  `id` bigint(20) NOT NULL,
  `pos_` tinyblob,
  `type_` varchar(255) DEFAULT NULL,
  `monType` int(11) DEFAULT NULL,
  `interval_` int(11) DEFAULT NULL,
  `chance_` int(11) DEFAULT NULL,
  `max_` int(11) DEFAULT NULL,
  `monList` blob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.packlimit 结构
CREATE TABLE IF NOT EXISTS `packlimit` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `limit_` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.packuser 结构
CREATE TABLE IF NOT EXISTS `packuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `itemsHash` blob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.shop 结构
CREATE TABLE IF NOT EXISTS `shop` (
  `id` bigint(20) NOT NULL,
  `s_` mediumtext,
  `owner` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `start` bigint(20) DEFAULT NULL,
  `last` bigint(20) DEFAULT NULL,
  `ticket` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.ticketcode 结构
CREATE TABLE IF NOT EXISTS `ticketcode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `ticket` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `useTime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.ticketlog 结构
CREATE TABLE IF NOT EXISTS `ticketlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `time` bigint(20) DEFAULT NULL,
  `log` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.ticketuser 结构
CREATE TABLE IF NOT EXISTS `ticketuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ticket` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.towninfo 结构
CREATE TABLE IF NOT EXISTS `towninfo` (
  `id` bigint(20) NOT NULL,
  `landId` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `level` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `safeLock` tinyint(1) NOT NULL,
  `x` int(11) DEFAULT NULL,
  `z` int(11) DEFAULT NULL,
  `userHash` blob,
  `giveHash` blob,
  `askHash` blob,
  `active` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.townuser 结构
CREATE TABLE IF NOT EXISTS `townuser` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `townId` bigint(20) DEFAULT NULL,
  `pos` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 mc001.user_s 结构
CREATE TABLE IF NOT EXISTS `user_s` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
