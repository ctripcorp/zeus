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
-- Dumping data for table zeus.app: ~4 rows (approximately)
DELETE FROM `app`;
/*!40000 ALTER TABLE `app` DISABLE KEYS */;
/*!40000 ALTER TABLE `app` ENABLE KEYS */;

-- Dumping data for table zeus.app_health_check: ~4 rows (approximately)
DELETE FROM `app_health_check`;
/*!40000 ALTER TABLE `app_health_check` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_health_check` ENABLE KEYS */;

-- Dumping data for table zeus.app_load_balancing_method: ~4 rows (approximately)
DELETE FROM `app_load_balancing_method`;
/*!40000 ALTER TABLE `app_load_balancing_method` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_load_balancing_method` ENABLE KEYS */;

-- Dumping data for table zeus.app_server: ~8 rows (approximately)
DELETE FROM `app_server`;
/*!40000 ALTER TABLE `app_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_server` ENABLE KEYS */;

-- Dumping data for table zeus.app_slb: ~4 rows (approximately)
DELETE FROM `app_slb`;
/*!40000 ALTER TABLE `app_slb` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_slb` ENABLE KEYS */;

-- Dumping data for table zeus.archive_app: ~14 rows (approximately)
DELETE FROM `archive_app`;
/*!40000 ALTER TABLE `archive_app` DISABLE KEYS */;
/*!40000 ALTER TABLE `archive_app` ENABLE KEYS */;

-- Dumping data for table zeus.archive_slb: ~5 rows (approximately)
DELETE FROM `archive_slb`;
/*!40000 ALTER TABLE `archive_slb` DISABLE KEYS */;
INSERT INTO `archive_slb` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(7, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="1">\r\n   <nginx-bin>/opt/app/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/opt/app/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>2</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 1, '2015-03-17 20:25:56', '2015-03-17 20:25:56');
/*!40000 ALTER TABLE `archive_slb` ENABLE KEYS */;

-- Dumping data for table zeus.build_info: ~1 rows (approximately)
DELETE FROM `build_info`;
/*!40000 ALTER TABLE `build_info` DISABLE KEYS */;
INSERT INTO `build_info` (`id`, `name`, `pending_ticket`, `current_ticket`, `created_time`, `last_modified`) VALUES
	(3, 'default', 5, 5, '2015-03-17 20:26:42', '2015-03-17 20:39:31');
/*!40000 ALTER TABLE `build_info` ENABLE KEYS */;

-- Dumping data for table zeus.conf_app_active: ~4 rows (approximately)
DELETE FROM `conf_app_active`;
/*!40000 ALTER TABLE `conf_app_active` DISABLE KEYS */;
/*!40000 ALTER TABLE `conf_app_active` ENABLE KEYS */;

-- Dumping data for table zeus.conf_slb_active: ~1 rows (approximately)
DELETE FROM `conf_slb_active`;
/*!40000 ALTER TABLE `conf_slb_active` DISABLE KEYS */;
INSERT INTO `conf_slb_active` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(74, 'default', '<?xml version="1.0" encoding="utf-8"?>\r\n<slb name="default" version="1">\r\n   <nginx-bin>/opt/app/nginx/sbin</nginx-bin>\r\n   <nginx-conf>/opt/app/nginx/conf</nginx-conf>\r\n   <nginx-worker-processes>2</nginx-worker-processes>\r\n   <status>TEST</status>\r\n   <vips>\r\n      <vip ip="10.2.25.93"/>\r\n   </vips>\r\n   <slb-servers>\r\n      <slb-server ip="10.2.25.93" host-name="uat0358" enable="true"/>\r\n      <slb-server ip="10.2.25.94" host-name="uat0359" enable="true"/>\r\n      <slb-server ip="10.2.25.95" host-name="uat0360" enable="true"/>\r\n   </slb-servers>\r\n   <virtual-servers>\r\n      <virtual-server name="site1" ssl="false">\r\n         <port>80</port>\r\n         <domains>\r\n            <domain name="s1.ctrip.com"/>\r\n         </domains>\r\n      </virtual-server>\r\n   </virtual-servers>\r\n</slb>\r\n', 1, '2015-03-17 20:26:42', '2015-03-17 20:39:31');
/*!40000 ALTER TABLE `conf_slb_active` ENABLE KEYS */;

-- Dumping data for table zeus.nginx_conf: ~1 rows (approximately)
DELETE FROM `nginx_conf`;
/*!40000 ALTER TABLE `nginx_conf` DISABLE KEYS */;
INSERT INTO `nginx_conf` (`id`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(83, 'default', 'worker_processes 2;\nevents {\n    worker_connections 1024;\n}\nhttp {\n    include    mime.types;\n    default_type    application/octet-stream;\n    keepalive_timeout    65;\nserver {\n    listen    10001;\n    location / {\n        add_header Access-Control-Allow-Origin *;\n        check_status;\n    }\n    location =/status.json {\n        add_header Access-Control-Allow-Origin *;\n        check_status json;\n    }\n}\n    include    upstreams/*.conf;\n    include    vhosts/*.conf;\n}\n', 1, '2015-03-17 20:26:42', '2015-03-17 20:26:42'),
	(84, 'default', 'worker_processes 2;\nevents {\n    worker_connections 1024;\n}\nhttp {\n    include    mime.types;\n    default_type    application/octet-stream;\n    keepalive_timeout    65;\nserver {\n    listen    10001;\n    location / {\n        add_header Access-Control-Allow-Origin *;\n        check_status;\n    }\n    location =/status.json {\n        add_header Access-Control-Allow-Origin *;\n        check_status json;\n    }\n}\n    include    upstreams/*.conf;\n    include    vhosts/*.conf;\n}\n', 2, '2015-03-17 20:26:44', '2015-03-17 20:26:44'),
	(85, 'default', 'worker_processes 2;\nevents {\n    worker_connections 1024;\n}\nhttp {\n    include    mime.types;\n    default_type    application/octet-stream;\n    keepalive_timeout    65;\nserver {\n    listen    10001;\n    location / {\n        add_header Access-Control-Allow-Origin *;\n        check_status;\n    }\n    location =/status.json {\n        add_header Access-Control-Allow-Origin *;\n        check_status json;\n    }\n}\n    include    upstreams/*.conf;\n    include    vhosts/*.conf;\n}\n', 3, '2015-03-17 20:27:14', '2015-03-17 20:27:14'),
	(86, 'default', 'worker_processes 2;\nevents {\n    worker_connections 1024;\n}\nhttp {\n    include    mime.types;\n    default_type    application/octet-stream;\n    keepalive_timeout    65;\nserver {\n    listen    10001;\n    location / {\n        add_header Access-Control-Allow-Origin *;\n        check_status;\n    }\n    location =/status.json {\n        add_header Access-Control-Allow-Origin *;\n        check_status json;\n    }\n}\n    include    upstreams/*.conf;\n    include    vhosts/*.conf;\n}\n', 4, '2015-03-17 20:37:18', '2015-03-17 20:37:18'),
	(87, 'default', 'worker_processes 2;\nevents {\n    worker_connections 1024;\n}\nhttp {\n    include    mime.types;\n    default_type    application/octet-stream;\n    keepalive_timeout    65;\nserver {\n    listen    10001;\n    location / {\n        add_header Access-Control-Allow-Origin *;\n        check_status;\n    }\n    location =/status.json {\n        add_header Access-Control-Allow-Origin *;\n        check_status json;\n    }\n}\n    include    upstreams/*.conf;\n    include    vhosts/*.conf;\n}\n', 5, '2015-03-17 20:39:31', '2015-03-17 20:39:31');
/*!40000 ALTER TABLE `nginx_conf` ENABLE KEYS */;

-- Dumping data for table zeus.nginx_conf_server: ~2 rows (approximately)
DELETE FROM `nginx_conf_server`;
/*!40000 ALTER TABLE `nginx_conf_server` DISABLE KEYS */;
INSERT INTO `nginx_conf_server` (`id`, `slb_name`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(139, 'default', 'site1', 'server {\n    listen    80;\n    server_name     s1.ctrip.com;\n}\n', 4, '2015-03-17 20:37:18', '2015-03-17 20:37:18'),
	(140, 'default', 'site1', 'server {\n    listen    80;\n    server_name     s1.ctrip.com;\n}\n', 5, '2015-03-17 20:39:31', '2015-03-17 20:39:31');
/*!40000 ALTER TABLE `nginx_conf_server` ENABLE KEYS */;

-- Dumping data for table zeus.nginx_conf_upstream: ~2 rows (approximately)
DELETE FROM `nginx_conf_upstream`;
/*!40000 ALTER TABLE `nginx_conf_upstream` DISABLE KEYS */;
INSERT INTO `nginx_conf_upstream` (`id`, `slb_name`, `name`, `content`, `version`, `created_time`, `last_modified`) VALUES
	(139, 'default', 'site1', '', 4, '2015-03-17 20:37:18', '2015-03-17 20:37:18'),
	(140, 'default', 'site1', '', 5, '2015-03-17 20:39:31', '2015-03-17 20:39:31');
/*!40000 ALTER TABLE `nginx_conf_upstream` ENABLE KEYS */;

-- Dumping data for table zeus.nginx_server: ~3 rows (approximately)
DELETE FROM `nginx_server`;
/*!40000 ALTER TABLE `nginx_server` DISABLE KEYS */;
INSERT INTO `nginx_server` (`id`, `slb_name`, `ip`, `version`, `created_time`, `last_modified`) VALUES
	(40, 'default', '10.2.25.93', 4, '2015-03-17 20:25:56', '2015-03-17 20:39:31'),
	(41, 'default', '10.2.25.94', 4, '2015-03-17 20:25:56', '2015-03-17 20:39:32'),
	(42, 'default', '10.2.25.95', 4, '2015-03-17 20:25:56', '2015-03-17 20:39:32');
/*!40000 ALTER TABLE `nginx_server` ENABLE KEYS */;

-- Dumping data for table zeus.server: ~8 rows (approximately)
DELETE FROM `server`;
/*!40000 ALTER TABLE `server` DISABLE KEYS */;
/*!40000 ALTER TABLE `server` ENABLE KEYS */;

-- Dumping data for table zeus.slb: ~1 rows (approximately)
DELETE FROM `slb`;
/*!40000 ALTER TABLE `slb` DISABLE KEYS */;
INSERT INTO `slb` (`id`, `name`, `nginx_bin`, `nginx_conf`, `nginx_worker_processes`, `status`, `version`, `created_time`, `last_modified`) VALUES
	(24, 'default', '/opt/app/nginx/sbin', '/opt/app/nginx/conf', 2, 'TEST', 1, '2015-03-17 20:25:56', '2015-03-17 20:25:56');
/*!40000 ALTER TABLE `slb` ENABLE KEYS */;

-- Dumping data for table zeus.slb_domain: ~3 rows (approximately)
DELETE FROM `slb_domain`;
/*!40000 ALTER TABLE `slb_domain` DISABLE KEYS */;
INSERT INTO `slb_domain` (`id`, `slb_virtual_server_id`, `name`, `created_time`, `last_modified`) VALUES
	(46, 31, 's1.ctrip.com', '2015-03-17 20:25:56', '2015-03-17 20:25:56');
/*!40000 ALTER TABLE `slb_domain` ENABLE KEYS */;

-- Dumping data for table zeus.slb_server: ~3 rows (approximately)
DELETE FROM `slb_server`;
/*!40000 ALTER TABLE `slb_server` DISABLE KEYS */;
INSERT INTO `slb_server` (`id`, `slb_id`, `ip`, `host_name`, `enable`, `created_time`, `last_modified`) VALUES
	(41, 24, '10.2.25.93', 'uat0358', b'1', '2015-03-17 20:25:56', '2015-03-17 20:25:56'),
	(42, 24, '10.2.25.94', 'uat0359', b'1', '2015-03-17 20:25:56', '2015-03-17 20:25:56'),
	(43, 24, '10.2.25.95', 'uat0360', b'1', '2015-03-17 20:25:56', '2015-03-17 20:25:56');
/*!40000 ALTER TABLE `slb_server` ENABLE KEYS */;

-- Dumping data for table zeus.slb_vip: ~1 rows (approximately)
DELETE FROM `slb_vip`;
/*!40000 ALTER TABLE `slb_vip` DISABLE KEYS */;
INSERT INTO `slb_vip` (`id`, `slb_id`, `ip`, `created_time`, `last_modified`) VALUES
	(40, 24, '10.2.25.93', '2015-03-17 20:25:56', '2015-03-17 20:25:56');
/*!40000 ALTER TABLE `slb_vip` ENABLE KEYS */;

-- Dumping data for table zeus.slb_virtual_server: ~2 rows (approximately)
DELETE FROM `slb_virtual_server`;
/*!40000 ALTER TABLE `slb_virtual_server` DISABLE KEYS */;
INSERT INTO `slb_virtual_server` (`id`, `slb_id`, `name`, `port`, `is_ssl`, `created_time`, `last_modified`) VALUES
	(31, 24, 'site1', '80', b'0', '2015-03-17 20:25:56', '2015-03-17 20:25:56');
/*!40000 ALTER TABLE `slb_virtual_server` ENABLE KEYS */;

-- Dumping data for table zeus.status_app_server: ~3 rows (approximately)
DELETE FROM `status_app_server`;
/*!40000 ALTER TABLE `status_app_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_app_server` ENABLE KEYS */;

-- Dumping data for table zeus.status_server: ~2 rows (approximately)
DELETE FROM `status_server`;
/*!40000 ALTER TABLE `status_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `status_server` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
