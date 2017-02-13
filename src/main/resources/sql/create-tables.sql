-- --------------------------------------------------------
-- Host:                         pub.mysql.db.dev.sh.ctripcorp.com
-- Server version:               5.6.12-log - MySQL Community Server (GPL)
-- Server OS:                    Linux
-- HeidiSQL Version:             9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for zeus_test
DROP DATABASE IF EXISTS `zeus_test`;
CREATE DATABASE IF NOT EXISTS `zeus_test` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `zeus_test`;


-- Dumping structure for table app_info
DROP TABLE IF EXISTS `app_info`;
CREATE TABLE IF NOT EXISTS `app_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `app_id` varchar(50) NOT NULL DEFAULT '' COMMENT 'app_id',
  `app_name` varchar(128) NOT NULL DEFAULT '' COMMENT 'app_name',
  `sbu` varchar(128) NOT NULL DEFAULT '' COMMENT 'sbu',
  `sbu_english_name` varchar(256) NOT NULL DEFAULT '' COMMENT 'sbuEnglishName',
  `owner` varchar(256) NOT NULL DEFAULT '' COMMENT 'owner',
  `owner_email` varchar(512) NOT NULL DEFAULT '' COMMENT 'owner_email',
  `app_container` varchar(512) NOT NULL DEFAULT '' COMMENT 'appContainer',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id` (`app_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app_info';

-- Data exporting was unselected.


-- Dumping structure for table app_slb
DROP TABLE IF EXISTS `app_slb`;
CREATE TABLE IF NOT EXISTS `app_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slbid',
  `app_id` varchar(128) NOT NULL DEFAULT 'unknown' COMMENT 'appid',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `slb_id_app_id` (`slb_id`,`app_id`),
  KEY `app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app_slb';

-- Data exporting was unselected.


-- Dumping structure for table app_vs
DROP TABLE IF EXISTS `app_vs`;
CREATE TABLE IF NOT EXISTS `app_vs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vid',
  `app_id` varchar(128) NOT NULL DEFAULT 'unknown' COMMENT 'appid',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `vs_id_app_id` (`vs_id`,`app_id`),
  KEY `app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app_vs';

-- Data exporting was unselected.


-- Dumping structure for table archive_commit
DROP TABLE IF EXISTS `archive_commit`;
CREATE TABLE IF NOT EXISTS `archive_commit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `archive_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'archive id',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT 'archive type',
  `ref_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'ref meta primary key',
  `author` varchar(255) DEFAULT NULL COMMENT 'author',
  `message` varchar(255) DEFAULT NULL COMMENT 'commit message',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `archive_id_type` (`archive_id`,`type`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `ref_id` (`ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for archive commit message';

-- Data exporting was unselected.


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
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `domain` (`domain`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='meta data table of certificate';

-- Data exporting was unselected.


-- Dumping structure for table commit
DROP TABLE IF EXISTS `commit`;
CREATE TABLE IF NOT EXISTS `commit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary',
  `version` bigint(20) DEFAULT NULL COMMENT 'version',
  `slb_id` bigint(20) DEFAULT NULL COMMENT 'slb id',
  `vs_ids` varchar(8124) CHARACTER SET latin1 DEFAULT NULL COMMENT 'vs ids',
  `group_ids` varchar(10240) CHARACTER SET latin1 DEFAULT NULL COMMENT 'group ids',
  `task_ids` varchar(4096) CHARACTER SET latin1 DEFAULT NULL COMMENT 'task ids',
  `cleanvs_ids` varchar(4096) CHARACTER SET latin1 DEFAULT NULL COMMENT 'cleanvs ids',
  `type` varchar(45) CHARACTER SET latin1 DEFAULT NULL COMMENT 'type',
  `DataChange_LastTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'date time last changed',
  PRIMARY KEY (`id`),
  KEY `version_slb_id` (`version`,`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='commit table';

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


-- Dumping structure for table conf_slb_version
DROP TABLE IF EXISTS `conf_slb_version`;
CREATE TABLE IF NOT EXISTS `conf_slb_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `previous_version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb previous version',
  `current_version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb current version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='slb conf version table';

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
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `server` (`server`)
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


-- Dumping structure for table group_history
DROP TABLE IF EXISTS `group_history`;
CREATE TABLE IF NOT EXISTS `group_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group id',
  `group_name` varchar(255) NOT NULL DEFAULT 'undefined' COMMENT 'group name',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`),
  KEY `group_name` (`group_name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='deleted group record';

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


-- Dumping structure for table group_server_status
DROP TABLE IF EXISTS `group_server_status`;
CREATE TABLE IF NOT EXISTS `group_server_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'gid',
  `ip` varchar(200) NOT NULL DEFAULT 'UNKNOW' COMMENT 'ip',
  `status` int(20) NOT NULL DEFAULT '0' COMMENT 'status',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'ct',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'dt',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_id_ip` (`group_id`,`ip`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_create_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='group_server_status';

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


-- Dumping structure for table message_queue
DROP TABLE IF EXISTS `message_queue`;
CREATE TABLE IF NOT EXISTS `message_queue` (
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
  KEY `status_create_time` (`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='message_queue';

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


-- Dumping structure for table nginx_conf_slb
DROP TABLE IF EXISTS `nginx_conf_slb`;
CREATE TABLE IF NOT EXISTS `nginx_conf_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb conf version',
  `content` mediumblob NOT NULL COMMENT 'conf content',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='conf file of each slb cluster';

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
  `version` bigint(20) DEFAULT NULL,
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
  KEY `idx_datetime` (`datetime`),
  KEY `type_target_id_operation_user_name_client_ip_datetime_success` (`type`,`target_id`,`operation`,`user_name`,`client_ip`,`datetime`,`success`),
  KEY `target_id` (`target_id`),
  KEY `operation` (`operation`),
  KEY `user_name` (`user_name`),
  KEY `client_ip` (`client_ip`),
  KEY `success` (`success`)
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
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `property_id` (`property_id`),
  KEY `item_id` (`item_id`),
  KEY `type` (`type`)
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
  `description` varchar(255) DEFAULT '0' COMMENT 'status description',
  `reported_version` int(11) NOT NULL DEFAULT '0' COMMENT 'the version reported',
  `current_version` int(11) NOT NULL DEFAULT '0' COMMENT 'the version to report',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'data changed timestamp',
  PRIMARY KEY (`group_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_status` (`status`),
  KEY `idx_reported_version` (`reported_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store report data';

-- Data exporting was unselected.


-- Dumping structure for table report_queue
DROP TABLE IF EXISTS `report_queue`;
CREATE TABLE IF NOT EXISTS `report_queue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `target_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'target id',
  `topic` int(11) NOT NULL DEFAULT '-1' COMMENT 'topic group',
  `state` bit(1) NOT NULL DEFAULT b'0' COMMENT 'result',
  `description` varchar(255) DEFAULT '0' COMMENT 'description',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_topic_target` (`topic`,`target_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='report queue for meta data insertion/update/deletion';

-- Data exporting was unselected.


-- Dumping structure for table role
DROP TABLE IF EXISTS `role`;
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) NOT NULL DEFAULT 'unknow' COMMENT 'name',
  `discription` varchar(256) DEFAULT NULL COMMENT 'discription',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='role';

-- Data exporting was unselected.


-- Dumping structure for table role_resource
DROP TABLE IF EXISTS `role_resource`;
CREATE TABLE IF NOT EXISTS `role_resource` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='role_resource';

-- Data exporting was unselected.


-- Dumping structure for table rule
DROP TABLE IF EXISTS `rule`;
CREATE TABLE IF NOT EXISTS `rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'rule name',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT 'dynamic/package',
  `target_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'rule target id',
  `target_type` int(11) NOT NULL DEFAULT '0' COMMENT 'rule target type',
  `value` varchar(255) DEFAULT NULL COMMENT 'rule value if exists',
  `phase` int(11) NOT NULL DEFAULT '0' COMMENT 'phase for rule to be injected',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'rule version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `target_id_target_type` (`target_id`,`target_type`),
  KEY `version` (`version`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='rule for config generation';

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
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `vs_id` (`vs_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of certificate and slb server';

-- Data exporting was unselected.


-- Dumping structure for table r_group_gs
DROP TABLE IF EXISTS `r_group_gs`;
CREATE TABLE IF NOT EXISTS `r_group_gs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'group_id',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'group_server ip',
  `group_version` int(11) NOT NULL DEFAULT '0' COMMENT 'group version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `ip` (`ip`),
  KEY `group_id` (`group_id`)
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
  `priority` int(11) NOT NULL DEFAULT '1000' COMMENT 'priority',
  `group_version` int(11) NOT NULL DEFAULT '0' COMMENT 'group version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_vs_id` (`vs_id`),
  KEY `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of group and vs';

-- Data exporting was unselected.


-- Dumping structure for table r_slb_slb_server
DROP TABLE IF EXISTS `r_slb_slb_server`;
CREATE TABLE IF NOT EXISTS `r_slb_slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `ip` varchar(200) NOT NULL DEFAULT '0' COMMENT 'slb_server ip',
  `slb_version` int(11) NOT NULL DEFAULT '0' COMMENT 'slb version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `slb_id` (`slb_id`),
  KEY `ip` (`ip`)
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
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `online_version` (`online_version`),
  KEY `offline_version` (`offline_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='slb status table';

-- Data exporting was unselected.


-- Dumping structure for table r_traffic_policy_group
DROP TABLE IF EXISTS `r_traffic_policy_group`;
CREATE TABLE IF NOT EXISTS `r_traffic_policy_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `policy_id` bigint(20) NOT NULL DEFAULT '0',
  `policy_version` int(11) NOT NULL DEFAULT '0',
  `weight` int(11) NOT NULL DEFAULT '0',
  `hash` int(11) NOT NULL DEFAULT '0',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `policy_id_policy_version` (`policy_id`,`policy_version`),
  KEY `group_id` (`group_id`),
  KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for traffic policy and group relationship maintenance';

-- Data exporting was unselected.


-- Dumping structure for table r_traffic_policy_vs
DROP TABLE IF EXISTS `r_traffic_policy_vs`;
CREATE TABLE IF NOT EXISTS `r_traffic_policy_vs` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for traffic policy and vs relationship maintenance';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_domain
DROP TABLE IF EXISTS `r_vs_domain`;
CREATE TABLE IF NOT EXISTS `r_vs_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `domain` varchar(200) NOT NULL DEFAULT 'Undefined' COMMENT 'slb_domain_name',
  `vs_version` int(11) NOT NULL DEFAULT '0' COMMENT 'vs version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `vs_id` (`vs_id`),
  KEY `domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='relation table of vs and domain';

-- Data exporting was unselected.


-- Dumping structure for table r_vs_slb
DROP TABLE IF EXISTS `r_vs_slb`;
CREATE TABLE IF NOT EXISTS `r_vs_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `vs_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_virtual_server_id',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb_id',
  `vs_version` int(11) NOT NULL DEFAULT '0' COMMENT 'vs version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last time modified',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `idx_vs_id` (`vs_id`),
  KEY `slb_id` (`slb_id`)
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
  `slb_id` bigint(20) DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `port` varchar(200) NOT NULL DEFAULT '0',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'version',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `is_ssl` (`is_ssl`)
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


-- Dumping structure for table stats_group_slb
DROP TABLE IF EXISTS `stats_group_slb`;
CREATE TABLE IF NOT EXISTS `stats_group_slb` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table to store group related statistics by slb';

-- Data exporting was unselected.


-- Dumping structure for table status_check_count_slb
DROP TABLE IF EXISTS `status_check_count_slb`;
CREATE TABLE IF NOT EXISTS `status_check_count_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `slb_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'slb id',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT 'count',
  `data_set` varchar(255) NOT NULL DEFAULT '0' COMMENT 'group data set',
  `data_set_timestamp` bigint(20) DEFAULT NULL COMMENT 'data set last modified time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id` (`slb_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for slb status check count';

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
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `type` (`type`)
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
  KEY `idx_create_time` (`create_time`),
  KEY `status_target_slb_id` (`status`,`target_slb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='task queue';

-- Data exporting was unselected.


-- Dumping structure for table task_execute_record
DROP TABLE IF EXISTS `task_execute_record`;
CREATE TABLE IF NOT EXISTS `task_execute_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `task_key` varchar(128) NOT NULL DEFAULT 'UNKNOW' COMMENT 'task key',
  `last_execute_time` bigint(20) NOT NULL DEFAULT '0' COMMENT 'last execute time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_key` (`task_key`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='task_execute_record';

-- Data exporting was unselected.


-- Dumping structure for table traffic_policy
DROP TABLE IF EXISTS `traffic_policy`;
CREATE TABLE IF NOT EXISTS `traffic_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` VARCHAR(255) NOT NULL DEFAULT 'null' COMMENT 'policy name',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT 'lastest version',
  `nx_active_version` int(11) NOT NULL DEFAULT '0' COMMENT 'offline version',
  `active_version` int(11) NOT NULL DEFAULT '0' COMMENT 'online verion',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last modified time',
  PRIMARY KEY (`id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `version` (`version`),
  KEY `nx_active_version` (`nx_active_version`),
  KEY `active_version` (`active_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for traffic policy and version status';

-- Data exporting was unselected.


-- Dumping structure for table user
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) NOT NULL DEFAULT 'unknow' COMMENT 'name',
  `email` varchar(128) DEFAULT 'unknow' COMMENT 'email',
  `bu` varchar(128) DEFAULT 'unknow' COMMENT 'bu',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user';

-- Data exporting was unselected.


-- Dumping structure for table user_resource
DROP TABLE IF EXISTS `user_resource`;
CREATE TABLE IF NOT EXISTS `user_resource` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user_resource';

-- Data exporting was unselected.


-- Dumping structure for table user_role
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'u',
  `role_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'r',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'DataChange_LastTime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_role_id` (`user_id`,`role_id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user_role';

-- Data exporting was unselected.


-- Dumping structure for table waf_data
DROP TABLE IF EXISTS `waf_data`;
CREATE TABLE IF NOT EXISTS `waf_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
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
