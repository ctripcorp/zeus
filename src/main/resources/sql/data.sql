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
-- Dumping data for table zeus.app: ~3 rows (approximately)
DELETE FROM `app`;
/*!40000 ALTER TABLE `app` DISABLE KEYS */;
INSERT INTO `app` (`id`, `name`, `app_id`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'h5GatewayFat', '921812', 7, '2015-03-10 17:10:03', '2015-03-14 21:58:52'),
	(2, 'h5GatewayUat', '921812', 0, '2015-03-10 17:13:52', '2015-03-12 20:52:19'),
	(11, 'h5GatewayUatApi', '921812', 0, '2015-03-11 16:19:31', '2015-03-11 16:21:12');
/*!40000 ALTER TABLE `app` ENABLE KEYS */;

-- Dumping data for table zeus.app_health_check: ~3 rows (approximately)
DELETE FROM `app_health_check`;
/*!40000 ALTER TABLE `app_health_check` DISABLE KEYS */;
INSERT INTO `app_health_check` (`id`, `app_id`, `uri`, `intervals`, `fails`, `passes`, `created_time`, `last_modified`) VALUES
	(1, 1, '/domaininfo/OnService.html', 10000, 5, 2, '2015-03-10 17:10:03', '2015-03-14 21:58:52'),
	(2, 2, '/domaininfo/xxxxx', 15000, 5, 2, '2015-03-10 17:13:52', '2015-03-12 20:52:19'),
	(11, 11, '/domaininfo/OnService.html', 15000, 5, 2, '2015-03-11 16:19:31', '2015-03-11 16:21:12');
/*!40000 ALTER TABLE `app_health_check` ENABLE KEYS */;

-- Dumping data for table zeus.app_load_balancing_method: ~3 rows (approximately)
DELETE FROM `app_load_balancing_method`;
/*!40000 ALTER TABLE `app_load_balancing_method` DISABLE KEYS */;
INSERT INTO `app_load_balancing_method` (`id`, `app_id`, `type`, `value`, `created_time`, `last_modified`) VALUES
	(1, 1, 'roundrobin', 'test', '2015-03-10 17:10:03', '2015-03-14 21:58:52'),
	(2, 2, 'roundrobin', 'test', '2015-03-10 17:13:52', '2015-03-12 20:52:19'),
	(11, 11, 'roundrobin', 'test', '2015-03-11 16:19:31', '2015-03-11 16:21:12');
/*!40000 ALTER TABLE `app_load_balancing_method` ENABLE KEYS */;

-- Dumping data for table zeus.app_server: ~6 rows (approximately)
DELETE FROM `app_server`;
/*!40000 ALTER TABLE `app_server` DISABLE KEYS */;
INSERT INTO `app_server` (`id`, `app_id`, `ip`, `port`, `weight`, `max_fails`, `fail_timeout`, `created_time`, `last_modified`) VALUES
	(7, 1, '10.2.6.201', 8080, 1, 2, 30, '2015-03-10 22:35:25', '2015-03-14 21:58:52'),
	(8, 1, '10.2.6.202', 8080, 2, 2, 30, '2015-03-10 22:35:25', '2015-03-14 21:58:52'),
	(19, 2, '10.2.24.69', 8080, 1, 2, 30, '2015-03-11 14:43:38', '2015-03-12 20:52:19'),
	(20, 2, '10.2.24.70', 8080, 2, 2, 30, '2015-03-11 14:43:38', '2015-03-12 20:52:19'),
	(23, 11, '10.2.24.69', 8080, 1, 2, 30, '2015-03-11 16:21:12', '2015-03-11 16:21:12'),
	(24, 11, '10.2.24.70', 8080, 2, 2, 30, '2015-03-11 16:21:12', '2015-03-11 16:21:12');
/*!40000 ALTER TABLE `app_server` ENABLE KEYS */;

-- Dumping data for table zeus.app_slb: ~3 rows (approximately)
DELETE FROM `app_slb`;
/*!40000 ALTER TABLE `app_slb` DISABLE KEYS */;
INSERT INTO `app_slb` (`id`, `app_id`, `slb_name`, `slb_virtual_server_name`, `path`, `created_time`, `last_modified`) VALUES
	(1, 1, 'default', 'site1', '/', '2015-03-10 17:10:03', '2015-03-14 21:58:52'),
	(7, 2, 'default', 'site2', '/', '2015-03-11 14:18:05', '2015-03-12 20:52:19'),
	(11, 11, 'default', 'site2', '/restapi', '2015-03-11 16:19:31', '2015-03-11 16:21:12');
/*!40000 ALTER TABLE `app_slb` ENABLE KEYS */;

-- Dumping data for table zeus.archive_app: ~3 rows (approximately)
DELETE FROM `archive_app`;
/*!40000 ALTER TABLE `archive_app` DISABLE KEYS */;
INSERT INTO `archive_app` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="1">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 1, '2015-03-14 21:55:52', '2015-03-14 21:55:52'),
	(3, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="6">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 6, '2015-03-14 21:58:43', '2015-03-14 21:58:43'),
	(4, 'h5GatewayFat', '<?xml version="1.0" encoding="utf-8"?>\r\n<app name="h5GatewayFat" app-id="921812" version="7">\r\n   <app-slbs>\r\n      <app-slb>\r\n         <slb-name>default</slb-name>\r\n         <path>/</path>\r\n         <virtual-server name="site1" ssl="false">\r\n            <port>80</port>\r\n            <domains>\r\n               <domain name="s1.ctrip.com"/>\r\n            </domains>\r\n         </virtual-server>\r\n      </app-slb>\r\n   </app-slbs>\r\n   <health-check intervals="10000" fails="5" passes="2">\r\n      <uri>/domaininfo/OnService.html</uri>\r\n   </health-check>\r\n   <load-balancing-method type="roundrobin">\r\n      <value>test</value>\r\n   </load-balancing-method>\r\n   <app-servers>\r\n      <app-server port="8080" weight="1" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.201</ip>\r\n      </app-server>\r\n      <app-server port="8080" weight="2" max-fails="2" fail-timeout="30">\r\n         <ip>10.2.6.202</ip>\r\n      </app-server>\r\n   </app-servers>\r\n</app>\r\n', 7, '2015-03-14 21:58:52', '2015-03-14 21:58:52');
/*!40000 ALTER TABLE `archive_app` ENABLE KEYS */;

-- Dumping data for table zeus.archive_slb: ~3 rows (approximately)
DELETE FROM `archive_slb`;
/*!40000 ALTER TABLE `archive_slb` DISABLE KEYS */;
INSERT INTO `archive_slb` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="1">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 1, '2015-03-14 22:00:53', '2015-03-14 22:00:53'),
	(2, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="2">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 2, '2015-03-14 22:00:54', '2015-03-14 22:00:54'),
	(3, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="3">\r\n   <nginx-bin>/usr/local/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/usr/local/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>1</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n      <virtual-server name="site2" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s2a.ctrip.com"/>\r\n            <domain name="s2b.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 3, '2015-03-14 22:00:55', '2015-03-14 22:00:55');
/*!40000 ALTER TABLE `archive_slb` ENABLE KEYS */;

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

-- Dumping data for table zeus.slb: ~1 rows (approximately)
DELETE FROM `slb`;
/*!40000 ALTER TABLE `slb` DISABLE KEYS */;
INSERT INTO `slb` (`id`, `name`, `nginx_bin`, `nginx_conf`, `nginx_worker_processes`, `status`, `version`, `created_time`, `last_modified`) VALUES
	(1, 'default', '/usr/local/nginx/sbin', '/usr/local/nginx/conf', 1, 'TEST', 3, '2015-03-10 16:58:16', '2015-03-14 22:00:55');
/*!40000 ALTER TABLE `slb` ENABLE KEYS */;

-- Dumping data for table zeus.slb_domain: ~3 rows (approximately)
DELETE FROM `slb_domain`;
/*!40000 ALTER TABLE `slb_domain` DISABLE KEYS */;
INSERT INTO `slb_domain` (`id`, `slb_virtual_server_id`, `name`, `created_time`, `last_modified`) VALUES
	(1, 1, 's1.ctrip.com', '2015-03-10 16:58:16', '2015-03-14 22:00:55'),
	(2, 2, 's2a.ctrip.com', '2015-03-10 16:58:16', '2015-03-14 22:00:55'),
	(3, 2, 's2b.ctrip.com', '2015-03-10 16:58:16', '2015-03-14 22:00:55');
/*!40000 ALTER TABLE `slb_domain` ENABLE KEYS */;

-- Dumping data for table zeus.slb_server: ~3 rows (approximately)
DELETE FROM `slb_server`;
/*!40000 ALTER TABLE `slb_server` DISABLE KEYS */;
INSERT INTO `slb_server` (`id`, `slb_id`, `ip`, `host_name`, `enable`, `created_time`, `last_modified`) VALUES
	(1, 1, '10.2.25.93', 'uat0358', b'1', '2015-03-10 16:58:16', '2015-03-14 22:00:55'),
	(2, 1, '10.2.25.94', 'uat0359', b'1', '2015-03-10 16:58:16', '2015-03-14 22:00:55'),
	(3, 1, '10.2.25.95', 'uat0360', b'1', '2015-03-10 16:58:16', '2015-03-14 22:00:55');
/*!40000 ALTER TABLE `slb_server` ENABLE KEYS */;

-- Dumping data for table zeus.slb_vip: ~1 rows (approximately)
DELETE FROM `slb_vip`;
/*!40000 ALTER TABLE `slb_vip` DISABLE KEYS */;
INSERT INTO `slb_vip` (`id`, `slb_id`, `ip`, `created_time`, `last_modified`) VALUES
	(1, 1, '10.2.25.93', '2015-03-10 16:58:16', '2015-03-14 22:00:55');
/*!40000 ALTER TABLE `slb_vip` ENABLE KEYS */;

-- Dumping data for table zeus.slb_virtual_server: ~2 rows (approximately)
DELETE FROM `slb_virtual_server`;
/*!40000 ALTER TABLE `slb_virtual_server` DISABLE KEYS */;
INSERT INTO `slb_virtual_server` (`id`, `slb_id`, `name`, `port`, `is_ssl`, `created_time`, `last_modified`) VALUES
	(1, 1, 'site1', '80', b'0', '2015-03-10 16:58:16', '2015-03-14 22:00:55'),
	(2, 1, 'site2', '80', b'0', '2015-03-10 16:58:16', '2015-03-14 22:00:55');
/*!40000 ALTER TABLE `slb_virtual_server` ENABLE KEYS */;

-- Dumping data for table zeus.status_app_server: ~0 rows (approximately)
DELETE FROM `status_app_server`;
/*!40000 ALTER TABLE `status_app_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_app_server` ENABLE KEYS */;

-- Dumping data for table zeus.status_server: ~0 rows (approximately)
DELETE FROM `status_server`;
/*!40000 ALTER TABLE `status_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_server` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
