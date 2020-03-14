-- MySQL dump 10.13  Distrib 5.6.21, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: open
-- ------------------------------------------------------
-- Server version	5.6.21

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `auth_approve`
--

DROP TABLE IF EXISTS `auth_approve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_approve` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `apply_by` varchar(20) NOT NULL DEFAULT '' COMMENT '空',
  `apply_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  `apply_type` varchar(20) DEFAULT NULL COMMENT '空',
  `apply_ops` varchar(200) DEFAULT NULL COMMENT '空',
  `apply_targets` varchar(50) DEFAULT NULL COMMENT '空',
  `approved_by` varchar(20) DEFAULT NULL COMMENT '空',
  `approved_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `approved` bit(1) DEFAULT b'0' COMMENT '空',
  `context` mediumblob COMMENT '空',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=721 DEFAULT CHARSET=utf8 COMMENT='approvals Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_approve`
--

LOCK TABLES `auth_approve` WRITE;
/*!40000 ALTER TABLE `auth_approve` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_approve` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_private_key`
--

DROP TABLE IF EXISTS `auth_private_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_private_key` (
  `private_key` varchar(50) NOT NULL DEFAULT '' COMMENT 'private key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last change time',
  PRIMARY KEY (`private_key`),
  KEY `time idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store the private key';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_private_key`
--

LOCK TABLES `auth_private_key` WRITE;
/*!40000 ALTER TABLE `auth_private_key` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_private_key` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_r_approve_targets`
--

DROP TABLE IF EXISTS `auth_r_approve_targets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_r_approve_targets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `approval_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `target_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `approval_id` (`approval_id`),
  KEY `target_id` (`target_id`),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=880 DEFAULT CHARSET=utf8 COMMENT='approval targets table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_r_approve_targets`
--

LOCK TABLES `auth_r_approve_targets` WRITE;
/*!40000 ALTER TABLE `auth_r_approve_targets` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_r_approve_targets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_role_e`
--

DROP TABLE IF EXISTS `auth_role_e`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_role_e` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) NOT NULL DEFAULT 'unknow' COMMENT 'name',
  `discription` varchar(256) DEFAULT NULL COMMENT 'discription',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='auth_role_e';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_role_e`
--

LOCK TABLES `auth_role_e` WRITE;
/*!40000 ALTER TABLE `auth_role_e` DISABLE KEYS */;
INSERT INTO `auth_role_e` (`id`, `name`, `discription`, `DataChange_LastTime`) VALUES (6,'superAdmin','superAdmin','2019-10-25 02:51:41');
INSERT INTO `auth_role_e` (`id`, `name`, `discription`, `DataChange_LastTime`) VALUES (7,'slbVisitor','default visitor','2019-10-25 03:02:05');
INSERT INTO `auth_role_e` (`id`, `name`, `discription`, `DataChange_LastTime`) VALUES (8,'ops','ops role','2019-10-25 03:39:11');
/*!40000 ALTER TABLE `auth_role_e` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_role_resource_r`
--

DROP TABLE IF EXISTS `auth_role_resource_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_role_resource_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `role_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'r',
  `type` varchar(128) NOT NULL DEFAULT 'unknow' COMMENT 't',
  `data` varchar(128) NOT NULL DEFAULT '' COMMENT 'o',
  `operation` varchar(2048) NOT NULL DEFAULT '0' COMMENT 'op',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_id_type_data_id` (`role_id`,`type`,`data`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `role_id` (`role_id`),
  KEY `role_id_type` (`role_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8 COMMENT='auth_role_resource_r';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_role_resource_r`
--

LOCK TABLES `auth_role_resource_r` WRITE;
/*!40000 ALTER TABLE `auth_role_resource_r` DISABLE KEYS */;
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (46,6,'Flow','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;NEW;READ;PROPERTY;FORCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (47,6,'SyncError','*','MAINTENANCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (48,6,'Conf','*','MAINTENANCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (49,6,'Ip','*','BLACK_LIST;OP_SERVER;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (50,6,'Clean','*','MAINTENANCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (51,6,'Rule','*','ACTIVATE;DEACTIVATE;UPDATE;NEW;DELETE;READ;PROPERTY;FORCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (52,6,'Slb','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;SYNC;ADMIN_INFO;WAF;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (53,6,'Policy','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;FORCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (54,6,'Auth','*','AUTH;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (55,6,'Group','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;SYNC;OP_MEMBER;OP_PULL;OP_HEALTH_CHECK;FORCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (56,6,'Vs','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;SYNC;CERT;FORCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (57,6,'Lock','*','MAINTENANCE;','2019-10-25 02:51:41');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (58,7,'Conf','*','MAINTENANCE;','2019-10-25 03:02:05');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (59,7,'Rule','*','UPDATE;NEW;READ;DELETE;','2019-10-25 03:02:05');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (60,7,'Slb','*','READ;PROPERTY;','2019-10-25 03:02:05');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (61,7,'Policy','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;','2019-10-25 03:02:05');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (62,7,'Group','*','DELETE;READ;PROPERTY;OP_MEMBER;OP_PULL;UPDATE;ACTIVATE;','2019-10-25 03:02:05');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (63,7,'Vs','*','ACTIVATE;UPDATE;READ;PROPERTY;','2019-10-25 03:02:05');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (64,8,'SyncError','*','MAINTENANCE;','2019-10-25 03:39:11');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (65,8,'Conf','*','MAINTENANCE;','2019-10-25 03:39:11');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (66,8,'Ip','*','OP_SERVER;','2019-10-25 03:39:11');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (67,8,'Slb','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;SYNC;ADMIN_INFO;WAF;','2019-10-25 03:39:11');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (68,8,'Group','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;SYNC;OP_MEMBER;OP_PULL;OP_HEALTH_CHECK;','2019-10-25 03:39:11');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (69,8,'Vs','*','ACTIVATE;DEACTIVATE;UPDATE;DELETE;READ;PROPERTY;SYNC;CERT;','2019-10-25 03:39:11');
INSERT INTO `auth_role_resource_r` (`id`, `role_id`, `type`, `data`, `operation`, `DataChange_LastTime`) VALUES (70,8,'Lock','*','MAINTENANCE;','2019-10-25 03:39:11');
/*!40000 ALTER TABLE `auth_role_resource_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_e`
--

DROP TABLE IF EXISTS `auth_user_e`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_e` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) NOT NULL DEFAULT 'unknow' COMMENT 'name',
  `email` varchar(128) DEFAULT 'unknow' COMMENT 'email',
  `bu` varchar(128) DEFAULT 'unknow' COMMENT 'bu',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  `chinese_name` varchar(128) DEFAULT NULL COMMENT 'cname',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=5707 DEFAULT CHARSET=utf8 COMMENT='auth_user_e';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_e`
--

LOCK TABLES `auth_user_e` WRITE;
/*!40000 ALTER TABLE `auth_user_e` DISABLE KEYS */;
INSERT INTO `auth_user_e` (`id`, `name`, `email`, `bu`, `DataChange_LastTime`, `chinese_name`) VALUES (5704,'admin','admin@test.com',NULL,'2019-10-25 03:30:59',NULL);
INSERT INTO `auth_user_e` (`id`, `name`, `email`, `bu`, `DataChange_LastTime`, `chinese_name`) VALUES (5705,'slbServer',NULL,NULL,'2019-10-25 03:32:40',NULL);
INSERT INTO `auth_user_e` (`id`, `name`, `email`, `bu`, `DataChange_LastTime`, `chinese_name`) VALUES (5706,'healthChecker','healthChecker','healthChecker','2019-10-25 03:40:25',NULL);
/*!40000 ALTER TABLE `auth_user_e` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_password`
--

DROP TABLE IF EXISTS `auth_user_password`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_password` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_name` varchar(32) NOT NULL COMMENT 'user_name',
  `password` varchar(64) NOT NULL COMMENT 'password',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datechange',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_password` (`user_name`,`password`),
  KEY `datechange` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=12149 DEFAULT CHARSET=utf8 COMMENT='auth_user_password';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_password`
--

LOCK TABLES `auth_user_password` WRITE;
/*!40000 ALTER TABLE `auth_user_password` DISABLE KEYS */;
INSERT INTO `auth_user_password` (`id`, `user_name`, `password`, `DataChange_LastTime`) VALUES (12148,'admin','admin','2019-10-25 03:20:24');
/*!40000 ALTER TABLE `auth_user_password` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_resource_r`
--

DROP TABLE IF EXISTS `auth_user_resource_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_resource_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'u',
  `type` varchar(128) NOT NULL DEFAULT 'unknown' COMMENT 't',
  `data` varchar(128) NOT NULL DEFAULT 'unknown' COMMENT 'r',
  `operation` varchar(2048) NOT NULL DEFAULT '0' COMMENT 'ops',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_type_data_id` (`user_id`,`type`,`data`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `user_id` (`user_id`),
  KEY `user_id_type` (`user_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=220073 DEFAULT CHARSET=utf8 COMMENT='auth_user_resource_r';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_resource_r`
--

LOCK TABLES `auth_user_resource_r` WRITE;
/*!40000 ALTER TABLE `auth_user_resource_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user_resource_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_role_r`
--

DROP TABLE IF EXISTS `auth_user_role_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_role_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'u',
  `role_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'r',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_role_id` (`user_id`,`role_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=5586 DEFAULT CHARSET=utf8 COMMENT='auth_user_role_r';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_role_r`
--

LOCK TABLES `auth_user_role_r` WRITE;
/*!40000 ALTER TABLE `auth_user_role_r` DISABLE KEYS */;
INSERT INTO `auth_user_role_r` (`id`, `user_id`, `role_id`, `DataChange_LastTime`) VALUES (5582,5704,6,'2019-10-25 03:30:59');
INSERT INTO `auth_user_role_r` (`id`, `user_id`, `role_id`, `DataChange_LastTime`) VALUES (5583,5705,7,'2019-10-25 03:32:40');
INSERT INTO `auth_user_role_r` (`id`, `user_id`, `role_id`, `DataChange_LastTime`) VALUES (5585,5706,8,'2019-10-25 03:40:25');
/*!40000 ALTER TABLE `auth_user_role_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cert_certificate`
--

DROP TABLE IF EXISTS `cert_certificate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cert_certificate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `domain` varchar(1024) NOT NULL DEFAULT 'localhost' COMMENT 'certificate domain',
  `cert` mediumblob NOT NULL COMMENT 'certificate file',
  `key` mediumblob NOT NULL COMMENT 'key file',
  `state` bit(1) NOT NULL DEFAULT b'1' COMMENT 'state',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  `cid` varchar(32) DEFAULT NULL COMMENT 'cert_id',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `domain` (`domain`(255)),
  KEY `cid` (`cid`)
) ENGINE=InnoDB AUTO_INCREMENT=2954 DEFAULT CHARSET=utf8 COMMENT='meta data table of certificate';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cert_certificate`
--

LOCK TABLES `cert_certificate` WRITE;
/*!40000 ALTER TABLE `cert_certificate` DISABLE KEYS */;
/*!40000 ALTER TABLE `cert_certificate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cert_certificate_slb_server_r`
--

DROP TABLE IF EXISTS `cert_certificate_slb_server_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cert_certificate_slb_server_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `cert_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'certificate id',
  `command` bigint(20) NOT NULL DEFAULT '0' COMMENT 'commanded cert id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'virtual server id',
  `ip` varchar(100) NOT NULL DEFAULT '0' COMMENT 'slb server ip',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip_vs_id` (`ip`,`vs_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=11438 DEFAULT CHARSET=utf8 COMMENT='relation table of certificate and slb server';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cert_certificate_slb_server_r`
--

LOCK TABLES `cert_certificate_slb_server_r` WRITE;
/*!40000 ALTER TABLE `cert_certificate_slb_server_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `cert_certificate_slb_server_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cert_certificate_vs_r`
--

DROP TABLE IF EXISTS `cert_certificate_vs_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cert_certificate_vs_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vsid',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'version',
  `cert_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'cert_id',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  `status` varchar(32) DEFAULT NULL COMMENT 'status',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vsversion` (`vs_id`,`version`),
  KEY `vsid` (`vs_id`),
  KEY `certId` (`cert_id`),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `vsstatus` (`vs_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2471 DEFAULT CHARSET=utf8 COMMENT='r_certificate_vs';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cert_certificate_vs_r`
--

LOCK TABLES `cert_certificate_vs_r` WRITE;
/*!40000 ALTER TABLE `cert_certificate_vs_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `cert_certificate_vs_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dist_lock`
--

DROP TABLE IF EXISTS `dist_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dist_lock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `lock_key` varchar(255) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'lock key',
  `owner` bigint(20) DEFAULT '0' COMMENT 'thread id',
  `server` varchar(50) DEFAULT '0' COMMENT 'server ip',
  `created_time` bigint(20) DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `lock_key` (`lock_key`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=35921 DEFAULT CHARSET=utf8 COMMENT='distribution lock';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dist_lock`
--

LOCK TABLES `dist_lock` WRITE;
/*!40000 ALTER TABLE `dist_lock` DISABLE KEYS */;
INSERT INTO `dist_lock` (`id`, `lock_key`, `owner`, `server`, `created_time`, `DataChange_LastTime`) VALUES (35920,'HealthCheckMetric_null',0,'',1571918646140,'2019-10-24 12:04:05');
/*!40000 ALTER TABLE `dist_lock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback_commit`
--

DROP TABLE IF EXISTS `feedback_commit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feedback_commit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'user',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create_time',
  `description` varchar(2048) DEFAULT NULL COMMENT 'description',
  `DataChange_LastTime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `user` (`user`),
  KEY `createTime` (`create_time`),
  KEY `lastchange` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='feedback';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback_commit`
--

LOCK TABLES `feedback_commit` WRITE;
/*!40000 ALTER TABLE `feedback_commit` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedback_commit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_active_r`
--

DROP TABLE IF EXISTS `file_active_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_active_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'sid',
  `key` varchar(128) NOT NULL DEFAULT '0' COMMENT 'key',
  `type` varchar(128) NOT NULL DEFAULT '0' COMMENT 'type',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_key` (`slb_id`,`key`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `slb_id_type` (`slb_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8 COMMENT='default_page_active';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_active_r`
--

LOCK TABLES `file_active_r` WRITE;
/*!40000 ALTER TABLE `file_active_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_active_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_data`
--

DROP TABLE IF EXISTS `file_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `key` varchar(128) NOT NULL DEFAULT '0' COMMENT 'key',
  `file_data` mediumblob NOT NULL COMMENT 'filedata',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_version` (`key`,`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='default_page_file';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_data`
--

LOCK TABLES `file_data` WRITE;
/*!40000 ALTER TABLE `file_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_server_active_r`
--

DROP TABLE IF EXISTS `file_server_active_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_server_active_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `server_ip` varchar(128) NOT NULL DEFAULT '0' COMMENT 'ip',
  `key` varchar(128) NOT NULL DEFAULT '0' COMMENT 'key',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `server_ip_key` (`server_ip`,`key`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=157 DEFAULT CHARSET=utf8 COMMENT='default_page_server_active';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_server_active_r`
--

LOCK TABLES `file_server_active_r` WRITE;
/*!40000 ALTER TABLE `file_server_active_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_server_active_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `log_operation_log`
--

DROP TABLE IF EXISTS `log_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` varchar(128) NOT NULL DEFAULT 'Unknown' COMMENT 'type',
  `target_id` varchar(128) NOT NULL DEFAULT '0' COMMENT 'target id',
  `operation` varchar(128) NOT NULL DEFAULT 'Unknown' COMMENT 'operation',
  `data` varchar(10240) DEFAULT NULL COMMENT 'data',
  `user_name` varchar(128) DEFAULT NULL COMMENT 'user name',
  `client_ip` varchar(128) DEFAULT NULL COMMENT 'client ip',
  `success` bit(1) NOT NULL DEFAULT b'0' COMMENT 'success',
  `err_msg` varchar(6144) DEFAULT NULL COMMENT 'err msg',
  `datetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datetime',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_datetime` (`datetime`),
  KEY `type_target_id_operation_user_name_client_ip_datetime_success` (`type`,`target_id`,`operation`,`user_name`,`client_ip`,`datetime`,`success`),
  KEY `user_name` (`user_name`),
  KEY `target_id` (`target_id`),
  KEY `operation` (`operation`),
  KEY `client_ip` (`client_ip`),
  KEY `success` (`success`)
) ENGINE=InnoDB AUTO_INCREMENT=2254672 DEFAULT CHARSET=utf8 COMMENT='operation log';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `log_operation_log`
--

LOCK TABLES `log_operation_log` WRITE;
/*!40000 ALTER TABLE `log_operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `log_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_queue`
--

DROP TABLE IF EXISTS `message_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message_queue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `performer` varchar(128) DEFAULT NULL COMMENT 'p',
  `type` varchar(128) NOT NULL DEFAULT 'unknown' COMMENT 'type',
  `target_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'tid',
  `target_data` varchar(5120) NOT NULL DEFAULT '0' COMMENT 'td',
  `status` varchar(128) NOT NULL DEFAULT 'unknow' COMMENT 'status',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'ct',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'dt',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `status_create_time` (`status`,`create_time`),
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2504621 DEFAULT CHARSET=utf8 COMMENT='message_queue';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_queue`
--

LOCK TABLES `message_queue` WRITE;
/*!40000 ALTER TABLE `message_queue` DISABLE KEYS */;
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504567,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:26:41','2019-10-25 02:26:42');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504568,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:26:41','2019-10-25 02:26:42');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504569,NULL,'/api/auth/user/resources',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/user/resources\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:27:41','2019-10-25 02:27:42');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504570,NULL,'/api/auth/user/resources',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/user/resources\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:28:41','2019-10-25 02:28:42');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504571,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:29:10','2019-10-25 02:29:11');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504572,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:29:10','2019-10-25 02:29:11');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504573,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:29:11','2019-10-25 02:29:12');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504574,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:29:15','2019-10-25 02:29:16');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504575,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"ips\" : [ ],\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"group-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:29:16','2019-10-25 02:29:17');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504576,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:30:13','2019-10-25 02:30:14');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504577,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:30:15','2019-10-25 02:30:14');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504578,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:30:47','2019-10-25 02:30:49');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504579,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:30:49','2019-10-25 02:30:49');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504580,NULL,'/api/auth/user/resources',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/user/resources\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:31:48','2019-10-25 02:31:50');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504581,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:32:49','2019-10-25 02:32:50');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504582,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:32:51','2019-10-25 02:32:50');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504583,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Bad Request Query Param.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:32:51','2019-10-25 02:32:51');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504584,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Bad Request Query Param.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:32:51','2019-10-25 02:32:51');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504585,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:32:57','2019-10-25 02:32:57');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504586,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Bad Request Query Param.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:32:57','2019-10-25 02:32:57');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504587,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Bad Request Query Param.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:32:57','2019-10-25 02:32:57');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504588,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:33:29','2019-10-25 02:33:30');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504589,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:33:31','2019-10-25 02:33:30');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504590,NULL,'/api/user/list',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/user/list\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"\\n### Error querying database.  Cause: com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Table \'open.auth_user_password\' doesn\'t exist\\n### The error may exist in com/ctrip/zeus/dao/mapper/AuthUserPasswordMapper.xml\\n### The error may involve com.ctrip.zeus.dao.mapper.AuthUserPasswordMapper.selectByExample-Inline\\n### The error occurred while setting parameters\\n### SQL: select                 id, user_name, `password`, DataChange_LastTime         from auth_user_password\\n### Cause: com.mysql.jdbc.except\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:33:31','2019-10-25 02:33:30');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504591,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:33:32','2019-10-25 02:33:33');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504592,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:33:34','2019-10-25 02:33:33');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504593,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"roleName=slbVisitor\",\n  \"error-message\" : \"Role Not Found.Id: null name: slbVisitor\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:33:34','2019-10-25 02:33:33');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504594,NULL,'/api/auth/role/new',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role/new\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Invalidate Data. Role data must have more than one AuthResources.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:33:51','2019-10-25 02:33:51');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504595,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"roleName=slbVisitor\",\n  \"error-message\" : \"Role Not Found.Id: null name: slbVisitor\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:02','2019-10-25 02:34:01');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504596,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:34:05','2019-10-25 02:34:06');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504597,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:07','2019-10-25 02:34:06');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504598,NULL,'/api/user/list',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/user/list\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"\\n### Error querying database.  Cause: com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Table \'open.auth_user_password\' doesn\'t exist\\n### The error may exist in com/ctrip/zeus/dao/mapper/AuthUserPasswordMapper.xml\\n### The error may involve com.ctrip.zeus.dao.mapper.AuthUserPasswordMapper.selectByExample-Inline\\n### The error occurred while setting parameters\\n### SQL: select                 id, user_name, `password`, DataChange_LastTime         from auth_user_password\\n### Cause: com.mysql.jdbc.except\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:07','2019-10-25 02:34:06');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504599,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:34:08','2019-10-25 02:34:09');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504600,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:10','2019-10-25 02:34:09');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504601,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"roleName=slbVisitor\",\n  \"error-message\" : \"Role Not Found.Id: null name: slbVisitor\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:10','2019-10-25 02:34:09');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504602,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:34:10','2019-10-25 02:34:11');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504603,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:12','2019-10-25 02:34:12');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504604,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:34:12','2019-10-25 02:34:13');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504605,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:14','2019-10-25 02:34:13');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504606,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:34:13','2019-10-25 02:34:14');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504607,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:15','2019-10-25 02:34:14');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504608,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:34:14','2019-10-25 02:34:16');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504609,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:16','2019-10-25 02:34:16');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504610,NULL,'/api/auth/role',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role\",\n  \"group-datas\" : [ ],\n  \"query\" : \"roleName=slbVisitor\",\n  \"error-message\" : \"Role Not Found.Id: null name: slbVisitor\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:16','2019-10-25 02:34:16');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504611,NULL,'/api/auth/role/new',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/role/new\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Invalidate Data. Role data must have more than one AuthResources.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 02:34:21','2019-10-25 02:34:20');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504612,NULL,'/api/auth/user/resources',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"uri\" : \"/api/auth/user/resources\",\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ],\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ]\r\n}','TODO','2019-10-25 02:35:14','2019-10-25 02:35:16');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504613,NULL,'/api/auth/user/update',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/update\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Not Found User By User Id.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 03:25:34','2019-10-25 03:25:34');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504614,NULL,'/api/auth/user/update',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/update\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Invalid data.Role id is null.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 03:29:57','2019-10-25 03:29:56');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504615,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 03:43:56','2019-10-25 03:43:56');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504616,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ]\r\n}','TODO','2019-10-25 03:43:56','2019-10-25 03:43:56');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504617,NULL,'/api/auth/current/user',0,'{\r\n  \"query\" : \"\",\r\n  \"success\" : false,\r\n  \"error-message\" : \"Unknown User.\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ],\r\n  \"uri\" : \"/api/auth/current/user\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ]\r\n}','TODO','2019-10-25 03:43:56','2019-10-25 03:43:56');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504618,NULL,'/api/auth/user/resources',0,'{\n  \"success\" : false,\n  \"uri\" : \"/api/auth/user/resources\",\n  \"group-datas\" : [ ],\n  \"query\" : \"\",\n  \"error-message\" : \"Unknown User.\",\n  \"client-ip\" : \"127.0.0.1\",\n  \"ips\" : [ ],\n  \"dr-datas\" : [ ],\n  \"policy-datas\" : [ ],\n  \"rule-datas\" : [ ],\n  \"slb-datas\" : [ ],\n  \"vs-datas\" : [ ]\n}','TODO','2019-10-25 03:44:03','2019-10-25 03:44:03');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504619,NULL,'/api/user/login',0,'{\r\n  \"query\" : \"password=admin&userName=admin\",\r\n  \"success\" : false,\r\n  \"error-message\" : \"Username or password is not matched\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ],\r\n  \"uri\" : \"/api/user/login\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ]\r\n}','TODO','2019-10-25 03:44:10','2019-10-25 03:44:09');
INSERT INTO `message_queue` (`id`, `performer`, `type`, `target_id`, `target_data`, `status`, `create_time`, `DataChange_LastTime`) VALUES (2504620,NULL,'/api/user/login',0,'{\r\n  \"query\" : \"password=admin&userName=admin\",\r\n  \"success\" : false,\r\n  \"error-message\" : \"Username or password is not matched\",\r\n  \"dr-datas\" : [ ],\r\n  \"policy-datas\" : [ ],\r\n  \"rule-datas\" : [ ],\r\n  \"slb-datas\" : [ ],\r\n  \"vs-datas\" : [ ],\r\n  \"uri\" : \"/api/user/login\",\r\n  \"client-ip\" : \"127.0.0.1\",\r\n  \"ips\" : [ ],\r\n  \"group-datas\" : [ ]\r\n}','TODO','2019-10-25 03:44:26','2019-10-25 03:44:25');
/*!40000 ALTER TABLE `message_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nginx_conf`
--

DROP TABLE IF EXISTS `nginx_conf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nginx_conf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=12167109 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nginx_conf`
--

LOCK TABLES `nginx_conf` WRITE;
/*!40000 ALTER TABLE `nginx_conf` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_conf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nginx_conf_slb`
--

DROP TABLE IF EXISTS `nginx_conf_slb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nginx_conf_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb conf version',
  `content` mediumblob NOT NULL COMMENT 'conf content',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=12069134 DEFAULT CHARSET=utf8 COMMENT='conf file of each slb cluster';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nginx_conf_slb`
--

LOCK TABLES `nginx_conf_slb` WRITE;
/*!40000 ALTER TABLE `nginx_conf_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_conf_slb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nginx_model_snapshot`
--

DROP TABLE IF EXISTS `nginx_model_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nginx_model_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slbId',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'version',
  `content` mediumblob NOT NULL COMMENT 'content',
  `DataChange_LastTime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `slbId` (`slb_id`),
  KEY `version` (`version`),
  KEY `slbId_version` (`slb_id`,`version`),
  KEY `datechange` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='nginx_model_snapshot';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nginx_model_snapshot`
--

LOCK TABLES `nginx_model_snapshot` WRITE;
/*!40000 ALTER TABLE `nginx_model_snapshot` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_model_snapshot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nginx_server`
--

DROP TABLE IF EXISTS `nginx_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nginx_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(200) DEFAULT NULL,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=343 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nginx_server`
--

LOCK TABLES `nginx_server` WRITE;
/*!40000 ALTER TABLE `nginx_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_server` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_snapshot`
--

DROP TABLE IF EXISTS `report_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `report_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `target_type` varchar(128) NOT NULL DEFAULT 'undefind' COMMENT 'target_type',
  `agg_key` varchar(128) DEFAULT 'undefind' COMMENT 'agg_key',
  `agg_value` varchar(128) DEFAULT 'undefind' COMMENT 'agg_value',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create_time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  `count` bigint(20) NOT NULL DEFAULT '0' COMMENT 'count',
  `target_tag` varchar(128) DEFAULT NULL COMMENT 'target_tag',
  PRIMARY KEY (`id`),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `target_type` (`target_type`),
  KEY `time_index` (`create_time`),
  KEY `agg_index` (`agg_key`,`agg_value`)
) ENGINE=InnoDB AUTO_INCREMENT=5019015 DEFAULT CHARSET=utf8mb4 COMMENT='snapshot';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_snapshot`
--

LOCK TABLES `report_snapshot` WRITE;
/*!40000 ALTER TABLE `report_snapshot` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_snapshot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_rule`
--

DROP TABLE IF EXISTS `rule_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'rule name',
  `rule_type` int(11) NOT NULL DEFAULT '0' COMMENT 'rule type',
  `attributes` mediumtext NOT NULL COMMENT 'rule attributes',
  `content` mediumtext NOT NULL COMMENT 'data of the rule',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=237 DEFAULT CHARSET=utf8 COMMENT='rule for config generation';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_rule`
--

LOCK TABLES `rule_rule` WRITE;
/*!40000 ALTER TABLE `rule_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `rule_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_rule_target_r`
--

DROP TABLE IF EXISTS `rule_rule_target_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule_rule_target_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `rule_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `target_id` char(20) NOT NULL DEFAULT '0' COMMENT 'rule target id',
  `target_type` char(20) NOT NULL DEFAULT '0' COMMENT 'rule target type',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `table_uniq` (`rule_id`),
  KEY `data_INDEX` (`target_type`,`target_id`),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8 COMMENT='rule relation for target and rule';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_rule_target_r`
--

LOCK TABLES `rule_rule_target_r` WRITE;
/*!40000 ALTER TABLE `rule_rule_target_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `rule_rule_target_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_app`
--

DROP TABLE IF EXISTS `slb_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `app_id` varchar(128) NOT NULL DEFAULT 'unknown' COMMENT 'appid',
  `sbu` varchar(256) NOT NULL DEFAULT 'unknown' COMMENT 's',
  `content` mediumtext NOT NULL COMMENT 'data of the ap',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id` (`app_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1141160 DEFAULT CHARSET=utf8 COMMENT='app';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_app`
--

LOCK TABLES `slb_app` WRITE;
/*!40000 ALTER TABLE `slb_app` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_archive_dr`
--

DROP TABLE IF EXISTS `slb_archive_dr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_archive_dr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `dr_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'dr id',
  `content` mediumtext COMMENT 'content',
  `version` int(11) DEFAULT NULL COMMENT 'version',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'created time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dr_id_version` (`dr_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8 COMMENT='dr data table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_archive_dr`
--

LOCK TABLES `slb_archive_dr` WRITE;
/*!40000 ALTER TABLE `slb_archive_dr` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_archive_dr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_archive_group`
--

DROP TABLE IF EXISTS `slb_archive_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_archive_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `content` mediumtext COMMENT '空',
  `version` int(11) DEFAULT NULL COMMENT '空',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_version` (`group_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB AUTO_INCREMENT=1401008 DEFAULT CHARSET=utf8 COMMENT='slb archive group';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_archive_group`
--

LOCK TABLES `slb_archive_group` WRITE;
/*!40000 ALTER TABLE `slb_archive_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_archive_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_archive_slb`
--

DROP TABLE IF EXISTS `slb_archive_slb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_archive_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `content` mediumtext COMMENT '空',
  `version` int(11) DEFAULT NULL COMMENT '空',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB AUTO_INCREMENT=1033 DEFAULT CHARSET=utf8 COMMENT='slb archive slb';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_archive_slb`
--

LOCK TABLES `slb_archive_slb` WRITE;
/*!40000 ALTER TABLE `slb_archive_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_archive_slb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_archive_traffic_policy`
--

DROP TABLE IF EXISTS `slb_archive_traffic_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_archive_traffic_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `policy_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'policy id',
  `policy_name` varchar(255) NOT NULL DEFAULT '0' COMMENT 'policy name',
  `content` varchar(2048) NOT NULL DEFAULT '0' COMMENT 'content',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version(0)',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `policy_id` (`policy_id`),
  KEY `name` (`policy_name`),
  KEY `version` (`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1302 DEFAULT CHARSET=utf8 COMMENT='table for traffic_policy archive';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_archive_traffic_policy`
--

LOCK TABLES `slb_archive_traffic_policy` WRITE;
/*!40000 ALTER TABLE `slb_archive_traffic_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_archive_traffic_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_archive_vs`
--

DROP TABLE IF EXISTS `slb_archive_vs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_archive_vs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'virtual server id',
  `content` mediumtext NOT NULL COMMENT 'content',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DateTime_LastChange` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vs_id_version` (`vs_id`,`version`),
  KEY `DateTime_LastChange` (`DateTime_LastChange`),
  KEY `hash` (`hash`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=27584 DEFAULT CHARSET=utf8 COMMENT='table of virtual server archive';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_archive_vs`
--

LOCK TABLES `slb_archive_vs` WRITE;
/*!40000 ALTER TABLE `slb_archive_vs` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_archive_vs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_build_commit`
--

DROP TABLE IF EXISTS `slb_build_commit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_build_commit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary',
  `version` bigint(20) DEFAULT NULL COMMENT 'version',
  `slb_id` bigint(20) DEFAULT NULL COMMENT 'slb id',
  `vs_ids` varchar(8124) DEFAULT NULL COMMENT 'vs ids',
  `group_ids` varchar(4096) DEFAULT NULL COMMENT 'group ids',
  `task_ids` varchar(4096) DEFAULT NULL COMMENT 'task ids',
  `cleanvs_ids` varchar(1024) DEFAULT NULL COMMENT 'cleanvs ids',
  `type` varchar(45) DEFAULT NULL COMMENT 'type',
  `DataChange_LastTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'date time last changed',
  PRIMARY KEY (`id`),
  KEY `version_slb_id` (`version`,`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=12084510 DEFAULT CHARSET=utf8 COMMENT='commit table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_build_commit`
--

LOCK TABLES `slb_build_commit` WRITE;
/*!40000 ALTER TABLE `slb_build_commit` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_build_commit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_build_ticket`
--

DROP TABLE IF EXISTS `slb_build_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_build_ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `pending_ticket` int(11) DEFAULT NULL COMMENT '空',
  `current_ticket` int(11) DEFAULT NULL COMMENT '空',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8 COMMENT='slb build ticket';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_build_ticket`
--

LOCK TABLES `slb_build_ticket` WRITE;
/*!40000 ALTER TABLE `slb_build_ticket` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_build_ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_conf_slb_version`
--

DROP TABLE IF EXISTS `slb_conf_slb_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_conf_slb_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `previous_version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb previous version',
  `current_version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb current version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8 COMMENT='slb conf version table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_conf_slb_version`
--

LOCK TABLES `slb_conf_slb_version` WRITE;
/*!40000 ALTER TABLE `slb_conf_slb_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_conf_slb_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_config`
--

DROP TABLE IF EXISTS `slb_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `property_key` varchar(128) NOT NULL DEFAULT '' COMMENT 'property_key',
  `property_value` varchar(512) NOT NULL DEFAULT '' COMMENT 'property_value',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datechange',
  PRIMARY KEY (`id`),
  UNIQUE KEY `property_key_index` (`property_key`),
  KEY `datechange` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='slb_config';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_config`
--

LOCK TABLES `slb_config` WRITE;
/*!40000 ALTER TABLE `slb_config` DISABLE KEYS */;
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (1,'auth.header.token.df2294b0333949eeaaf1ec2615723c7b','slbServer','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (2,'auth.header.token.tf2294b0333949eeaaf1ec2615723c7b','healthChecker','2019-10-25 03:43:36');

INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (3,'cookies.domain','.localhost','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (3,'token.cookies.max.age','600','2019-10-25 03:43:36');

INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (3,'mail.host','http://mail.host/emailservice','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (4,'smtp.mail.server.host','mail.server.com','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (5,'smtp.mail.server.port',25,'2019-10-25 03:43:36');


INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (6,'slb.client.proxy.server','proxyserver:8080','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (7,'slb.client.private.zone.list','privatedomain1;privatedomain2','2019-10-25 03:43:36');

INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (8,'allow.origin','http://domain1.com,http://domain2.com','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (9,'allow.header','Content-Type,Target-Url,slb_token,Target-Method,UserCookie,_stok','2019-10-25 03:43:36');

INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (10,'slb.portal.url','http://locahost/portal/','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (11,'slb.portal.url.env','pro','2019-10-25 03:43:36');

INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (12,'auth.apply.apply.recipients','alias@mail.com;alias2@mail.com','2019-10-25 03:43:36');
INSERT INTO `slb_config` (`id`, `property_key`, `property_value`, `DataChange_LastTime`) VALUES (12,'slb.team.mail','alias@mail.com','2019-10-25 03:43:36');
/*!40000 ALTER TABLE `slb_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_dr`
--

DROP TABLE IF EXISTS `slb_dr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_dr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `name` varchar(200) NOT NULL DEFAULT '0' COMMENT '空',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '空',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8 COMMENT='dr';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_dr`
--

LOCK TABLES `slb_dr` WRITE;
/*!40000 ALTER TABLE `slb_dr` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_dr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_dr_status_r`
--

DROP TABLE IF EXISTS `slb_dr_status_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_dr_status_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `dr_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'dr id',
  `offline_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `online_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dr_id` (`dr_id`),
  KEY `offline_version` (`offline_version`),
  KEY `online_version` (`online_version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='dr status table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_dr_status_r`
--

LOCK TABLES `slb_dr_status_r` WRITE;
/*!40000 ALTER TABLE `slb_dr_status_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_dr_status_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_dr_vs_r`
--

DROP TABLE IF EXISTS `slb_dr_vs_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_dr_vs_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `dr_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'dr id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vs_id',
  `dr_version` int(11) DEFAULT NULL COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_dr_id` (`dr_id`),
  KEY `vs_id` (`vs_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8 COMMENT='relation table of vs and dr';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_dr_vs_r`
--

LOCK TABLES `slb_dr_vs_r` WRITE;
/*!40000 ALTER TABLE `slb_dr_vs_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_dr_vs_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_group`
--

DROP TABLE IF EXISTS `slb_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL DEFAULT '0',
  `app_id` varchar(200) NOT NULL DEFAULT '0',
  `version` int(11) NOT NULL DEFAULT '0',
  `ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=25611 DEFAULT CHARSET=utf8 COMMENT='slb group';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_group`
--

LOCK TABLES `slb_group` WRITE;
/*!40000 ALTER TABLE `slb_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_group_gs_r`
--

DROP TABLE IF EXISTS `slb_group_gs_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_group_gs_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group_id',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'group_server ip',
  `group_version` int(11) NOT NULL DEFAULT '0' COMMENT 'group version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_group_id` (`group_id`),
  KEY `ip` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=2986731 DEFAULT CHARSET=utf8 COMMENT='relation table of group and group server ip';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_group_gs_r`
--

LOCK TABLES `slb_group_gs_r` WRITE;
/*!40000 ALTER TABLE `slb_group_gs_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_group_gs_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_group_history`
--

DROP TABLE IF EXISTS `slb_group_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_group_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `group_name` varchar(255) NOT NULL DEFAULT 'undefined' COMMENT 'group name',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `group_name` (`group_name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=6954 DEFAULT CHARSET=utf8 COMMENT='deleted group record';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_group_history`
--

LOCK TABLES `slb_group_history` WRITE;
/*!40000 ALTER TABLE `slb_group_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_group_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_group_server_status`
--

DROP TABLE IF EXISTS `slb_group_server_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_group_server_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'gid',
  `ip` varchar(200) NOT NULL DEFAULT 'UNKNOW' COMMENT 'ip',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT 'status',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'ct',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'dt',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_id_ip` (`group_id`,`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_create_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=24421849 DEFAULT CHARSET=utf8 COMMENT='group_server_status';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_group_server_status`
--

LOCK TABLES `slb_group_server_status` WRITE;
/*!40000 ALTER TABLE `slb_group_server_status` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_group_server_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_group_status_r`
--

DROP TABLE IF EXISTS `slb_group_status_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_group_status_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `offline_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `online_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  `canary_version` int(11) DEFAULT NULL COMMENT 'canary version',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `offline_version` (`offline_version`),
  KEY `online_version` (`online_version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=464470 DEFAULT CHARSET=utf8 COMMENT='group status table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_group_status_r`
--

LOCK TABLES `slb_group_status_r` WRITE;
/*!40000 ALTER TABLE `slb_group_status_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_group_status_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_group_vs_r`
--

DROP TABLE IF EXISTS `slb_group_vs_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_group_vs_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group_id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vs_id',
  `path` varchar(4096) DEFAULT NULL COMMENT 'path',
  `priority` int(11) NOT NULL DEFAULT '1000' COMMENT 'priority',
  `group_version` int(11) NOT NULL DEFAULT '0' COMMENT 'group version',
  `content` mediumblob COMMENT '空',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_vs_id` (`vs_id`),
  KEY `group_id` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=484837 DEFAULT CHARSET=utf8 COMMENT='relation table of group and vs';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_group_vs_r`
--

LOCK TABLES `slb_group_vs_r` WRITE;
/*!40000 ALTER TABLE `slb_group_vs_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_group_vs_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_server_status`
--

DROP TABLE IF EXISTS `slb_server_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_server_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `ip` varchar(200) NOT NULL DEFAULT '' COMMENT '空',
  `up` bit(1) NOT NULL DEFAULT b'0' COMMENT '空',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='slb group';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_server_status`
--

LOCK TABLES `slb_server_status` WRITE;
/*!40000 ALTER TABLE `slb_server_status` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_server_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_slb`
--

DROP TABLE IF EXISTS `slb_slb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `name` varchar(100) NOT NULL DEFAULT '0' COMMENT '空',
  `status` varchar(300) NOT NULL DEFAULT '0' COMMENT '空',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '空',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '空',
  `content` mediumtext NOT NULL COMMENT 'data of the slb',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=151 DEFAULT CHARSET=utf8 COMMENT='slb ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_slb`
--

LOCK TABLES `slb_slb` WRITE;
/*!40000 ALTER TABLE `slb_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_slb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_slb_server_r`
--

DROP TABLE IF EXISTS `slb_slb_server_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_slb_server_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'slb_server ip',
  `slb_version` int(11) NOT NULL DEFAULT '0' COMMENT 'slb version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `slb_id` (`slb_id`),
  KEY `ip` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=354 DEFAULT CHARSET=utf8 COMMENT='relation table of slb and slb server';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_slb_server_r`
--

LOCK TABLES `slb_slb_server_r` WRITE;
/*!40000 ALTER TABLE `slb_slb_server_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_slb_server_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_slb_status_r`
--

DROP TABLE IF EXISTS `slb_slb_status_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_slb_status_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `online_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online version',
  `offline_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `online_version` (`online_version`),
  KEY `offline_version` (`offline_version`)
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8 COMMENT='slb status table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_slb_status_r`
--

LOCK TABLES `slb_slb_status_r` WRITE;
/*!40000 ALTER TABLE `slb_slb_status_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_slb_status_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_traffic_policy`
--

DROP TABLE IF EXISTS `slb_traffic_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_traffic_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` varchar(255) NOT NULL DEFAULT 'null' COMMENT 'policy name',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'lastest version',
  `nx_active_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `active_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online verion',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `version` (`version`),
  KEY `nx_active_version` (`nx_active_version`),
  KEY `active_version` (`active_version`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1340 DEFAULT CHARSET=utf8 COMMENT='table for traffic policy and version status';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_traffic_policy`
--

LOCK TABLES `slb_traffic_policy` WRITE;
/*!40000 ALTER TABLE `slb_traffic_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_traffic_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_traffic_policy_group_r`
--

DROP TABLE IF EXISTS `slb_traffic_policy_group_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_traffic_policy_group_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'control id',
  `policy_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'policy id',
  `policy_version` int(11) NOT NULL DEFAULT '0' COMMENT 'related policy version',
  `weight` int(11) NOT NULL DEFAULT '0' COMMENT 'proxying weight',
  `hash` int(11) NOT NULL DEFAULT '0' COMMENT 'hash',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `policy_id_policy_version` (`policy_id`,`policy_version`),
  KEY `group_id` (`group_id`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB AUTO_INCREMENT=1273 DEFAULT CHARSET=utf8 COMMENT='table for traffic policy and group relationship maintenance';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_traffic_policy_group_r`
--

LOCK TABLES `slb_traffic_policy_group_r` WRITE;
/*!40000 ALTER TABLE `slb_traffic_policy_group_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_traffic_policy_group_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_traffic_policy_vs_r`
--

DROP TABLE IF EXISTS `slb_traffic_policy_vs_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_traffic_policy_vs_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vs id',
  `policy_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'traffic policy id',
  `policy_version` int(11) NOT NULL DEFAULT '0' COMMENT 'traffic policy verion',
  `path` varchar(4096) NOT NULL DEFAULT '0' COMMENT 'path',
  `priority` int(11) NOT NULL DEFAULT '1000' COMMENT 'priority',
  `hash` int(11) NOT NULL DEFAULT '0' COMMENT 'hash',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `vs_id` (`vs_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `policy_id_policy_version` (`policy_id`,`policy_version`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB AUTO_INCREMENT=777 DEFAULT CHARSET=utf8 COMMENT='table for traffic policy and vs relationship maintenance';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_traffic_policy_vs_r`
--

LOCK TABLES `slb_traffic_policy_vs_r` WRITE;
/*!40000 ALTER TABLE `slb_traffic_policy_vs_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_traffic_policy_vs_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_virtual_server`
--

DROP TABLE IF EXISTS `slb_virtual_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_virtual_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) DEFAULT NULL,
  `name` varchar(200) NOT NULL DEFAULT '0',
  `port` varchar(200) NOT NULL DEFAULT '0',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `is_ssl` (`is_ssl`)
) ENGINE=InnoDB AUTO_INCREMENT=15567 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_virtual_server`
--

LOCK TABLES `slb_virtual_server` WRITE;
/*!40000 ALTER TABLE `slb_virtual_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_virtual_server` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_vs_domain_r`
--

DROP TABLE IF EXISTS `slb_vs_domain_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_vs_domain_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `domain` varchar(200) NOT NULL DEFAULT 'Undefined' COMMENT 'slb_domain_name',
  `vs_version` int(11) NOT NULL DEFAULT '0' COMMENT 'vs version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `vs_id` (`vs_id`),
  KEY `domain` (`domain`)
) ENGINE=InnoDB AUTO_INCREMENT=30283 DEFAULT CHARSET=utf8 COMMENT='relation table of vs and domain';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_vs_domain_r`
--

LOCK TABLES `slb_vs_domain_r` WRITE;
/*!40000 ALTER TABLE `slb_vs_domain_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_vs_domain_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_vs_slb_r`
--

DROP TABLE IF EXISTS `slb_vs_slb_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_vs_slb_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `vs_version` int(11) NOT NULL DEFAULT '0' COMMENT 'vs version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_vs_id` (`vs_id`),
  KEY `slb_id` (`slb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27920 DEFAULT CHARSET=utf8 COMMENT='relation table of slb and vs';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_vs_slb_r`
--

LOCK TABLES `slb_vs_slb_r` WRITE;
/*!40000 ALTER TABLE `slb_vs_slb_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_vs_slb_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slb_vs_status_r`
--

DROP TABLE IF EXISTS `slb_vs_status_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slb_vs_status_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vs id',
  `offline_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `online_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vs_id` (`vs_id`),
  KEY `offline_version` (`offline_version`),
  KEY `online_version` (`online_version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=25287 DEFAULT CHARSET=utf8 COMMENT='vs status table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slb_vs_status_r`
--

LOCK TABLES `slb_vs_status_r` WRITE;
/*!40000 ALTER TABLE `slb_vs_status_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_vs_status_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `soa_service_info`
--

DROP TABLE IF EXISTS `soa_service_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `soa_service_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `service_code` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'service_code',
  `service_id` varchar(256) NOT NULL DEFAULT 'unknown' COMMENT 'service_id',
  `app_ids` varchar(512) NOT NULL DEFAULT 'unknown' COMMENT 'app_ids',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  `ip_direct` bit(1) NOT NULL DEFAULT b'1' COMMENT 'ip_direct',
  PRIMARY KEY (`id`),
  UNIQUE KEY `servicecode` (`service_code`),
  KEY `serviceId` (`service_id`(191)),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=3431862 DEFAULT CHARSET=utf8mb4 COMMENT='soa_service_info';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `soa_service_info`
--

LOCK TABLES `soa_service_info` WRITE;
/*!40000 ALTER TABLE `soa_service_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `soa_service_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stats_group_slb`
--

DROP TABLE IF EXISTS `stats_group_slb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stats_group_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `val_status` int(11) NOT NULL DEFAULT '0' COMMENT 'status value',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `group_id` (`group_id`),
  KEY `slb_id` (`slb_id`),
  KEY `val_status` (`val_status`)
) ENGINE=InnoDB AUTO_INCREMENT=6621 DEFAULT CHARSET=utf8 COMMENT='table to store group related statistics by slb';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stats_group_slb`
--

LOCK TABLES `stats_group_slb` WRITE;
/*!40000 ALTER TABLE `stats_group_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `stats_group_slb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status_check_count_slb`
--

DROP TABLE IF EXISTS `status_check_count_slb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `status_check_count_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT 'count',
  `data_set` varchar(255) NOT NULL DEFAULT '0' COMMENT 'group data set',
  `data_set_timestamp` bigint(20) DEFAULT NULL COMMENT 'data set last modified time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8 COMMENT='table for slb status check count';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status_check_count_slb`
--

LOCK TABLES `status_check_count_slb` WRITE;
/*!40000 ALTER TABLE `status_check_count_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_check_count_slb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status_stats_group_slb`
--

DROP TABLE IF EXISTS `status_stats_group_slb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `status_stats_group_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `val_status` int(11) NOT NULL DEFAULT '0' COMMENT 'status value',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `group_id` (`group_id`),
  KEY `slb_id` (`slb_id`),
  KEY `val_status` (`val_status`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8 COMMENT='table to store group related statistics by slb';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status_stats_group_slb`
--

LOCK TABLES `status_stats_group_slb` WRITE;
/*!40000 ALTER TABLE `status_stats_group_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_stats_group_slb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag_property`
--

DROP TABLE IF EXISTS `tag_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `property_name` varchar(255) NOT NULL DEFAULT '0' COMMENT 'property name',
  `property_value` varchar(255) NOT NULL DEFAULT '0' COMMENT 'property value',
  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `property_name_property_value` (`property_name`,`property_value`),
  KEY `property_name` (`property_name`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=35258 DEFAULT CHARSET=utf8 COMMENT='property table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag_property`
--

LOCK TABLES `tag_property` WRITE;
/*!40000 ALTER TABLE `tag_property` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag_property_item_r`
--

DROP TABLE IF EXISTS `tag_property_item_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_property_item_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `property_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'property ref id',
  `item_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'item id',
  `type` varchar(255) NOT NULL DEFAULT 'Undefined' COMMENT 'item type',
  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `property_id_item_id_type` (`property_id`,`item_id`,`type`),
  KEY `type` (`type`),
  KEY `property_id` (`property_id`),
  KEY `item_id` (`item_id`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=482258 DEFAULT CHARSET=utf8 COMMENT='property item';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag_property_item_r`
--

LOCK TABLES `tag_property_item_r` WRITE;
/*!40000 ALTER TABLE `tag_property_item_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag_property_item_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag_tag`
--

DROP TABLE IF EXISTS `tag_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary',
  `name` varchar(255) NOT NULL DEFAULT '0' COMMENT 'tag name',
  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=36835 DEFAULT CHARSET=utf8 COMMENT='tag def table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag_tag`
--

LOCK TABLES `tag_tag` WRITE;
/*!40000 ALTER TABLE `tag_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag_tag_item_r`
--

DROP TABLE IF EXISTS `tag_tag_item_r`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_tag_item_r` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary',
  `tag_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'tag def id',
  `item_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'item ref id',
  `type` varchar(255) NOT NULL DEFAULT 'Undefined' COMMENT 'item type',
  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tag_id_item_id_type` (`tag_id`,`item_id`,`type`),
  KEY `tag_id` (`tag_id`),
  KEY `item_id` (`item_id`),
  KEY `type` (`type`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=18196790 DEFAULT CHARSET=utf8 COMMENT='tag item mapping';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag_tag_item_r`
--

LOCK TABLES `tag_tag_item_r` WRITE;
/*!40000 ALTER TABLE `tag_tag_item_r` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag_tag_item_r` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_execute_record`
--

DROP TABLE IF EXISTS `task_execute_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_execute_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `task_key` varchar(128) NOT NULL DEFAULT 'UNKNOW' COMMENT 'task key',
  `last_execute_time` bigint(20) NOT NULL DEFAULT '0' COMMENT 'last execute time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_key` (`task_key`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=25305 DEFAULT CHARSET=utf8 COMMENT='task_execute_record';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_execute_record`
--

LOCK TABLES `task_execute_record` WRITE;
/*!40000 ALTER TABLE `task_execute_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_execute_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_global_job`
--

DROP TABLE IF EXISTS `task_global_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_global_job` (
  `job_key` varchar(254) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'job key',
  `owner` varchar(256) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'owner',
  `status` varchar(256) DEFAULT NULL COMMENT 'status',
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'start time',
  `finish_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'finish_time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`job_key`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='global job';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_global_job`
--

LOCK TABLES `task_global_job` WRITE;
/*!40000 ALTER TABLE `task_global_job` DISABLE KEYS */;
INSERT INTO `task_global_job` (`job_key`, `owner`, `status`, `start_time`, `finish_time`, `DataChange_LastTime`) VALUES ('HealthCheckMetric_null','127.0.0.1','DONE','2019-10-24 12:04:05','2019-10-24 12:04:06','2019-10-24 12:04:06');
/*!40000 ALTER TABLE `task_global_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_task`
--

DROP TABLE IF EXISTS `task_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ops_type` varchar(50) NOT NULL DEFAULT 'UNDEFINE' COMMENT 'ops type',
  `group_id` bigint(20) DEFAULT NULL COMMENT 'group id',
  `policy_id` bigint(20) DEFAULT NULL COMMENT 'policy_id',
  `slb_id` bigint(20) DEFAULT NULL COMMENT 'slb id',
  `slb_virtual_server_id` bigint(20) DEFAULT NULL COMMENT 'vs id',
  `ip_list` varchar(4096) DEFAULT NULL COMMENT 'ip list',
  `up` bit(1) DEFAULT NULL COMMENT 'up',
  `status` varchar(50) NOT NULL DEFAULT 'UNDEFINE' COMMENT 'status',
  `target_slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'target slb id',
  `resources` varchar(128) DEFAULT NULL COMMENT 'resources',
  `version` int(11) DEFAULT '0' COMMENT 'version',
  `skip_validate` bit(1) NOT NULL DEFAULT b'0' COMMENT 'skip_validate',
  `fail_cause` varchar(1024) DEFAULT NULL COMMENT 'fail cause',
  `create_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  `task_list` mediumblob COMMENT 'task list',
  `dr_id` bigint(20) DEFAULT NULL COMMENT 'dr id',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_create_time` (`create_time`),
  KEY `status_target_slb_id` (`status`,`target_slb_id`),
  KEY `idx_target_slb_id_status` (`target_slb_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=20350753 DEFAULT CHARSET=utf8 COMMENT='task queue';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_task`
--

LOCK TABLES `task_task` WRITE;
/*!40000 ALTER TABLE `task_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tools_dns_switch`
--

DROP TABLE IF EXISTS `tools_dns_switch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_dns_switch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL DEFAULT 'unknown' COMMENT 'name',
  `status` varchar(64) DEFAULT NULL COMMENT 'status',
  `content` varchar(2048) DEFAULT NULL COMMENT 'content',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DateChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `time` (`DataChange_LastTime`),
  KEY `name` (`name`),
  KEY `status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COMMENT='flow_dns_switch';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tools_dns_switch`
--

LOCK TABLES `tools_dns_switch` WRITE;
/*!40000 ALTER TABLE `tools_dns_switch` DISABLE KEYS */;
/*!40000 ALTER TABLE `tools_dns_switch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tools_slb_creating`
--

DROP TABLE IF EXISTS `tools_slb_creating`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_slb_creating` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'name',
  `content` mediumblob COMMENT 'content',
  `status` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'status',
  `DataChange_LastTime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `status` (`status`),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8 COMMENT='flow_slb_creating';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tools_slb_creating`
--

LOCK TABLES `tools_slb_creating` WRITE;
/*!40000 ALTER TABLE `tools_slb_creating` DISABLE KEYS */;
/*!40000 ALTER TABLE `tools_slb_creating` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tools_slb_sharding`
--

DROP TABLE IF EXISTS `tools_slb_sharding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_slb_sharding` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'name',
  `status` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'status',
  `content` varchar(2048) DEFAULT NULL COMMENT 'content',
  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `status` (`status`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='flow_slb_sharding';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tools_slb_sharding`
--

LOCK TABLES `tools_slb_sharding` WRITE;
/*!40000 ALTER TABLE `tools_slb_sharding` DISABLE KEYS */;
/*!40000 ALTER TABLE `tools_slb_sharding` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tools_vs_merge`
--

DROP TABLE IF EXISTS `tools_vs_merge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_vs_merge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL DEFAULT 'unknown' COMMENT 'name',
  `status` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'status',
  `content` varchar(2048) NOT NULL DEFAULT 'NULL' COMMENT 'content',
  `datachange_lasttime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datachange_lasttime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `status` (`status`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='flow_vs_merge';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tools_vs_merge`
--

LOCK TABLES `tools_vs_merge` WRITE;
/*!40000 ALTER TABLE `tools_vs_merge` DISABLE KEYS */;
/*!40000 ALTER TABLE `tools_vs_merge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tools_vs_migration`
--

DROP TABLE IF EXISTS `tools_vs_migration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_vs_migration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `name` varchar(200) DEFAULT NULL COMMENT '空',
  `content` mediumblob COMMENT '空',
  `status` bit(1) NOT NULL DEFAULT b'1' COMMENT 'status',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `ix_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8 COMMENT='VS Migration Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tools_vs_migration`
--

LOCK TABLES `tools_vs_migration` WRITE;
/*!40000 ALTER TABLE `tools_vs_migration` DISABLE KEYS */;
/*!40000 ALTER TABLE `tools_vs_migration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tools_vs_split`
--

DROP TABLE IF EXISTS `tools_vs_split`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_vs_split` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL DEFAULT 'unknown' COMMENT 'name',
  `status` varchar(32) NOT NULL DEFAULT 'unknown' COMMENT 'status',
  `content` varchar(2048) NOT NULL DEFAULT 'NULL' COMMENT 'content',
  `datachange_lasttime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datachange_lasttime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `status` (`status`),
  KEY `ix_DataChange_LastTime` (`datachange_lasttime`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8 COMMENT='flow_vs_split';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tools_vs_split`
--

LOCK TABLES `tools_vs_split` WRITE;
/*!40000 ALTER TABLE `tools_vs_split` DISABLE KEYS */;
/*!40000 ALTER TABLE `tools_vs_split` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unhealthy_alert_item`
--

DROP TABLE IF EXISTS `unhealthy_alert_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unhealthy_alert_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '空',
  `content` mediumblob NOT NULL COMMENT 'content',
  `target` bigint(20) NOT NULL DEFAULT '0' COMMENT '空',
  `type` char(20) NOT NULL DEFAULT '' COMMENT '空',
  `status` bit(1) DEFAULT b'0' COMMENT '空',
  `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '空',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=201288 DEFAULT CHARSET=utf8 COMMENT='Alert items for slb objects';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unhealthy_alert_item`
--

LOCK TABLES `unhealthy_alert_item` WRITE;
/*!40000 ALTER TABLE `unhealthy_alert_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `unhealthy_alert_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waf_data`
--

DROP TABLE IF EXISTS `waf_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `waf_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `file_name` varchar(128) NOT NULL DEFAULT 'UNKNOW' COMMENT 'name',
  `data` mediumtext COMMENT 'data',
  `version` int(10) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`file_name`,`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=243 DEFAULT CHARSET=utf8 COMMENT='waf_data';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waf_data`
--

LOCK TABLES `waf_data` WRITE;
/*!40000 ALTER TABLE `waf_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `waf_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waf_install_active`
--

DROP TABLE IF EXISTS `waf_install_active`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `waf_install_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `server_ip` varchar(50) NOT NULL DEFAULT 'unknown' COMMENT 'ip',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'ver',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `server_ip` (`server_ip`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8 COMMENT='waf_install_active';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waf_install_active`
--

LOCK TABLES `waf_install_active` WRITE;
/*!40000 ALTER TABLE `waf_install_active` DISABLE KEYS */;
/*!40000 ALTER TABLE `waf_install_active` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waf_install_file`
--

DROP TABLE IF EXISTS `waf_install_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `waf_install_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `file` mediumblob NOT NULL COMMENT 'file',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `version` (`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COMMENT='waf_install_file';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waf_install_file`
--

LOCK TABLES `waf_install_file` WRITE;
/*!40000 ALTER TABLE `waf_install_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `waf_install_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waf_rule_active`
--

DROP TABLE IF EXISTS `waf_rule_active`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `waf_rule_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `server_ip` varchar(128) NOT NULL DEFAULT 'UNKNOW' COMMENT 'server_ip',
  `file_name` varchar(128) NOT NULL DEFAULT 'UNKNOW' COMMENT 'file name',
  `version` int(10) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `server_ip_file_name` (`server_ip`,`file_name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8 COMMENT='waf_rule_active';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waf_rule_active`
--

LOCK TABLES `waf_rule_active` WRITE;
/*!40000 ALTER TABLE `waf_rule_active` DISABLE KEYS */;
/*!40000 ALTER TABLE `waf_rule_active` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-10-25 13:41:56
