-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.5.28 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL Version:             9.1.0.4867
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table zeus.application
CREATE TABLE IF NOT EXISTS `application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL DEFAULT '0',
  `app_id` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.app_health_check
CREATE TABLE IF NOT EXISTS `app_health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL DEFAULT '0',
  `uri` varchar(200) NOT NULL DEFAULT '0',
  `interval` int(11) NOT NULL DEFAULT '0',
  `fails` int(11) NOT NULL DEFAULT '0',
  `passes` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.app_load_balancing_method
CREATE TABLE IF NOT EXISTS `app_load_balancing_method` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL DEFAULT '0',
  `type` varchar(100) NOT NULL DEFAULT '0',
  `value` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.app_server
CREATE TABLE IF NOT EXISTS `app_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL DEFAULT '0',
  `server_id` bigint(20) NOT NULL DEFAULT '0',
  `port` int(11) NOT NULL DEFAULT '0',
  `weight` int(11) NOT NULL DEFAULT '0',
  `max_fails` int(11) NOT NULL DEFAULT '0',
  `fail_timeout` int(11) NOT NULL DEFAULT '0',
  `healthy` bit(1) NOT NULL DEFAULT b'0',
  `enable` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id_server_id` (`application_id`,`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.app_slb
CREATE TABLE IF NOT EXISTS `app_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL DEFAULT '0',
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `path` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id_slb_virtual_server_id` (`application_id`,`slb_virtual_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.server
CREATE TABLE IF NOT EXISTS `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(100) NOT NULL DEFAULT '0',
  `host_name` varchar(100) NOT NULL DEFAULT '0',
  `up` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.slb_cluster
CREATE TABLE IF NOT EXISTS `slb_cluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '0',
  `nginx_bin` varchar(300) NOT NULL DEFAULT '0',
  `nginx_conf` varchar(300) NOT NULL DEFAULT '0',
  `nginx_worker_processes` int(11) NOT NULL DEFAULT '0',
  `status` varchar(300) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.slb_domain
CREATE TABLE IF NOT EXISTS `slb_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `port` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.slb_server
CREATE TABLE IF NOT EXISTS `slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_cluster_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `host_name` varchar(200) NOT NULL DEFAULT '0',
  `enable` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.slb_vip
CREATE TABLE IF NOT EXISTS `slb_vip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_cluster_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table zeus.slb_virtual_server
CREATE TABLE IF NOT EXISTS `slb_virtual_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_cluster_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
