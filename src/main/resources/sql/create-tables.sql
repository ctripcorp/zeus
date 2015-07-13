-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        5.6.10 - MySQL Community Server (GPL)
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  9.1.0.4867
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
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_version` (`group_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table archive_group: ~0 rows (approximately)
/*!40000 ALTER TABLE `archive_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `archive_group` ENABLE KEYS */;


-- Dumping structure for table archive_slb
DROP TABLE IF EXISTS `archive_slb`;
CREATE TABLE IF NOT EXISTS `archive_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table archive_slb: ~0 rows (approximately)
/*!40000 ALTER TABLE `archive_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `archive_slb` ENABLE KEYS */;


-- Dumping structure for table auth_private_key
DROP TABLE IF EXISTS `auth_private_key`;
CREATE TABLE IF NOT EXISTS `auth_private_key` (
  `private_key` varchar(50) NOT NULL DEFAULT '' COMMENT 'private key',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last change time',
  PRIMARY KEY (`private_key`),
  KEY `time idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='store the private key';

-- Dumping data for table auth_private_key: ~1 rows (approximately)
/*!40000 ALTER TABLE `auth_private_key` DISABLE KEYS */;
INSERT INTO `auth_private_key` (`private_key`, `DataChange_LastTime`) VALUES
	('testSlbServer', '2015-05-15 14:18:30');
/*!40000 ALTER TABLE `auth_private_key` ENABLE KEYS */;


-- Dumping structure for table auth_resource
DROP TABLE IF EXISTS `auth_resource`;
CREATE TABLE IF NOT EXISTS `auth_resource` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_name` varchar(100) CHARACTER SET latin1 NOT NULL DEFAULT '0' COMMENT 'resource name',
  `resource_type` varchar(50) CHARACTER SET latin1 DEFAULT NULL COMMENT 'resource type',
  `description` varchar(100) CHARACTER SET latin1 DEFAULT NULL COMMENT 'description',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time ',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='resource table';

-- Dumping data for table auth_resource: ~0 rows (approximately)
/*!40000 ALTER TABLE `auth_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_resource` ENABLE KEYS */;


-- Dumping structure for table auth_resource_role
DROP TABLE IF EXISTS `auth_resource_role`;
CREATE TABLE IF NOT EXISTS `auth_resource_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `resource_name` varchar(50) CHARACTER SET latin1 NOT NULL DEFAULT '0' COMMENT 'resource name',
  `role_name` varchar(50) NOT NULL DEFAULT '0' COMMENT 'role name',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `role_idx` (`role_name`),
  KEY `res_idx` (`resource_name`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='resource role table';

-- Dumping data for table auth_resource_role: ~0 rows (approximately)
/*!40000 ALTER TABLE `auth_resource_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_resource_role` ENABLE KEYS */;


-- Dumping structure for table auth_role
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE IF NOT EXISTS `auth_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `role_name` varchar(50) CHARACTER SET latin1 NOT NULL DEFAULT '0' COMMENT 'role name',
  `description` varchar(100) DEFAULT '0' COMMENT 'description',
  `created_time` timestamp NULL DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_idx` (`role_name`),
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth role table';

-- Dumping data for table auth_role: ~0 rows (approximately)
/*!40000 ALTER TABLE `auth_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_role` ENABLE KEYS */;


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
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth user table';

-- Dumping data for table auth_user: ~0 rows (approximately)
/*!40000 ALTER TABLE `auth_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user` ENABLE KEYS */;


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
  KEY `time_idx` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth user role';

-- Dumping data for table auth_user_role: ~0 rows (approximately)
/*!40000 ALTER TABLE `auth_user_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user_role` ENABLE KEYS */;


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
  UNIQUE KEY `slb_id` (`slb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table build_info: ~0 rows (approximately)
/*!40000 ALTER TABLE `build_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `build_info` ENABLE KEYS */;


-- Dumping structure for table conf_group_active
DROP TABLE IF EXISTS `conf_group_active`;
CREATE TABLE IF NOT EXISTS `conf_group_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table conf_group_active: ~0 rows (approximately)
/*!40000 ALTER TABLE `conf_group_active` DISABLE KEYS */;
/*!40000 ALTER TABLE `conf_group_active` ENABLE KEYS */;


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
  UNIQUE KEY `group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table conf_group_slb_active: ~0 rows (approximately)
/*!40000 ALTER TABLE `conf_group_slb_active` DISABLE KEYS */;
/*!40000 ALTER TABLE `conf_group_slb_active` ENABLE KEYS */;


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
  UNIQUE KEY `slb_id` (`slb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table conf_slb_active: ~0 rows (approximately)
/*!40000 ALTER TABLE `conf_slb_active` DISABLE KEYS */;
/*!40000 ALTER TABLE `conf_slb_active` ENABLE KEYS */;


-- Dumping structure for table dist_lock
DROP TABLE IF EXISTS `dist_lock`;
CREATE TABLE IF NOT EXISTS `dist_lock` (
  `lock_key` varchar(255) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'lock key',
  `created_time` bigint(20) DEFAULT NULL COMMENT 'create time',
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`lock_key`),
  KEY `dcl_key` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='distribution lock';

-- Dumping data for table dist_lock: ~0 rows (approximately)
/*!40000 ALTER TABLE `dist_lock` DISABLE KEYS */;
/*!40000 ALTER TABLE `dist_lock` ENABLE KEYS */;


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
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table group: ~0 rows (approximately)
/*!40000 ALTER TABLE `group` DISABLE KEYS */;
/*!40000 ALTER TABLE `group` ENABLE KEYS */;


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
  UNIQUE KEY `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table group_health_check: ~0 rows (approximately)
/*!40000 ALTER TABLE `group_health_check` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_health_check` ENABLE KEYS */;


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
  UNIQUE KEY `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table group_load_balancing_method: ~0 rows (approximately)
/*!40000 ALTER TABLE `group_load_balancing_method` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_load_balancing_method` ENABLE KEYS */;


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
  UNIQUE KEY `group_id_ip` (`group_id`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table group_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `group_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_server` ENABLE KEYS */;


-- Dumping structure for table group_slb
DROP TABLE IF EXISTS `group_slb`;
CREATE TABLE IF NOT EXISTS `group_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) DEFAULT '0',
  `path` varchar(200) NOT NULL DEFAULT '0',
  `rewrite` varchar(255) DEFAULT NULL,
  `priority` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id_slb_virtual_server_id` (`group_id`,`slb_virtual_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table group_slb: ~0 rows (approximately)
/*!40000 ALTER TABLE `group_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_slb` ENABLE KEYS */;


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
  UNIQUE KEY `slb_id_version` (`slb_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table nginx_conf: ~0 rows (approximately)
/*!40000 ALTER TABLE `nginx_conf` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_conf` ENABLE KEYS */;


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
  UNIQUE KEY `slb_virtual_server_id_version` (`slb_virtual_server_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table nginx_conf_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `nginx_conf_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_conf_server` ENABLE KEYS */;


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
  UNIQUE KEY `slb_virtual_server_id_version` (`slb_virtual_server_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table nginx_conf_upstream: ~0 rows (approximately)
/*!40000 ALTER TABLE `nginx_conf_upstream` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_conf_upstream` ENABLE KEYS */;


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
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table nginx_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `nginx_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `nginx_server` ENABLE KEYS */;


-- Dumping structure for table server
DROP TABLE IF EXISTS `server`;
CREATE TABLE IF NOT EXISTS `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(100) NOT NULL DEFAULT '0',
  `host_name` varchar(100) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table server: ~0 rows (approximately)
/*!40000 ALTER TABLE `server` DISABLE KEYS */;
/*!40000 ALTER TABLE `server` ENABLE KEYS */;


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
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table slb: ~0 rows (approximately)
/*!40000 ALTER TABLE `slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb` ENABLE KEYS */;


-- Dumping structure for table slb_domain
DROP TABLE IF EXISTS `slb_domain`;
CREATE TABLE IF NOT EXISTS `slb_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_name` (`slb_virtual_server_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table slb_domain: ~0 rows (approximately)
/*!40000 ALTER TABLE `slb_domain` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_domain` ENABLE KEYS */;


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
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table slb_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `slb_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_server` ENABLE KEYS */;


-- Dumping structure for table slb_vip
DROP TABLE IF EXISTS `slb_vip`;
CREATE TABLE IF NOT EXISTS `slb_vip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table slb_vip: ~0 rows (approximately)
/*!40000 ALTER TABLE `slb_vip` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_vip` ENABLE KEYS */;


-- Dumping structure for table slb_virtual_server
DROP TABLE IF EXISTS `slb_virtual_server`;
CREATE TABLE IF NOT EXISTS `slb_virtual_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `port` varchar(200) NOT NULL DEFAULT '0',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_name` (`slb_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table slb_virtual_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `slb_virtual_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `slb_virtual_server` ENABLE KEYS */;


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
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_group_id_ip` (`slb_virtual_server_id`,`group_id`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table status_group_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `status_group_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_group_server` ENABLE KEYS */;


-- Dumping structure for table status_server
DROP TABLE IF EXISTS `status_server`;
CREATE TABLE IF NOT EXISTS `status_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(200) NOT NULL,
  `up` bit(1) NOT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table status_server: ~0 rows (approximately)
/*!40000 ALTER TABLE `status_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_server` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

-- Dumping structure for table report
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
	`group_id` BIGINT(20) NOT NULL DEFAULT '0' COMMENT 'group primary key',
	`status` INT(11) NOT NULL DEFAULT '0' COMMENT 'status',
	`description` VARCHAR(255) NOT NULL DEFAULT '0' COMMENT 'status description',
	`reported_version` INT(11) NOT NULL DEFAULT '0' COMMENT 'the version reported',
	`current_version` INT(11) NOT NULL DEFAULT '0' COMMENT 'the version to report',
	UNIQUE INDEX `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;