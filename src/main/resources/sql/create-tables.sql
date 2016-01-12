-- --------------------------------------------------------
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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_version` (`group_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table archive_slb
DROP TABLE IF EXISTS `archive_slb`;
CREATE TABLE IF NOT EXISTS `archive_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table archive_vs
DROP TABLE IF EXISTS `archive_vs`;
CREATE TABLE IF NOT EXISTS `archive_vs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'virtual server id',
  `content` mediumtext NOT NULL COMMENT 'content',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DateTime_LastChange` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vs_id_version` (`vs_id`,`version`),
  KEY `DateTime_LastChange` (`DateTime_LastChange`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table of virtual server archive';

-- Data exporting was unselected.


-- Dumping structure for table auth_private_key
DROP TABLE IF EXISTS `auth_private_key`;
CREATE TABLE IF NOT EXISTS `auth_private_key` (
  `private_key` varchar(50) NOT NULL DEFAULT '' COMMENT 'private key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last change time',
  PRIMARY KEY (`private_key`),
  KEY `time idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store the private key';

-- Data exporting was unselected.


-- Dumping structure for table auth_resource
DROP TABLE IF EXISTS `auth_resource`;
CREATE TABLE IF NOT EXISTS `auth_resource` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_name` varchar(100) CHARACTER SET latin1 NOT NULL DEFAULT '0' COMMENT 'resource name',
  `resource_type` varchar(50) CHARACTER SET latin1 DEFAULT NULL COMMENT 'resource type',
  `description` varchar(100) CHARACTER SET latin1 DEFAULT NULL COMMENT 'description',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time ',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='resource table';

-- Data exporting was unselected.


-- Dumping structure for table auth_resource_role
DROP TABLE IF EXISTS `auth_resource_role`;
CREATE TABLE IF NOT EXISTS `auth_resource_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_name` varchar(50) CHARACTER SET latin1 NOT NULL DEFAULT '0' COMMENT 'resource name',
  `role_name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'role name',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `role_idx` (`role_name`),
  KEY `res_idx` (`resource_name`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='resource role table';

-- Data exporting was unselected.


-- Dumping structure for table auth_role
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE IF NOT EXISTS `auth_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `role_name` varchar(50) CHARACTER SET latin1 NOT NULL DEFAULT '0' COMMENT 'role name',
  `description` varchar(100) DEFAULT '0' COMMENT 'description',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_idx` (`role_name`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth role table';

-- Data exporting was unselected.


-- Dumping structure for table auth_user
DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE IF NOT EXISTS `auth_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_name` varchar(50) DEFAULT NULL COMMENT 'user name',
  `description` varchar(100) DEFAULT NULL COMMENT 'description',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `usr_name_idx` (`user_name`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth user table';

-- Data exporting was unselected.


-- Dumping structure for table auth_user_role
DROP TABLE IF EXISTS `auth_user_role`;
CREATE TABLE IF NOT EXISTS `auth_user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_name` varchar(50) DEFAULT NULL COMMENT 'user name',
  `role_name` varchar(50) DEFAULT NULL COMMENT 'role name',
  `group` varchar(50) DEFAULT NULL COMMENT 'group name',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `usr_idx` (`user_name`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth user role';

-- Data exporting was unselected.


-- Dumping structure for table build_info
DROP TABLE IF EXISTS `build_info`;
CREATE TABLE IF NOT EXISTS `build_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `pending_ticket` int(11) DEFAULT NULL,
  `current_ticket` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table certificate
DROP TABLE IF EXISTS `certificate`;
CREATE TABLE IF NOT EXISTS `certificate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `domain` varchar(1024) NOT NULL DEFAULT 'localhost' COMMENT 'certificate domain',
  `cert` mediumblob NOT NULL COMMENT 'certificate file',
  `key` mediumblob NOT NULL COMMENT 'key file',
  `state` bit(1) NOT NULL DEFAULT b'1' COMMENT 'state',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='meta data table of certificate';

-- Data exporting was unselected.


-- Dumping structure for table conf_group_active
DROP TABLE IF EXISTS `conf_group_active`;
CREATE TABLE IF NOT EXISTS `conf_group_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table conf_group_slb_active
DROP TABLE IF EXISTS `conf_group_slb_active`;
CREATE TABLE IF NOT EXISTS `conf_group_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `priority` int(11) NOT NULL DEFAULT '0',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_slb_id` (`slb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table conf_slb_active
DROP TABLE IF EXISTS `conf_slb_active`;
CREATE TABLE IF NOT EXISTS `conf_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table conf_slb_virtual_server_active
DROP TABLE IF EXISTS `conf_slb_virtual_server_active`;
CREATE TABLE IF NOT EXISTS `conf_slb_virtual_server_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_slb_id` (`slb_virtual_server_id`,`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='conf_slb_virtual_server_active';

-- Data exporting was unselected.


-- Dumping structure for table fxslbdb.conf_slb_virtual_server_active
DROP TABLE IF EXISTS `conf_slb_virtual_server_active`;
CREATE TABLE IF NOT EXISTS `conf_slb_virtual_server_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `content` mediumtext COMMENT 'null',
  `version` int(11) DEFAULT NULL COMMENT 'null',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_slb_id` (`slb_virtual_server_id`,`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='conf_slb_virtual_server_active';

-- Data exporting was unselected.


-- Dumping structure for table dist_lock
DROP TABLE IF EXISTS `dist_lock`;
CREATE TABLE IF NOT EXISTS `dist_lock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `lock_key` varchar(255) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'lock key',
  `owner` bigint(20) DEFAULT '0' COMMENT 'thread id',
  `server` varchar(50) DEFAULT '0' COMMENT 'server ip',
  `created_time` bigint(20) DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `lock_key` (`lock_key`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='distribution lock';

-- Data exporting was unselected.


-- Dumping structure for table global_job
DROP TABLE IF EXISTS `global_job`;
CREATE TABLE IF NOT EXISTS `global_job` (
  `job_key` varchar(254) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'job key',
  `owner` varchar(256) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'owner',
  `status` varchar(256) DEFAULT NULL COMMENT 'status',
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'start time',
  `finish_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'finish_time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`job_key`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='global job';

-- Data exporting was unselected.


-- Dumping structure for table group
DROP TABLE IF EXISTS `group`;
CREATE TABLE IF NOT EXISTS `group` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table group_health_check
DROP TABLE IF EXISTS `group_health_check`;
CREATE TABLE IF NOT EXISTS `group_health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `uri` varchar(200) NOT NULL DEFAULT '0',
  `intervals` int(11) NOT NULL DEFAULT '0',
  `fails` int(11) NOT NULL DEFAULT '0',
  `passes` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table group_load_balancing_method
DROP TABLE IF EXISTS `group_load_balancing_method`;
CREATE TABLE IF NOT EXISTS `group_load_balancing_method` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `type` varchar(100) NOT NULL DEFAULT '0',
  `value` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table group_server
DROP TABLE IF EXISTS `group_server`;
CREATE TABLE IF NOT EXISTS `group_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(200) NOT NULL DEFAULT '0',
  `host_name` varchar(200) NOT NULL DEFAULT '0',
  `port` int(11) NOT NULL DEFAULT '0',
  `weight` int(11) NOT NULL DEFAULT '0',
  `max_fails` int(11) NOT NULL DEFAULT '0',
  `fail_timeout` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_ip` (`group_id`,`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table group_slb
DROP TABLE IF EXISTS `group_slb`;
CREATE TABLE IF NOT EXISTS `group_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) DEFAULT '0',
  `path` varchar(4096) NOT NULL DEFAULT '0' COMMENT 'null',
  `rewrite` mediumtext COMMENT 'null',
  `priority` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table nginx_conf
DROP TABLE IF EXISTS `nginx_conf`;
CREATE TABLE IF NOT EXISTS `nginx_conf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table nginx_conf_server
DROP TABLE IF EXISTS `nginx_conf_server`;
CREATE TABLE IF NOT EXISTS `nginx_conf_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` mediumtext,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_version` (`slb_virtual_server_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_slb_id_virtual_server_id_version` (`slb_id`,`slb_virtual_server_id`,`version`),
  KEY `idx_slb_id_version` (`slb_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table nginx_conf_upstream
DROP TABLE IF EXISTS `nginx_conf_upstream`;
CREATE TABLE IF NOT EXISTS `nginx_conf_upstream` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_version` (`slb_virtual_server_id`,`version`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_slb_id_virtual_server_id_version` (`slb_id`,`slb_virtual_server_id`,`version`),
  KEY `idx_slb_id_version` (`slb_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table nginx_server
DROP TABLE IF EXISTS `nginx_server`;
CREATE TABLE IF NOT EXISTS `nginx_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(200) DEFAULT NULL,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `err_msg` varchar(6144) DEFAULT NULL COMMENT 'err msg',
  `datetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'datetime',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_datetime` (`datetime`)
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
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_status` (`status`),
  KEY `idx_reported_version` (`reported_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store report data';

-- Data exporting was unselected.


-- Dumping structure for table r_certificate_slb_server
DROP TABLE IF EXISTS `r_certificate_slb_server`;
CREATE TABLE IF NOT EXISTS `r_certificate_slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `cert_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'certificate id',
  `command` bigint(20) NOT NULL DEFAULT '0' COMMENT 'commanded cert id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'virtual server id',
  `ip` varchar(100) NOT NULL DEFAULT '0' COMMENT 'slb server ip',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip_vs_id` (`ip`,`vs_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of certificate and slb server';

-- Data exporting was unselected.


-- Dumping structure for table r_group_gs
DROP TABLE IF EXISTS `r_group_gs`;
CREATE TABLE IF NOT EXISTS `r_group_gs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group_id',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'group_server ip',
  `group_version` int(11) NOT NULL DEFAULT '0' COMMENT 'group version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of group and group server ip';

-- Data exporting was unselected.


-- Dumping structure for table r_group_status
DROP TABLE IF EXISTS `r_group_status`;
CREATE TABLE IF NOT EXISTS `r_group_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `offline_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `online_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `offline_version` (`offline_version`),
  KEY `online_version` (`online_version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='group status table';

-- Data exporting was unselected.


-- Dumping structure for table r_group_vg
DROP TABLE IF EXISTS `r_group_vg`;
CREATE TABLE IF NOT EXISTS `r_group_vg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of group and virtual group';

-- Data exporting was unselected.


-- Dumping structure for table r_group_vs
DROP TABLE IF EXISTS `r_group_vs`;
CREATE TABLE IF NOT EXISTS `r_group_vs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group_id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vs_id',
  `path` varchar(4096) DEFAULT NULL COMMENT 'path',
  `group_version` int(11) NOT NULL DEFAULT '0' COMMENT 'group version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_vs_id` (`vs_id`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of group and vs';

-- Data exporting was unselected.


-- Dumping structure for table r_slb_slb_server
DROP TABLE IF EXISTS `r_slb_slb_server`;
CREATE TABLE IF NOT EXISTS `r_slb_slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'slb_server ip',
  `slb_version` int(11) NOT NULL DEFAULT '0' COMMENT 'slb version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of slb and slb server';

-- Data exporting was unselected.


-- Dumping structure for table r_slb_status
DROP TABLE IF EXISTS `r_slb_status`;
CREATE TABLE IF NOT EXISTS `r_slb_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `online_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online version',
  `offline_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `slb_id` (`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `online_version` (`online_version`),
  KEY `offline_version` (`offline_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='slb status table';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_domain
DROP TABLE IF EXISTS `r_vs_domain`;
CREATE TABLE IF NOT EXISTS `r_vs_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `domain` varchar(200) NOT NULL DEFAULT 'Undefined' COMMENT 'slb_domain_name',
  `vs_version` int(11) NOT NULL DEFAULT '0' COMMENT 'vs version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `vs_id_domain` (`vs_id`,`domain`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of vs and domain';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_slb
DROP TABLE IF EXISTS `r_vs_slb`;
CREATE TABLE IF NOT EXISTS `r_vs_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `vs_version` int(11) NOT NULL DEFAULT '0' COMMENT 'vs version',
  `hash` int(11) DEFAULT NULL COMMENT 'hash search key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_vs_id` (`vs_id`),
  KEY `slb_id` (`slb_id`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of slb and vs';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_status
DROP TABLE IF EXISTS `r_vs_status`;
CREATE TABLE IF NOT EXISTS `r_vs_status` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='vs status table';

-- Data exporting was unselected.


-- Dumping structure for table server
DROP TABLE IF EXISTS `server`;
CREATE TABLE IF NOT EXISTS `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(100) NOT NULL DEFAULT '0',
  `host_name` varchar(100) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table slb
DROP TABLE IF EXISTS `slb`;
CREATE TABLE IF NOT EXISTS `slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '0',
  `nginx_bin` varchar(300) NOT NULL DEFAULT '0',
  `nginx_conf` varchar(300) NOT NULL DEFAULT '0',
  `nginx_worker_processes` int(11) NOT NULL DEFAULT '0',
  `status` varchar(300) NOT NULL DEFAULT '0',
  `version` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table slb_domain
DROP TABLE IF EXISTS `slb_domain`;
CREATE TABLE IF NOT EXISTS `slb_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_name` (`slb_virtual_server_id`,`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table slb_server
DROP TABLE IF EXISTS `slb_server`;
CREATE TABLE IF NOT EXISTS `slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `host_name` varchar(200) NOT NULL DEFAULT '0',
  `enable` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table slb_vip
DROP TABLE IF EXISTS `slb_vip`;
CREATE TABLE IF NOT EXISTS `slb_vip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table slb_virtual_server
DROP TABLE IF EXISTS `slb_virtual_server`;
CREATE TABLE IF NOT EXISTS `slb_virtual_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `port` varchar(200) NOT NULL DEFAULT '0',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_name` (`slb_id`,`name`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table snap_server_group
DROP TABLE IF EXISTS `snap_server_group`;
CREATE TABLE IF NOT EXISTS `snap_server_group` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ip` varchar(50) NOT NULL DEFAULT '0' COMMENT 'ip',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='snap_server_group';

-- Data exporting was unselected.


-- Dumping structure for table status_group_server
DROP TABLE IF EXISTS `status_group_server`;
CREATE TABLE IF NOT EXISTS `status_group_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(200) NOT NULL,
  `up` bit(1) NOT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` int(20) NOT NULL DEFAULT '0' COMMENT 'status',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_slb_virtual_server_id_group_id_ip` (`group_id`,`ip`,`slb_virtual_server_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_slb_id` (`slb_id`),
  KEY `idx_create_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table status_health_check
DROP TABLE IF EXISTS `status_health_check`;
CREATE TABLE IF NOT EXISTS `status_health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'null',
  `slb_server_ip` varchar(64) NOT NULL DEFAULT '0.0.0.0' COMMENT 'null',
  `upstream_name` varchar(128) NOT NULL DEFAULT 'backend_0' COMMENT 'null',
  `member_ip_port` varchar(128) NOT NULL DEFAULT 'backend_0' COMMENT 'null',
  `status` varchar(128) DEFAULT NULL COMMENT 'null',
  `rise` int(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `fall` int(20) NOT NULL DEFAULT '0' COMMENT 'null',
  `type` varchar(128) DEFAULT NULL COMMENT 'null',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_slb_server_ip_member_ip_port_upstream_name` (`slb_server_ip`,`member_ip_port`,`upstream_name`),
  KEY `slb_server_ip` (`slb_server_ip`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='status_health_check';

-- Data exporting was unselected.


-- Dumping structure for table status_server
DROP TABLE IF EXISTS `status_server`;
CREATE TABLE IF NOT EXISTS `status_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(200) NOT NULL,
  `up` bit(1) NOT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `slb_virtual_server_id` bigint(20) DEFAULT NULL COMMENT 'vs id',
  `ip_list` varchar(4096) DEFAULT NULL COMMENT 'ip list',
  `up` bit(1) DEFAULT NULL COMMENT 'up',
  `status` varchar(50) NOT NULL DEFAULT 'UNDEFINE' COMMENT 'status',
  `target_slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'target slb id',
  `resources` varchar(128) DEFAULT NULL COMMENT 'resources',
  `version` int(11) DEFAULT '0' COMMENT 'version',
  `fail_cause` varchar(1024) DEFAULT NULL COMMENT 'fail cause',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='task queue';

-- Data exporting was unselected.


-- Dumping structure for table waf_data
DROP TABLE IF EXISTS `waf_data`;
CREATE TABLE IF NOT EXISTS `waf_data` (
  `id` bigint(10) NOT NULL COMMENT 'id',
  `file_name` varchar(128) NOT NULL DEFAULT 'UNKNOW' COMMENT 'name',
  `data` mediumtext COMMENT 'data',
  `version` int(10) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`file_name`,`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='waf_data';

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
