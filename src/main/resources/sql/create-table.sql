-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        5.6.10 - MySQL Community Server (GPL)
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  9.1.0.4867
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- 导出 zeus 的数据库结构
DROP DATABASE IF EXISTS `zeus_test`;
CREATE DATABASE IF NOT EXISTS `zeus_test` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `zeus_test`;


-- 导出  表 zeus_test.app 结构
DROP TABLE IF EXISTS `app`;
CREATE TABLE IF NOT EXISTS `app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL DEFAULT '0',
  `app_id` varchar(200) NOT NULL DEFAULT '0',
  `version` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.app_health_check 结构
DROP TABLE IF EXISTS `app_health_check`;
CREATE TABLE IF NOT EXISTS `app_health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `uri` varchar(200) NOT NULL DEFAULT '0',
  `intervals` int(11) NOT NULL DEFAULT '0',
  `fails` int(11) NOT NULL DEFAULT '0',
  `passes` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.app_load_balancing_method 结构
DROP TABLE IF EXISTS `app_load_balancing_method`;
CREATE TABLE IF NOT EXISTS `app_load_balancing_method` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `type` varchar(100) NOT NULL DEFAULT '0',
  `value` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.app_server 结构
DROP TABLE IF EXISTS `app_server`;
CREATE TABLE IF NOT EXISTS `app_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(200) NOT NULL DEFAULT '0',
  `host_name` varchar(200) NOT NULL DEFAULT '0',
  `port` int(11) NOT NULL DEFAULT '0',
  `weight` int(11) NOT NULL DEFAULT '0',
  `max_fails` int(11) NOT NULL DEFAULT '0',
  `fail_timeout` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id_ip` (`app_id`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.app_slb 结构
DROP TABLE IF EXISTS `app_slb`;
CREATE TABLE IF NOT EXISTS `app_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(200) NOT NULL DEFAULT '0',
  `slb_name` varchar(200) NOT NULL DEFAULT '0',
  `slb_virtual_server_name` varchar(200) NOT NULL DEFAULT '0',
  `path` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_name_slb_name_slb_virtual_server_name` (`app_name`,`slb_name`,`slb_virtual_server_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.archive_app 结构
DROP TABLE IF EXISTS `archive_app`;
CREATE TABLE IF NOT EXISTS `archive_app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`name`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.archive_slb 结构
DROP TABLE IF EXISTS `archive_slb`;
CREATE TABLE IF NOT EXISTS `archive_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`name`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.build_info 结构
DROP TABLE IF EXISTS `build_info`;
CREATE TABLE IF NOT EXISTS `build_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `pending_ticket` int(11) DEFAULT NULL,
  `current_ticket` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.conf_app_active 结构
DROP TABLE IF EXISTS `conf_app_active`;
CREATE TABLE IF NOT EXISTS `conf_app_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.conf_app_slb_active 结构
DROP TABLE IF EXISTS `conf_app_slb_active`;
CREATE TABLE IF NOT EXISTS `conf_app_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(200) NOT NULL,
  `slb_name` varchar(200) NOT NULL,
  `slb_virtual_server_name` varchar(200) NOT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_name_slb_name_slb_virtual_server_name` (`app_name`,`slb_name`,`slb_virtual_server_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.conf_slb_active 结构
DROP TABLE IF EXISTS `conf_slb_active`;
CREATE TABLE IF NOT EXISTS `conf_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.nginx_conf 结构
DROP TABLE IF EXISTS `nginx_conf`;
CREATE TABLE IF NOT EXISTS `nginx_conf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`name`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.nginx_conf_server 结构
DROP TABLE IF EXISTS `nginx_conf_server`;
CREATE TABLE IF NOT EXISTS `nginx_conf_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_name` varchar(200) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_name_name_version` (`slb_name`,`name`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.nginx_conf_upstream 结构
DROP TABLE IF EXISTS `nginx_conf_upstream`;
CREATE TABLE IF NOT EXISTS `nginx_conf_upstream` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_name` varchar(200) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_name_name_version` (`slb_name`,`name`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.nginx_server 结构
DROP TABLE IF EXISTS `nginx_server`;
CREATE TABLE IF NOT EXISTS `nginx_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_name` varchar(200) DEFAULT NULL,
  `ip` varchar(200) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.server 结构
DROP TABLE IF EXISTS `server`;
CREATE TABLE IF NOT EXISTS `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(100) NOT NULL DEFAULT '0',
  `host_name` varchar(100) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.slb 结构
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
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.slb_domain 结构
DROP TABLE IF EXISTS `slb_domain`;
CREATE TABLE IF NOT EXISTS `slb_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_name` (`slb_virtual_server_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.slb_server 结构
DROP TABLE IF EXISTS `slb_server`;
CREATE TABLE IF NOT EXISTS `slb_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `host_name` varchar(200) NOT NULL DEFAULT '0',
  `enable` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.slb_vip 结构
DROP TABLE IF EXISTS `slb_vip`;
CREATE TABLE IF NOT EXISTS `slb_vip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.slb_virtual_server 结构
DROP TABLE IF EXISTS `slb_virtual_server`;
CREATE TABLE IF NOT EXISTS `slb_virtual_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `port` varchar(200) NOT NULL DEFAULT '0',
  `is_ssl` bit(1) NOT NULL DEFAULT b'0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_id_name` (`slb_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.status_app_server 结构
DROP TABLE IF EXISTS `status_app_server`;
CREATE TABLE IF NOT EXISTS `status_app_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_name` varchar(200) NOT NULL DEFAULT '0',
  `virtual_server_name` varchar(200) NOT NULL DEFAULT '0',
  `app_name` varchar(200) NOT NULL,
  `ip` varchar(200) NOT NULL,
  `up` bit(1) NOT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_name_virtual_server_name_app_name_ip` (`slb_name`,`virtual_server_name`,`app_name`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 导出  表 zeus_test.status_server 结构
DROP TABLE IF EXISTS `status_server`;
CREATE TABLE IF NOT EXISTS `status_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(200) NOT NULL,
  `up` bit(1) NOT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
DROP TABLE IF EXISTS `commit`;
CREATE TABLE `commit` (
  `id` bigint(20) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `slb_id` bigint(20) DEFAULT NULL,
  `vs_ids` varchar(4096) DEFAULT NULL,
  `group_ids` varchar(4096) DEFAULT NULL,
  `task_ids` varchar(4096) DEFAULT NULL,
  `cleanvs_ids` varchar(4096) DEFAULT NULL,
  `type` varchar(45) DEFAULT NULL,
  `DataChange_LastTime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
