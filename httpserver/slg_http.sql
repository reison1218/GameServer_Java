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

/*Table structure for table `db_version` */

DROP TABLE IF EXISTS `db_version`;

CREATE TABLE `db_version` (
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `db_version` */

insert  into `db_version`(`version`) values 
(7);

/*Table structure for table `merge_change` */

DROP TABLE IF EXISTS `merge_change`;

CREATE TABLE `merge_change` (
  `reload` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `merge_change` */

/*Table structure for table `server_list` */

DROP TABLE IF EXISTS `server_list`;

CREATE TABLE `server_list` (
  `server_id` int NOT NULL COMMENT '服务器id',
  `name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL COMMENT '服务器名字',
  `ws` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'ws连接',
  `open_time` datetime DEFAULT NULL COMMENT '开服时间',
  `register_state` int DEFAULT NULL COMMENT '0超过N天不可注册1可注册',
  `state` int DEFAULT NULL COMMENT '0:正常开服状态 4：停服维护状态',
  `letter` int DEFAULT NULL COMMENT '0正常1强行推荐',
  `target_server_id` int DEFAULT NULL COMMENT '目标服id',
  `merge_times` int DEFAULT NULL COMMENT '第几次合服',
  `update_merge_times_time` datetime DEFAULT NULL COMMENT '修改merge_times的时间',
  `type` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL COMMENT '类型：0:开发服 1：测试服  2：内测服 10：正式服',
  `manager` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'http',
  `inner_manager` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL COMMENT '内网http',
  `server_type` int DEFAULT '0' COMMENT '是否版署服（0：不是 1是）',
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `server_list` */

insert  into `server_list`(`server_id`,`name`,`ws`,`open_time`,`register_state`,`state`,`letter`,`target_server_id`,`merge_times`,`update_merge_times_time`,`type`,`manager`,`inner_manager`,`server_type`) values 
(1501,'预演1501','wss://access-2141.skaa5.com/1501/game/wb','2023-06-09 18:06:14',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1501/manager','http://g2141-r2141-cn-yy-a2-w1501-0-intranet:31001',0),
(1502,'预演1502','wss://access-2141.skaa5.com/1502/game/wb','2023-07-06 12:09:49',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1502/manager','http://g2141-r2141-cn-yy-a2-w1502-0-intranet:31001',0),
(1503,'预演1503','wss://access-2141.skaa5.com/1503/game/wb','2023-07-07 14:00:00',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1503/manager','http://g2141-r2141-cn-yy-a2-w1503-0-intranet:31001',0),
(1504,'预演1504','wss://access-2141.skaa5.com/1504/game/wb','2023-07-25 15:40:42',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1504/manager','http://g2141-r2141-cn-yy-a2-w1504-0-intranet:31001',0),
(1505,'预演1505','wss://access-2141.skaa5.com/1505/game/wb','2023-07-11 14:28:25',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1505/manager','http://g2141-r2141-cn-yy-a2-w1505-0-intranet:31001',0),
(1506,'预演1506','wss://access-2141.skaa5.com/1506/game/wb','2023-07-12 08:00:00',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1506/manager','http://g2141-r2141-cn-yy-a2-w1506-0-intranet:31001',0),
(1507,'预演1507','wss://access-2141.skaa5.com/1507/game/wb','2023-07-12 08:00:00',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1507/manager','http://g2141-r2141-cn-yy-a2-w1507-0-intranet:31001',0),
(1508,'预演1508','wss://access-2141.skaa5.com/1508/game/wb','2023-07-19 18:27:29',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1508/manager','http://g2141-r2141-cn-yy-a2-w1508-0-intranet:31001',0),
(1509,'预演1509','wss://access-2141.skaa5.com/1509/game/wb','2023-07-25 15:40:07',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1509/manager','http://g2141-r2141-cn-yy-a2-w1509-0-intranet:31001',0),
(1510,'预演1510','wss://access-2141.skaa5.com/1510/game/wb','2023-07-19 18:27:29',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1510/manager','http://g2141-r2141-cn-yy-a2-w1510-0-intranet:31001',0),
(1511,'预演1511','wss://access-2141.skaa5.com/1511/game/wb','2023-07-19 18:27:29',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1511/manager','http://g2141-r2141-cn-yy-a2-w1511-0-intranet:31001',0),
(1512,'预演1512','wss://access-2141.skaa5.com/1512/game/wb','2023-07-28 10:45:00',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1512/manager','http://g2141-r2141-cn-yy-a2-w1512-0-intranet:31001',0),
(1513,'预演1513','wss://access-2141.skaa5.com/1513/game/wb','2023-07-20 16:55:02',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1513/manager','http://g2141-r2141-cn-yy-a2-w1513-0-intranet:31001',0),
(1514,'预演1514','wss://access-2141.skaa5.com/1514/game/wb','2023-07-20 16:55:02',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1514/manager','http://g2141-r2141-cn-yy-a2-w1514-0-intranet:31001',0),
(1515,'预演1515','wss://access-2141.skaa5.com/1515/game/wb','2023-07-20 16:55:02',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1515/manager','http://g2141-r2141-cn-yy-a2-w1515-0-intranet:31001',0),
(1516,'预演1516','wss://access-2141.skaa5.com/1516/game/wb','2023-07-20 16:55:02',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1516/manager','http://g2141-r2141-cn-yy-a2-w1516-0-intranet:31001',0),
(1517,'预演1517','wss://access-2141.skaa5.com/1517/game/wb','2023-07-31 15:00:00',1,0,0,1522,0,'2023-07-31 14:32:48','10','https://access-2141.skaa5.com/1517/manager','http://g2141-r2141-cn-yy-a2-w1517-0-intranet:31001',0),
(1518,'预演1518','wss://access-2141.skaa5.com/1518/game/wb','2023-07-20 16:55:02',1,0,0,1517,1,'2023-07-31 14:32:48','10','https://access-2141.skaa5.com/1518/manager','http://g2141-r2141-cn-yy-a2-w1518-0-intranet:31001',0),
(1519,'预演1519','wss://access-2141.skaa5.com/1519/game/wb','2023-07-20 16:55:02',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1519/manager','http://g2141-r2141-cn-yy-a2-w1519-0-intranet:31001',0),
(1520,'预演1520','wss://access-2141.skaa5.com/1520/game/wb','2023-08-02 09:00:00',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1520/manager','http://g2141-r2141-cn-yy-a2-w1520-0-intranet:31001',0),
(1521,'1521','wss://access-2141.skaa5.com/1521/game/wb','2023-07-27 15:54:55',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1521/manager','http://g2141-r2141-cn-yy-a2-w1521-0-intranet:31001',0),
(1522,'1522','wss://access-2141.skaa5.com/1522/game/wb','2023-07-31 16:00:00',1,0,0,0,0,'2023-07-31 15:21:26','10','https://access-2141.skaa5.com/1522/manager','http://g2141-r2141-cn-yy-a2-w1522-0-intranet:31001',0),
(1523,'预演1523','wss://access-2141.skaa5.com/1523/game/wb','2023-08-01 16:38:19',1,0,0,0,0,NULL,'10','https://access-2141.skaa5.com/1523/manager','http://g2141-r2141-cn-yy-a2-w1523-0-intranet:31001',0);

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` char(50) CHARACTER SET utf8mb3 COLLATE utf8_general_ci NOT NULL,
  `combine_id` bigint NOT NULL,
  `operator_id` int NOT NULL,
  `server_id` int NOT NULL,
  `level` int DEFAULT '0' COMMENT '基地等级',
  `player_name` char(50) CHARACTER SET utf8mb3 COLLATE utf8_general_ci DEFAULT NULL,
  `login_time` bigint DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`id`,`server_id`,`operator_id`),
  UNIQUE KEY `combine` (`combine_id`),
  UNIQUE KEY `name` (`name`,`server_id`,`operator_id`),
  UNIQUE KEY `player_name` (`server_id`,`operator_id`,`player_name`)
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb3;

/*Data for the table `users` */

insert  into `users`(`id`,`name`,`combine_id`,`operator_id`,`server_id`,`level`,`player_name`,`login_time`) values 
(4,'1017580893',780502931865602,11,1501,0,'困惑劳伦',1688390353125),
(5,'1017579271',780502931865603,11,1501,0,'1017579271',1688438482774),
(7,'1017572605',780502931865604,11,1501,0,'1017572605',1688630778807),
(14,'1017584143',780511521800193,11,1503,0,'自信泰勒',1688721431012),
(21,'1017584143',780507226832897,11,1502,0,'合服',1689047943887),
(23,'1017584143',780502931865601,11,1501,0,'合服',1689049125216),
(31,'1017910813',780515816767496,11,1504,1,'镇定柏莎',1689149803333),
(34,'1017583579',780515816767497,11,1504,2,'怡然奧德莉',1689153839144),
(43,'1017583579',780528701669378,11,1507,1,'高石佩顿',1689214316622),
(46,'1017584143',780524406702081,11,1506,1,'悠然弗里达',1689731675042),
(49,'1017584143',780520111734785,11,1505,1,'胆怯宁静',1689731921877),
(51,'1017572605',780528701669379,11,1507,1,'1017572605',1689758125531),
(55,'1017584143',780532996636673,11,1508,1,'野生佩顿',1689834935899),
(61,'1017584143',780545881538561,11,1511,1,'烦闷碧昂丝',1689835655617),
(69,'1017733309',780554471473153,11,1513,1,'好奇维纳斯',1689848832096),
(76,'1017584143',780563061407746,11,1515,1,'1017584143',1690163065194),
(77,'1018225329',780563061407747,11,1515,1,'1018225329',1690163259434),
(78,'1017910813',780563061407748,11,1515,1,'1017910813',1690163370558),
(79,'1017733309',780563061407745,11,1515,2,'浮夸爱丽丝',1690163396827),
(81,'1017584143',780558766440449,11,1514,2,'家伟',1690184164630),
(86,'1017584143',780541586571265,11,1510,2,'家伟',1690253695045),
(88,'1017584143',780515816767489,11,1504,4,'康康',1690253781935),
(90,'1017584143',780528701669377,11,1507,3,'康康',1690253825484),
(93,'1017584143',780537291603969,11,1509,2,'家伟',1690270936759),
(100,'1017584143',780588831211521,11,1521,1,'钻石凯尔',1690444622429),
(101,'1017584143',780567356375041,11,1516,2,'康康',1690509271824),
(110,'1017584143',780550176505857,11,1512,2,'康康',1690774557547),
(113,'1018508515',780571651342338,11,1517,1,'吉田伊妮德',1690786911525),
(116,'1017584143',780571651342337,11,1517,12,'迷惑艾维',1690790501999),
(117,'1017579331',780588831211522,11,1521,1,'1017579331',1690795736074),
(127,'1017579279',780580241276929,11,1519,1,'1017579279',1690878413033),
(143,'1018508515',780575946309633,11,1518,12,'广成喵',1690884439514),
(144,'1017579271',780580241276930,11,1519,1,'1017579271',1690885462314),
(179,'1017579325',780597421146113,11,1523,1,'尊敬邓肯',1691114454391),
(181,'1017579271',780597421146114,11,1523,1,'心酸雷切尔',1691115033333),
(182,'1017579271',780584536244225,11,1520,1,'1017579271',1691115406716),
(192,'1017580659',780584536244228,11,1520,1,'尊敬利萨',1691120936288),
(193,'1017581289',780584536244227,11,1520,1,'体面佩顿',1691120972801),
(195,'1017580807',780584536244229,11,1520,1,'北野黛利拉',1691121601461),
(196,'1018542823',780597421146116,11,1523,2,'邻家哈利',1691127816526),
(198,'1017580805',780584536244230,11,1520,1,'服部肯恩',1691130259732),
(202,'1017579281',780584536244226,11,1520,2,'桃谷伊夫林',1691133089661),
(206,'1017579279',780588831211523,11,1521,1,'困惑星',1691136028948),
(207,'1017579277',780597421146115,11,1523,1,'骄傲阿什顿',1691136306524);

/*Table structure for table `white_users` */

DROP TABLE IF EXISTS `white_users`;

CREATE TABLE `white_users` (
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '玩家账号',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `white_users` */

/*Table structure for table `wx_users_subscribe` */

DROP TABLE IF EXISTS `wx_users_subscribe`;

CREATE TABLE `wx_users_subscribe` (
  `name` varchar(128) NOT NULL COMMENT '玩家账号',
  `open_id` varchar(128) DEFAULT NULL COMMENT '玩家微信open_id',
  `templ_ids` varchar(1024) DEFAULT NULL COMMENT '玩家订阅的消息膜拜id',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `wx_users_subscribe` */

insert  into `wx_users_subscribe`(`name`,`open_id`,`templ_ids`) values 
('1017579271','oQ_A14w5Su492p71zJipAi-3_3XE','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|9990,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|9999,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|9999'),
('1017579277','oQ_A149fyWrpzMKU1gYOYmnnb5J4','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|9989,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|9999,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|9999'),
('1017579279','oQ_A143ISPXp5K7ASwPpXyGd9Sd8','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|1,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|4,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|1'),
('1017579281','oQ_A14zHKUCJSKXVsS8dpjgHkfbM','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|3,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|5,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|4'),
('1017579325','oQ_A14wgU1ncSkhhLbhRkEeualMI','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|0,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|0,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|0'),
('1017580659','oQ_A14yDcTFPbshl3iyaTQL4mJDQ','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|1,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|0,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|1'),
('1017581289','oQ_A14zifb4222QdosYNwvoWyPhc','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|1,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|0,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|1'),
('1018542823','oQ_A147MbfT-_wkx6uoXS8h9HSFw','muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE|9996,nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00|9999,TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY|9998');

/* Procedure structure for procedure `PrWs_Combine_slg_center` */

/*!50003 DROP PROCEDURE IF EXISTS  `PrWs_Combine_slg_center` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`%` PROCEDURE `PrWs_Combine_slg_center`(IN _Json JSON)
LABEL_PROC:
          BEGIN
            DECLARE _JsonLen INT DEFAULT 0;
            DECLARE _ToDB_Name VARCHAR(32);
            DECLARE _ToDB_WorldID INT;
            DECLARE _FromDB_Name VARCHAR(32);
            DECLARE _FromDB_WorldID INT;
            DECLARE msg TEXT;

            SELECT  JSON_LENGTH(_Json) INTO _JsonLen;
            IF _JsonLen = 0 THEN
              SELECT
                -1 `Code`,
                'Json长度异常' `Msg`;
              LEAVE LABEL_PROC;
            END IF;
            SET _ToDB_Name = JSON_UNQUOTE(JSON_EXTRACT(_Json, '$.ToDB_Name'));
            SET _ToDB_WorldID = JSON_UNQUOTE(JSON_EXTRACT(_Json, '$.ToDB_WorldID'));
            SET _FromDB_Name = JSON_UNQUOTE(JSON_EXTRACT(_Json, '$.FromDB_Name'));
            SET _FromDB_WorldID = JSON_UNQUOTE(JSON_EXTRACT(_Json, '$.FromDB_WorldID'));

            
            
  -- 设置merge_times
	SET @time_tmp = Null;
	SELECT update_merge_times_time into @time_tmp FROM server_list WHERE server_id = _ToDB_WorldID;
   IF @time_tmp is null or TO_DAYS(@time_tmp) != TO_DAYS(NOW()) 
   THEN
		SET @sQuery = CONCAT('update server_list SET merge_times = merge_times+1,update_merge_times_time = NOW() WHERE server_id = ',_ToDB_WorldID,'');
		PREPARE exesql FROM @sQuery;
		EXECUTE exesql;
   END IF; 

	SET @time_tmp = 0;
	SELECT update_merge_times_time into @time_tmp FROM server_list WHERE server_id = _FromDB_WorldID;
    IF  @time_tmp IS NULL or TO_DAYS(@time_tmp) != TO_DAYS(NOW())
    THEN
		SET @sQuery = CONCAT('UPDATE server_list SET merge_times = merge_times+1,update_merge_times_time = NOW() WHERE server_id = ',_FromDB_WorldID,'');
		PREPARE exesql FROM @sQuery;
		EXECUTE exesql;
    END IF; 
    
  -- 设置合服reloading状态

  -- 开始处理重名
   INSERT INTO merge_change(RELOAD) VALUES(1);

   SELECT 1 `Code`, '中心库合区成功' `Msg`;

    END */$$
DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
