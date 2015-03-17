-- --------------------------------------------------------
-- Host:                         10.2.25.93
-- Server version:               10.0.14-MariaDB - MariaDB Server
-- Server OS:                    Linux
-- HeidiSQL Version:             9.1.0.4867
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table zeus.app
CREATE TABLE IF NOT EXISTS `app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL DEFAULT '0',
  `app_id` varchar(200) NOT NULL DEFAULT '0',
  `version` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.app: ~3 rows (approximately)
DELETE FROM `app`;
/*!40000 ALTER TABLE `app` DISABLE KEYS */;
INSERT INTO `app` (`id`, `name`, `app_id`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'h5GatewayFat', '921812', 11, '2015-03-10 17:10:03', '2015-03-17 19:19:17'),
	(2, 'h5GatewayUat', '921812', 3, '2015-03-10 17:13:52', '2015-03-17 19:19:32'),
	(11, 'h5GatewayUatApi', '921812', 2, '2015-03-11 16:19:31', '2015-03-17 19:19:41'),
	(54, 'h5GatewayFat10108', '921812', 2, '2015-03-16 17:55:19', '2015-03-17 19:19:51');
/*!40000 ALTER TABLE `app` ENABLE KEYS */;


-- Dumping structure for table zeus.app_health_check
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
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.app_health_check: ~3 rows (approximately)
DELETE FROM `app_health_check`;
/*!40000 ALTER TABLE `app_health_check` DISABLE KEYS */;
INSERT INTO `app_health_check` (`id`, `app_id`, `uri`, `intervals`, `fails`, `passes`, `created_time`, `last_modified`) VALUES
	(1, 1, '/domaininfo/OnService.html', 5000, 1, 1, '2015-03-10 17:10:03', '2015-03-17 19:19:17'),
	(2, 2, '/domaininfo/OnService.html', 15000, 1, 1, '2015-03-10 17:13:52', '2015-03-17 19:19:33'),
	(11, 11, '/domaininfo/OnService.html', 15000, 1, 1, '2015-03-11 16:19:31', '2015-03-17 19:19:41'),
	(71, 54, '/domaininfo/OnService.html', 5000, 1, 1, '2015-03-16 17:55:19', '2015-03-17 19:19:51');
/*!40000 ALTER TABLE `app_health_check` ENABLE KEYS */;


-- Dumping structure for table zeus.app_load_balancing_method
CREATE TABLE IF NOT EXISTS `app_load_balancing_method` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `type` varchar(100) NOT NULL DEFAULT '0',
  `value` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id` (`app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.app_load_balancing_method: ~3 rows (approximately)
DELETE FROM `app_load_balancing_method`;
/*!40000 ALTER TABLE `app_load_balancing_method` DISABLE KEYS */;
INSERT INTO `app_load_balancing_method` (`id`, `app_id`, `type`, `value`, `created_time`, `last_modified`) VALUES
	(1, 1, 'roundrobin', 'test', '2015-03-10 17:10:03', '2015-03-17 19:19:17'),
	(2, 2, 'roundrobin', 'test', '2015-03-10 17:13:52', '2015-03-17 19:19:33'),
	(11, 11, 'roundrobin', 'test', '2015-03-11 16:19:31', '2015-03-17 19:19:41'),
	(32, 54, 'roundrobin', 'test', '2015-03-16 17:55:19', '2015-03-17 19:19:51');
/*!40000 ALTER TABLE `app_load_balancing_method` ENABLE KEYS */;


-- Dumping structure for table zeus.app_server
CREATE TABLE IF NOT EXISTS `app_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(200) NOT NULL DEFAULT '0',
  `port` int(11) NOT NULL DEFAULT '0',
  `weight` int(11) NOT NULL DEFAULT '0',
  `max_fails` int(11) NOT NULL DEFAULT '0',
  `fail_timeout` int(11) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id_ip` (`app_id`,`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.app_server: ~6 rows (approximately)
DELETE FROM `app_server`;
/*!40000 ALTER TABLE `app_server` DISABLE KEYS */;
INSERT INTO `app_server` (`id`, `app_id`, `ip`, `port`, `weight`, `max_fails`, `fail_timeout`, `created_time`, `last_modified`) VALUES
	(7, 1, '10.2.6.201', 8080, 1, 2, 30, '2015-03-10 22:35:25', '2015-03-17 19:19:17'),
	(8, 1, '10.2.6.202', 8080, 2, 2, 30, '2015-03-10 22:35:25', '2015-03-17 19:19:17'),
	(19, 2, '10.2.24.69', 8080, 1, 2, 30, '2015-03-11 14:43:38', '2015-03-17 19:19:33'),
	(20, 2, '10.2.24.70', 8080, 2, 2, 30, '2015-03-11 14:43:38', '2015-03-17 19:19:33'),
	(23, 11, '10.2.24.69', 8080, 1, 2, 30, '2015-03-11 16:21:12', '2015-03-17 19:19:41'),
	(24, 11, '10.2.24.70', 8080, 2, 2, 30, '2015-03-11 16:21:12', '2015-03-17 19:19:41'),
	(63, 54, '10.2.6.201', 8080, 1, 2, 30, '2015-03-16 17:55:19', '2015-03-17 19:19:51'),
	(64, 54, '10.2.6.202', 8080, 2, 2, 30, '2015-03-16 17:55:19', '2015-03-17 19:19:51');
/*!40000 ALTER TABLE `app_server` ENABLE KEYS */;


-- Dumping structure for table zeus.app_slb
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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.app_slb: ~3 rows (approximately)
DELETE FROM `app_slb`;
/*!40000 ALTER TABLE `app_slb` DISABLE KEYS */;
INSERT INTO `app_slb` (`id`, `app_name`, `slb_name`, `slb_virtual_server_name`, `path`, `created_time`, `last_modified`) VALUES
	(1, 'h5GatewayFat', 'default', 'site1', '/', '2015-03-10 17:10:03', '2015-03-17 19:19:17'),
	(7, 'h5GatewayUat', 'default', 'site2', '/', '2015-03-11 14:18:05', '2015-03-17 19:19:33'),
	(11, 'h5GatewayUatApi', 'default', 'site2', '/restapi', '2015-03-11 16:19:31', '2015-03-17 19:19:41'),
	(32, 'h5GatewayFat10108', 'default', 'site1', '/restapi/soa2/10108', '2015-03-16 17:55:19', '2015-03-17 19:19:51');
/*!40000 ALTER TABLE `app_slb` ENABLE KEYS */;


-- Dumping structure for table zeus.archive_app
CREATE TABLE IF NOT EXISTS `archive_app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`name`,`version`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.archive_app: ~5 rows (approximately)
DELETE FROM `archive_app`;
/*!40000 ALTER TABLE `archive_app` DISABLE KEYS */;
INSERT INTO `archive_app` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="1">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 1, '2015-03-14 21:55:52', '2015-03-14 21:55:52'),
	(3, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="6">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 6, '2015-03-14 21:58:43', '2015-03-14 21:58:43'),
	(4, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="7">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 7, '2015-03-14 21:58:52', '2015-03-14 21:58:52'),
	(5, 'h5GatewayUat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUat" app-id="921812" version="1">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="5" passes="2">\r\n      <uri>/domaininfo/xxxxx</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 1, '2015-03-15 19:14:23', '2015-03-15 19:14:23'),
	(6, 'h5GatewayUatApi', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUatApi" app-id="921812" version="1">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/restapi</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 1, '2015-03-15 19:14:47', '2015-03-15 19:14:47'),
	(7, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="8">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/xxxx.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 8, '2015-03-16 17:42:03', '2015-03-16 17:42:03'),
	(8, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="9">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="2">\r\n      <uri>/domaininfo/xxxx.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 9, '2015-03-16 17:44:58', '2015-03-16 17:44:58'),
	(9, 'h5GatewayFat10108', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat10108" app-id="921812" version="1">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/restapi/soa2/10108</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 1, '2015-03-16 17:55:19', '2015-03-16 17:55:19'),
	(10, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="10">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 10, '2015-03-17 19:14:41', '2015-03-17 19:14:41'),
	(11, 'h5GatewayUat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUat" app-id="921812" version="2">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 2, '2015-03-17 19:18:02', '2015-03-17 19:18:02'),
	(12, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="11">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 11, '2015-03-17 19:19:17', '2015-03-17 19:19:17'),
	(13, 'h5GatewayUat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUat" app-id="921812" version="3">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 3, '2015-03-17 19:19:33', '2015-03-17 19:19:33'),
	(14, 'h5GatewayUatApi', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUatApi" app-id="921812" version="2">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/restapi</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 2, '2015-03-17 19:19:41', '2015-03-17 19:19:41'),
	(15, 'h5GatewayFat10108', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat10108" app-id="921812" version="2">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/restapi/soa2/10108</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 2, '2015-03-17 19:19:51', '2015-03-17 19:19:51');
/*!40000 ALTER TABLE `archive_app` ENABLE KEYS */;


-- Dumping structure for table zeus.archive_slb
CREATE TABLE IF NOT EXISTS `archive_slb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`name`,`version`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.archive_slb: ~4 rows (approximately)
DELETE FROM `archive_slb`;
/*!40000 ALTER TABLE `archive_slb` DISABLE KEYS */;
INSERT INTO `archive_slb` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="1">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 1, '2015-03-14 22:00:53', '2015-03-14 22:00:53'),
	(2, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="2">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 2, '2015-03-14 22:00:54', '2015-03-14 22:00:54'),
	(3, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="3">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 3, '2015-03-14 22:00:55', '2015-03-14 22:00:55'),
	(4, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="4">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 4, '2015-03-15 19:37:35', '2015-03-15 19:37:35'),
	(5, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="5">\r\n   <nginx-bin>/opt/app/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/opt/app/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>2</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 5, '2015-03-16 17:34:12', '2015-03-16 17:34:12');
/*!40000 ALTER TABLE `archive_slb` ENABLE KEYS */;


-- Dumping structure for table zeus.build_info
CREATE TABLE IF NOT EXISTS `build_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `pending_ticket` int(11) DEFAULT NULL,
  `current_ticket` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.build_info: ~1 rows (approximately)
DELETE FROM `build_info`;
/*!40000 ALTER TABLE `build_info` DISABLE KEYS */;
INSERT INTO `build_info` (`id`, `name`, `pending_ticket`, `current_ticket`, `created_time`, `last_modified`) VALUES
	(2, 'default', 85, 85, '2015-03-15 17:03:31', '2015-03-17 19:19:52');
/*!40000 ALTER TABLE `build_info` ENABLE KEYS */;


-- Dumping structure for table zeus.conf_app_active
CREATE TABLE IF NOT EXISTS `conf_app_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.conf_app_active: ~3 rows (approximately)
DELETE FROM `conf_app_active`;
/*!40000 ALTER TABLE `conf_app_active` DISABLE KEYS */;
INSERT INTO `conf_app_active` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(8, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="11">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 11, NULL, '2015-03-17 19:19:18'),
	(62, 'h5GatewayUatApi', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUatApi" app-id="921812" version="2">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/restapi</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 2, '2015-03-15 19:20:51', '2015-03-17 19:19:43'),
	(63, 'h5GatewayUat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayUat" app-id="921812" version="3">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site2" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s2a.ctrip.com"/>\r\n               <domain name="s2b.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="15000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.69</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.24.70</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 3, '2015-03-15 19:21:02', '2015-03-17 19:19:33'),
	(75, 'h5GatewayFat10108', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat10108" app-id="921812" version="2">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/restapi/soa2/10108</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="5000" fails="1" passes="1">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 2, '2015-03-16 17:56:22', '2015-03-17 19:19:52');
/*!40000 ALTER TABLE `conf_app_active` ENABLE KEYS */;


-- Dumping structure for table zeus.conf_slb_active
CREATE TABLE IF NOT EXISTS `conf_slb_active` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.conf_slb_active: ~1 rows (approximately)
DELETE FROM `conf_slb_active`;
/*!40000 ALTER TABLE `conf_slb_active` DISABLE KEYS */;
INSERT INTO `conf_slb_active` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(5, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="5">\r\n   <nginx-bin>/opt/app/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/opt/app/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>2</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 5, '2015-03-15 14:55:07', '2015-03-16 17:48:06');
/*!40000 ALTER TABLE `conf_slb_active` ENABLE KEYS */;


-- Dumping structure for table zeus.nginx_conf
CREATE TABLE IF NOT EXISTS `nginx_conf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `content` mediumtext,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_version` (`name`,`version`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.nginx_conf: ~32 rows (approximately)
DELETE FROM `nginx_conf`;
/*!40000 ALTER TABLE `nginx_conf` DISABLE KEYS */;
INSERT INTO `nginx_conf` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(79, 'default', 'worker_processes 2;\nevents {\n    worker_connections 1024;\n}\nhttp {\n    include    mime.types;\n    default_type    application/octet-stream;\n    keepalive_timeout    65;\nserver {\n    listen    10001;\n    location / {\n        add_header Access-Control-Allow-Origin *;\n        check_status;\n    }\n    location =/status.json {\n        add_header Access-Control-Allow-Origin *;\n        check_status json;\n    }\n}\n    include    upstreams/*.conf;\n    include    vhosts/*.conf;\n}\n', 85, '2015-03-17 19:19:52', '2015-03-17 19:19:52');
/*!40000 ALTER TABLE `nginx_conf` ENABLE KEYS */;


-- Dumping structure for table zeus.nginx_conf_server
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
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.nginx_conf_server: ~40 rows (approximately)
DELETE FROM `nginx_conf_server`;
/*!40000 ALTER TABLE `nginx_conf_server` DISABLE KEYS */;
INSERT INTO `nginx_conf_server` (`id`, `slb_name`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(131, 'default', 'site1', 'server {\n    listen    80;\n    server_name     s1.ctrip.com;\n    location /{\n        check_status;\n        proxy_pass http://backend_site1_h5GatewayFat;\n    }\n    location /restapi/soa2/10108{\n        check_status;\n        proxy_pass http://backend_site1_h5GatewayFat10108;\n    }\n}\n', 85, '2015-03-17 19:19:52', '2015-03-17 19:19:52'),
	(132, 'default', 'site2', 'server {\n    listen    80;\n    server_name     s2a.ctrip.com s2b.ctrip.com;\n    location /restapi{\n        check_status;\n        proxy_pass http://backend_site2_h5GatewayUatApi;\n    }\n    location /{\n        check_status;\n        proxy_pass http://backend_site2_h5GatewayUat;\n    }\n}\n', 85, '2015-03-17 19:19:52', '2015-03-17 19:19:52');
/*!40000 ALTER TABLE `nginx_conf_server` ENABLE KEYS */;


-- Dumping structure for table zeus.nginx_conf_upstream
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
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.nginx_conf_upstream: ~43 rows (approximately)
DELETE FROM `nginx_conf_upstream`;
/*!40000 ALTER TABLE `nginx_conf_upstream` DISABLE KEYS */;
INSERT INTO `nginx_conf_upstream` (`id`, `slb_name`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(131, 'default', 'site1', 'upstream backend_site1_h5GatewayFat {\n        server 10.2.6.201:8080 weight=1 max_fails=2 fail_timeout=30;\n    server 10.2.6.202:8080 weight=2 max_fails=2 fail_timeout=30;\n    check interval=5000 rise=1 fall=1 timeout=1000 type=http;\n    check_keepalive_requests 100;\n    check_http_send "GET /domaininfo/OnService.html HTTP/1.0\\r\\nConnection: keep-alive\\r\\nHost: s1.ctrip.com\\r\\n\\r\\n";\n    check_http_expect_alive http_2xx http_3xx;\n}\nupstream backend_site1_h5GatewayFat10108 {\n        server 10.2.6.201:8080 weight=1 max_fails=2 fail_timeout=30 down;\n    server 10.2.6.202:8080 weight=2 max_fails=2 fail_timeout=30;\n    check interval=5000 rise=1 fall=1 timeout=1000 type=http;\n    check_keepalive_requests 100;\n    check_http_send "GET /domaininfo/OnService.html HTTP/1.0\\r\\nConnection: keep-alive\\r\\nHost: s1.ctrip.com\\r\\n\\r\\n";\n    check_http_expect_alive http_2xx http_3xx;\n}\n', 85, '2015-03-17 19:19:52', '2015-03-17 19:19:52'),
	(132, 'default', 'site2', 'upstream backend_site2_h5GatewayUatApi {\n        server 10.2.24.69:8080 weight=1 max_fails=2 fail_timeout=30;\n    server 10.2.24.70:8080 weight=2 max_fails=2 fail_timeout=30;\n    check interval=15000 rise=1 fall=1 timeout=1000 type=http;\n    check_keepalive_requests 100;\n    check_http_send "GET /domaininfo/OnService.html HTTP/1.0\\r\\nConnection: keep-alive\\r\\nHost: s2a.ctrip.com\\r\\n\\r\\n";\n    check_http_expect_alive http_2xx http_3xx;\n}\nupstream backend_site2_h5GatewayUat {\n        server 10.2.24.69:8080 weight=1 max_fails=2 fail_timeout=30;\n    server 10.2.24.70:8080 weight=2 max_fails=2 fail_timeout=30;\n    check interval=15000 rise=1 fall=1 timeout=1000 type=http;\n    check_keepalive_requests 100;\n    check_http_send "GET /domaininfo/OnService.html HTTP/1.0\\r\\nConnection: keep-alive\\r\\nHost: s2a.ctrip.com\\r\\n\\r\\n";\n    check_http_expect_alive http_2xx http_3xx;\n}\n', 85, '2015-03-17 19:19:52', '2015-03-17 19:19:52');
/*!40000 ALTER TABLE `nginx_conf_upstream` ENABLE KEYS */;


-- Dumping structure for table zeus.nginx_server
CREATE TABLE IF NOT EXISTS `nginx_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_name` varchar(200) DEFAULT NULL,
  `ip` varchar(200) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.nginx_server: ~3 rows (approximately)
DELETE FROM `nginx_server`;
/*!40000 ALTER TABLE `nginx_server` DISABLE KEYS */;
INSERT INTO `nginx_server` (`id`, `slb_name`, `ip`, `version`, `created_time`, `last_modified`) VALUES
	(30, 'default', '10.2.25.93', 84, '2015-03-15 19:37:35', '2015-03-17 19:19:52'),
	(31, 'default', '10.2.25.94', 84, '2015-03-15 19:37:35', '2015-03-17 19:19:53'),
	(32, 'default', '10.2.25.95', 84, '2015-03-15 19:37:35', '2015-03-17 19:19:53');
/*!40000 ALTER TABLE `nginx_server` ENABLE KEYS */;


-- Dumping structure for table zeus.server
CREATE TABLE IF NOT EXISTS `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(100) NOT NULL DEFAULT '0',
  `host_name` varchar(100) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.server: ~8 rows (approximately)
DELETE FROM `server`;
/*!40000 ALTER TABLE `server` DISABLE KEYS */;
INSERT INTO `server` (`id`, `ip`, `host_name`, `created_time`, `last_modified`) VALUES
	(1, '192.2.6.201', 'gateway1-h5', '2015-03-10 17:10:03', '2015-03-10 17:10:03'),
	(2, '192.2.6.202', 'gateway2-h5', '2015-03-10 17:10:03', '2015-03-10 17:10:03'),
	(3, '192.2.24.69', 'uat0068-app', '2015-03-10 17:13:52', '2015-03-11 16:19:31'),
	(4, '192.2.24.70', 'uat0069-app', '2015-03-10 17:13:52', '2015-03-11 16:19:31'),
	(5, '10.2.24.69', 'uat0068-app', '2015-03-10 22:34:53', '2015-03-12 20:52:19'),
	(6, '10.2.24.70', 'uat0069-app', '2015-03-10 22:34:53', '2015-03-12 20:52:19'),
	(7, '10.2.6.201', 'gateway1-h5', '2015-03-10 22:35:25', '2015-03-11 14:38:19'),
	(8, '10.2.6.202', 'gateway2-h5', '2015-03-10 22:35:25', '2015-03-11 14:38:19');
/*!40000 ALTER TABLE `server` ENABLE KEYS */;


-- Dumping structure for table zeus.slb
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.slb: ~1 rows (approximately)
DELETE FROM `slb`;
/*!40000 ALTER TABLE `slb` DISABLE KEYS */;
INSERT INTO `slb` (`id`, `name`, `nginx_bin`, `nginx_conf`, `nginx_worker_processes`, `status`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'default', '/opt/app/nginx/sbin', '/opt/app/nginx/conf', 2, 'TEST', 5, '2015-03-10 16:58:16', '2015-03-16 17:34:12');
/*!40000 ALTER TABLE `slb` ENABLE KEYS */;


-- Dumping structure for table zeus.slb_domain
CREATE TABLE IF NOT EXISTS `slb_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_virtual_server_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slb_virtual_server_id_name` (`slb_virtual_server_id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.slb_domain: ~3 rows (approximately)
DELETE FROM `slb_domain`;
/*!40000 ALTER TABLE `slb_domain` DISABLE KEYS */;
INSERT INTO `slb_domain` (`id`, `slb_virtual_server_id`, `name`, `created_time`, `last_modified`) VALUES
	(1, 1, 's1.ctrip.com', '2015-03-10 16:58:16', '2015-03-16 17:34:12'),
	(2, 2, 's2a.ctrip.com', '2015-03-10 16:58:16', '2015-03-16 17:34:12'),
	(3, 2, 's2b.ctrip.com', '2015-03-10 16:58:16', '2015-03-16 17:34:12');
/*!40000 ALTER TABLE `slb_domain` ENABLE KEYS */;


-- Dumping structure for table zeus.slb_server
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
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.slb_server: ~3 rows (approximately)
DELETE FROM `slb_server`;
/*!40000 ALTER TABLE `slb_server` DISABLE KEYS */;
INSERT INTO `slb_server` (`id`, `slb_id`, `ip`, `host_name`, `enable`, `created_time`, `last_modified`) VALUES
	(1, 1, '10.2.25.93', 'uat0358', b'1', '2015-03-10 16:58:16', '2015-03-16 17:34:12'),
	(2, 1, '10.2.25.94', 'uat0359', b'1', '2015-03-10 16:58:16', '2015-03-16 17:34:12'),
	(3, 1, '10.2.25.95', 'uat0360', b'1', '2015-03-10 16:58:16', '2015-03-16 17:34:12');
/*!40000 ALTER TABLE `slb_server` ENABLE KEYS */;


-- Dumping structure for table zeus.slb_vip
CREATE TABLE IF NOT EXISTS `slb_vip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slb_id` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(50) NOT NULL DEFAULT '0',
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.slb_vip: ~1 rows (approximately)
DELETE FROM `slb_vip`;
/*!40000 ALTER TABLE `slb_vip` DISABLE KEYS */;
INSERT INTO `slb_vip` (`id`, `slb_id`, `ip`, `created_time`, `last_modified`) VALUES
	(1, 1, '10.2.25.93', '2015-03-10 16:58:16', '2015-03-16 17:34:12');
/*!40000 ALTER TABLE `slb_vip` ENABLE KEYS */;


-- Dumping structure for table zeus.slb_virtual_server
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.slb_virtual_server: ~2 rows (approximately)
DELETE FROM `slb_virtual_server`;
/*!40000 ALTER TABLE `slb_virtual_server` DISABLE KEYS */;
INSERT INTO `slb_virtual_server` (`id`, `slb_id`, `name`, `port`, `is_ssl`, `created_time`, `last_modified`) VALUES
	(1, 1, 'site1', '80', b'0', '2015-03-10 16:58:16', '2015-03-16 17:34:12'),
	(2, 1, 'site2', '80', b'0', '2015-03-10 16:58:16', '2015-03-16 17:34:12');
/*!40000 ALTER TABLE `slb_virtual_server` ENABLE KEYS */;


-- Dumping structure for table zeus.status_app_server
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.status_app_server: ~0 rows (approximately)
DELETE FROM `status_app_server`;
/*!40000 ALTER TABLE `status_app_server` DISABLE KEYS */;
INSERT INTO `status_app_server` (`id`, `slb_name`, `virtual_server_name`, `app_name`, `ip`, `up`, `created_time`, `last_modified`) VALUES
	(1, 'default', 'site1', 'h5GatewayFat', '10.2.6.201', b'1', '2015-03-16 13:30:51', '2015-03-16 14:36:15'),
	(6, 'default', 'site2', 'h5GatewayUat', '10.2.24.69', b'1', '2015-03-16 14:14:53', '2015-03-16 14:15:21'),
	(11, 'default', 'site1', 'h5GatewayFat10108', '10.2.6.201', b'0', '2015-03-16 18:00:51', '2015-03-16 18:00:53');
/*!40000 ALTER TABLE `status_app_server` ENABLE KEYS */;


-- Dumping structure for table zeus.status_server
CREATE TABLE IF NOT EXISTS `status_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(200) NOT NULL,
  `up` bit(1) NOT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;

-- Dumping data for table zeus.status_server: ~0 rows (approximately)
DELETE FROM `status_server`;
/*!40000 ALTER TABLE `status_server` DISABLE KEYS */;
INSERT INTO `status_server` (`id`, `ip`, `up`, `created_time`, `last_modified`) VALUES
	(1, '10.2.6.201', b'1', '2015-03-16 13:29:38', '2015-03-16 17:58:43'),
	(7, '10.2.24.69', b'1', '2015-03-16 14:15:48', '2015-03-16 14:26:05');
/*!40000 ALTER TABLE `status_server` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
