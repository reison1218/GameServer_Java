/*
SQLyog Community v13.1.9 (64 bit)
MySQL - 8.0.29 : Database - slg_http
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`slg_http` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `slg_http`;

/*Table structure for table `server_list` */

DROP TABLE IF EXISTS `server_list`;

/*中心服，服务器列表*/
CREATE TABLE `server_list` (
  `server_id` int NOT NULL COMMENT '服务器id',
  `name` varchar(128) DEFAULT NULL COMMENT '服务器名字',
  `ws` varchar(128) DEFAULT NULL COMMENT 'ws连接',
  `open_time` datetime DEFAULT NULL COMMENT '开服时间',
  `register_state` int DEFAULT NULL COMMENT '0超过N天不可注册1可注册',
  `state` int DEFAULT NULL COMMENT '0:正常开服状态 4：停服维护状态',
  `letter` int DEFAULT NULL COMMENT '0正常1强行推荐',
  `target_server_id` int DEFAULT NULL COMMENT '目标服id',
  `merge_times` int DEFAULT NULL COMMENT '第几次合服',
  `type` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL COMMENT '类型：0:开发服 1：测试服  2：内测服 10：正式服',
  `recharge_http_url` varchar(128) DEFAULT NULL COMMENT '充值http接口',
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `server_list` */

/*新增服务器sql例句*/
insert  into `server_list`(`server_id`,`name`,`ws`,`open_time`,`register_state`,`state`,`letter`,`target_server_id`,`merge_times`,`type`,`recharge_http_url`) values 
(1001,'火星1服','121.5.175.87|30001/wb','2022-11-11 08:00:00',1,0,0,0,0,'10','http://121.5.175.87:16666/api/common/recharge'),

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

/*中心服，玩家列表*/
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` char(50) NOT NULL,
  `combine_id` bigint NOT NULL,
  `operator_id` int NOT NULL,
  `server_id` int NOT NULL,
  `player_name` char(50) DEFAULT NULL,
  `login_time` bigint DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`id`,`server_id`,`operator_id`),
  UNIQUE KEY `combine` (`combine_id`),
  UNIQUE KEY `name` (`name`,`server_id`,`operator_id`) USING BTREE,
  UNIQUE KEY `player_name` (`server_id`,`operator_id`,`player_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19811 DEFAULT CHARSET=utf8mb3;

/*Data for the table `users` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
