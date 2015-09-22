-- --------------------------------------------------------
-- Host:                         pub.mysql.db.dev.sh.ctripcorp.com
-- Server version:               5.6.12-log - MySQL Community Server (GPL)
-- Server OS:                    Linux
-- HeidiSQL Version:             8.3.0.4694
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for zeus_test
DROP DATABASE IF EXISTS `zeus_test`;
CREATE DATABASE IF NOT EXISTS `zeus_test` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `zeus_test`;


-- Dumping structure for table archive_group
DROP TABLE IF EXISTS `archive_group`;
CREATE TABLE IF NOT EXISTS `archive_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_version` (`group_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table archive_slb
DROP TABLE IF EXISTS `archive_slb`;
CREATE TABLE IF NOT EXISTS `archive_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table auth_private_key
DROP TABLE IF EXISTS `auth_private_key`;
CREATE TABLE IF NOT EXISTS `auth_private_key` (
  `private_key` varchar(50) NOT NULL DEFAULT '' COMMENT 'private key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last change time',
  PRIMARY KEY (`private_key`),
  KEY `time idx` (`DataChange_LastTime`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store the private key';

-- Data exporting was unselected.


-- Dumping structure for table auth_resource
DROP TABLE IF EXISTS `auth_resource`;
CREATE TABLE IF NOT EXISTS `auth_resource` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `resource_name` varchar(100) NOT NULL DEFAULT '0' COMMENT 'resource name',
  `resource_type` varchar(50) DEFAULT NULL COMMENT 'resource type',
  `description` varchar(100) DEFAULT NULL COMMENT 'description',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time ',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `time_idx` (`DataChange_LastTime`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='resource table';

-- Data exporting was unselected.


-- Dumping structure for table auth_resource_role
DROP TABLE IF EXISTS `auth_resource_role`;
CREATE TABLE IF NOT EXISTS `auth_resource_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'resource name',
  `role_name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'role name',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `role_idx` (`role_name`),
  KEY `res_idx` (`resource_name`),
  KEY `time_idx` (`DataChange_LastTime`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='resource role table';

-- Data exporting was unselected.


-- Dumping structure for table auth_role
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE IF NOT EXISTS `auth_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `role_name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'role name',
  `description` varchar(100) DEFAULT '0' COMMENT 'description',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_idx` (`role_name`),
  KEY `time_idx` (`DataChange_LastTime`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth role table';

-- Data exporting was unselected.


-- Dumping structure for table auth_user
DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE IF NOT EXISTS `auth_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_name` varchar(50) DEFAULT NULL COMMENT 'user name',
  `description` varchar(100) DEFAULT NULL COMMENT 'description',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `usr_name_idx` (`user_name`),
  KEY `time_idx` (`DataChange_LastTime`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth user table';

-- Data exporting was unselected.


-- Dumping structure for table auth_user_role
DROP TABLE IF EXISTS `auth_user_role`;
CREATE TABLE IF NOT EXISTS `auth_user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_name` varchar(50) DEFAULT NULL COMMENT 'user name',
  `role_name` varchar(50) DEFAULT NULL COMMENT 'role name',
  `group` varchar(50) DEFAULT NULL COMMENT 'group name',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `usr_idx` (`user_name`),
  KEY `time_idx` (`DataChange_LastTime`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth user role';

-- Data exporting was unselected.


-- Dumping structure for table build_info
DROP TABLE IF EXISTS `build_info`;
CREATE TABLE IF NOT EXISTS `build_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `pending_ticket` int(11) DEFAULT NULL COMMENT 'null',
  `current_ticket` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table conf_group_active
DROP TABLE IF EXISTS `conf_group_active`;
CREATE TABLE IF NOT EXISTS `conf_group_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_slb_id` (`group_id`,`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table conf_group_slb_active
DROP TABLE IF EXISTS `conf_group_slb_active`;
CREATE TABLE IF NOT EXISTS `conf_group_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `priority` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table conf_slb_active
DROP TABLE IF EXISTS `conf_slb_active`;
CREATE TABLE IF NOT EXISTS `conf_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table dist_lock
DROP TABLE IF EXISTS `dist_lock`;
CREATE TABLE IF NOT EXISTS `dist_lock` (
  `lock_key` varchar(255) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'lock key',
  `owner` bigint(20) DEFAULT '0' COMMENT 'thread id',
  `server` varchar(50) DEFAULT '0' COMMENT 'server ip',
  `created_time` bigint(20) DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`lock_key`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='distribution lock';

-- Data exporting was unselected.


-- Dumping structure for table group
DROP TABLE IF EXISTS `group`;
CREATE TABLE IF NOT EXISTS `group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `name` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `app_id` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `ssl` bit(1) NOT NULL DEFAULT b'0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table group_health_check
DROP TABLE IF EXISTS `group_health_check`;
CREATE TABLE IF NOT EXISTS `group_health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `uri` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `intervals` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `fails` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `passes` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table group_load_balancing_method
DROP TABLE IF EXISTS `group_load_balancing_method`;
CREATE TABLE IF NOT EXISTS `group_load_balancing_method` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `type` varchar(100) NOT NULL DEFAULT '0' COMMENT 'null',
  `value` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table group_server
DROP TABLE IF EXISTS `group_server`;
CREATE TABLE IF NOT EXISTS `group_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `host_name` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `port` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `weight` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `max_fails` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `fail_timeout` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_ip` (`group_id`,`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table group_slb
DROP TABLE IF EXISTS `group_slb`;
CREATE TABLE IF NOT EXISTS `group_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_virtual_server_id` bigint(20) DEFAULT '0' COMMENT 'null',
  `path` varchar(4096) NOT NULL DEFAULT '0' COMMENT 'null',
  `rewrite` mediumtext COMMENT 'null',
  `priority` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table m_vs_content
DROP TABLE IF EXISTS `m_vs_content`;
CREATE TABLE IF NOT EXISTS `m_vs_content` (
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vs_archive_id',
  `content` mediumtext NOT NULL COMMENT 'vs_archive_content',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`vs_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='metadata table of virtual server content';

-- Data exporting was unselected.


-- Dumping structure for table nginx_conf
DROP TABLE IF EXISTS `nginx_conf`;
CREATE TABLE IF NOT EXISTS `nginx_conf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table nginx_conf_server
DROP TABLE IF EXISTS `nginx_conf_server`;
CREATE TABLE IF NOT EXISTS `nginx_conf_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_version` (`slb_virtual_server_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table nginx_conf_upstream
DROP TABLE IF EXISTS `nginx_conf_upstream`;
CREATE TABLE IF NOT EXISTS `nginx_conf_upstream` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_version` (`slb_virtual_server_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table nginx_server
DROP TABLE IF EXISTS `nginx_server`;
CREATE TABLE IF NOT EXISTS `nginx_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `ip` varchar(200) DEFAULT NULL COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table operation_log
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` varchar(128) NOT NULL DEFAULT 'Unknown' COMMENT 'type',
  `target_id` varchar(128) NOT NULL DEFAULT '0' COMMENT 'target id',
  `operation` varchar(128) NOT NULL DEFAULT 'Unknown' COMMENT 'operation',
  `data` varchar(10240) DEFAULT NULL COMMENT 'data',
  `user_name` varchar(128) DEFAULT NULL COMMENT 'user name',
  `client_ip` varchar(128) DEFAULT NULL COMMENT 'client ip',
  `success` bit(1) NOT NULL DEFAULT b'0' COMMENT 'success',
  `err_msg` varchar(2048) DEFAULT NULL COMMENT 'err msg',
  `datetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datetime',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='operation log';

-- Data exporting was unselected.


-- Dumping structure for table property
DROP TABLE IF EXISTS `property`;
CREATE TABLE IF NOT EXISTS `property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `property_key_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'property name ref id',
  `property_value` varchar(255) NOT NULL DEFAULT '0' COMMENT 'property value',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `property_name_property_value` (`property_key_id`,`property_value`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='property table';

-- Data exporting was unselected.


-- Dumping structure for table property_item
DROP TABLE IF EXISTS `property_item`;
CREATE TABLE IF NOT EXISTS `property_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `property_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'property ref id',
  `item_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'item id',
  `type` varchar(255) NOT NULL DEFAULT '0' COMMENT 'item type',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `property_id_item_id_type` (`property_id`,`item_id`,`type`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='property item';

-- Data exporting was unselected.


-- Dumping structure for table property_key
DROP TABLE IF EXISTS `property_key`;
CREATE TABLE IF NOT EXISTS `property_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` varchar(255) NOT NULL DEFAULT '0' COMMENT 'name',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='property key';

-- Data exporting was unselected.


-- Dumping structure for table report
DROP TABLE IF EXISTS `report`;
CREATE TABLE IF NOT EXISTS `report` (
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group primary key',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT 'status',
  `description` varchar(255) NOT NULL DEFAULT '0' COMMENT 'status description',
  `reported_version` int(11) NOT NULL DEFAULT '0' COMMENT 'the version reported',
  `current_version` int(11) NOT NULL DEFAULT '0' COMMENT 'the version to report',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'data changed timestamp',
  PRIMARY KEY (`group_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store report data';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_domain
DROP TABLE IF EXISTS `r_vs_domain`;
CREATE TABLE IF NOT EXISTS `r_vs_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `domain` varchar(200) NOT NULL DEFAULT 'Undefined' COMMENT 'slb_domain_name',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vs_id_domain` (`vs_id`,`domain`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of vs and domain';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_slb
DROP TABLE IF EXISTS `r_vs_slb`;
CREATE TABLE IF NOT EXISTS `r_vs_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_vs_id` (`slb_id`,`vs_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of slb and vs';

-- Data exporting was unselected.


-- Dumping structure for table server
DROP TABLE IF EXISTS `server`;
CREATE TABLE IF NOT EXISTS `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `ip` varchar(100) NOT NULL DEFAULT '0' COMMENT 'null',
  `host_name` varchar(100) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table slb
DROP TABLE IF EXISTS `slb`;
CREATE TABLE IF NOT EXISTS `slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `name` varchar(100) NOT NULL DEFAULT '0' COMMENT 'null',
  `nginx_bin` varchar(300) NOT NULL DEFAULT '0' COMMENT 'null',
  `nginx_conf` varchar(300) NOT NULL DEFAULT '0' COMMENT 'null',
  `nginx_worker_processes` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `status` varchar(300) NOT NULL DEFAULT '0' COMMENT 'null',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table slb_domain
DROP TABLE IF EXISTS `slb_domain`;
CREATE TABLE IF NOT EXISTS `slb_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `name` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_name` (`slb_virtual_server_id`,`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table slb_server
DROP TABLE IF EXISTS `slb_server`;
CREATE TABLE IF NOT EXISTS `slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `ip` varchar(50) NOT NULL DEFAULT '0' COMMENT 'null',
  `host_name` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `enable` bit(1) NOT NULL DEFAULT b'0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table slb_vip
DROP TABLE IF EXISTS `slb_vip`;
CREATE TABLE IF NOT EXISTS `slb_vip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `ip` varchar(50) NOT NULL DEFAULT '0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table slb_virtual_server
DROP TABLE IF EXISTS `slb_virtual_server`;
CREATE TABLE IF NOT EXISTS `slb_virtual_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `name` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `port` varchar(200) NOT NULL DEFAULT '0' COMMENT 'null',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_name` (`slb_id`,`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table snap_server_group
DROP TABLE IF EXISTS `snap_server_group`;
CREATE TABLE IF NOT EXISTS `snap_server_group` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ip` varchar(50) NOT NULL DEFAULT '0' COMMENT 'ip',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='snap_server_group';

-- Data exporting was unselected.


-- Dumping structure for table status_group_server
DROP TABLE IF EXISTS `status_group_server`;
CREATE TABLE IF NOT EXISTS `status_group_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `ip` varchar(200) NOT NULL DEFAULT '0.0.0.0' COMMENT 'null',
  `up` bit(1) NOT NULL DEFAULT b'0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_group_id_ip` (`slb_virtual_server_id`,`group_id`,`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table status_server
DROP TABLE IF EXISTS `status_server`;
CREATE TABLE IF NOT EXISTS `status_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `ip` varchar(200) NOT NULL DEFAULT '0.0.0.0' COMMENT 'null',
  `up` bit(1) NOT NULL DEFAULT b'0' COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null';

-- Data exporting was unselected.


-- Dumping structure for table tag
DROP TABLE IF EXISTS `tag`;
CREATE TABLE IF NOT EXISTS `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary',
  `name` varchar(255) NOT NULL DEFAULT '0' COMMENT 'tag name',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='tag def table';

-- Data exporting was unselected.


-- Dumping structure for table tag_item
DROP TABLE IF EXISTS `tag_item`;
CREATE TABLE IF NOT EXISTS `tag_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary',
  `tag_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'tag def id',
  `item_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'item ref id',
  `type` varchar(255) NOT NULL DEFAULT 'Undefined' COMMENT 'item type',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tag_id_item_id_type` (`tag_id`,`item_id`,`type`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='tag item mapping';

-- Data exporting was unselected.


-- Dumping structure for table task
DROP TABLE IF EXISTS `task`;
CREATE TABLE IF NOT EXISTS `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ops_type` varchar(50) NOT NULL DEFAULT 'UNDEFINE' COMMENT 'ops type',
  `group_id` bigint(20) DEFAULT NULL COMMENT 'group id',
  `slb_id` bigint(20) DEFAULT NULL COMMENT 'slb id',
  `ip_list` varchar(4096) DEFAULT NULL COMMENT 'ip list',
  `up` bit(1) DEFAULT NULL COMMENT 'up',
  `status` varchar(50) NOT NULL DEFAULT 'UNDEFINE' COMMENT 'status',
  `target_slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'target slb id',
  `version` int(11) DEFAULT '0' COMMENT 'version',
  `fail_cause` varchar(1024) DEFAULT NULL COMMENT 'fail cause',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='task queue';

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
